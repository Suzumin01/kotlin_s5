package com.example.pr10

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pr10.ui.theme.Pr10Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Pr10Theme {
                val imageList = remember { mutableStateListOf<Bitmap>() }
                AppScaffold(imageList = imageList)
            }
        }
    }

    suspend fun downloadImage(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val inputStream = response.body?.byteStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveImage(image: Bitmap, filename: String) = withContext(Dispatchers.IO) {
        try {
            val fos: FileOutputStream = openFileOutput(filename, MODE_PRIVATE)
            image.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun downloadAndSaveImage(url: String, filename: String, imageList: MutableList<Bitmap>) {
        CoroutineScope(Dispatchers.Main).launch {
            val image: Bitmap? = downloadImage(url)
            if (image != null) {
                saveImage(image, "$filename.png")
                imageList.add(image)
                Toast.makeText(applicationContext, "Image saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Error downloading image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(imageList: MutableList<Bitmap>) {
    var currentScreen by remember { mutableStateOf("Download") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(onScreenSelect = { screen ->
                currentScreen = screen
                scope.launch { drawerState.close() }
            })
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                BottomAppBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Download, contentDescription = "Download") },
                        label = { Text("Download") },
                        selected = currentScreen == "Download",
                        onClick = { currentScreen = "Download" }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.History, contentDescription = "History") },
                        label = { Text("History") },
                        selected = currentScreen == "History",
                        onClick = { currentScreen = "History" }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentScreen) {
                    "Download" -> DownloadScreen(imageList)
                    "History" -> HistoryScreen(imageList)
                }
            }
        }
    }
}

@Composable
fun DrawerContent(onScreenSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextButton(onClick = { onScreenSelect("Download") }) {
            Text("Download Screen")
        }
        TextButton(onClick = { onScreenSelect("History") }) {
            Text("History Screen")
        }
    }
}

@Composable
fun DownloadScreen(imageList: MutableList<Bitmap>) {
    val context = LocalContext.current
    var url by remember { mutableStateOf("") }
    var filename by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter image URL") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = filename,
            onValueChange = { filename = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter file name") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (url.isNotEmpty() && filename.isNotEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val image: Bitmap? = MainActivity().downloadImage(url)
                        if (image != null) {
                            MainActivity().saveImage(image, filename)
                            imageList.add(image)
                            Toast.makeText(context, "Image saved!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text("Download and Save")
        }
    }
}

@Composable
fun HistoryScreen(imageList: List<Bitmap>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(imageList) { bitmap ->
            ImageItem(bitmap)
        }
    }
}

@Composable
fun ImageItem(bitmap: Bitmap) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    Pr10Theme {
        AppScaffold(imageList = remember { mutableStateListOf() })
    }
}
