package com.example.pr10

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Pr10Theme {
                val context = LocalContext.current
                val imageList = remember { mutableStateListOf<Bitmap>() }

                MainScreen(
                    imageList = imageList,
                    onSaveClick = { url, filename ->
                        if (url.isEmpty() || filename.isEmpty()) {
                            Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
                        } else {
                            downloadAndSaveImage(url, filename, imageList)
                        }
                    }
                )
            }
        }
    }

    private suspend fun downloadImage(url: String): Bitmap? = withContext(Dispatchers.IO) {
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

    private suspend fun saveImage(image: Bitmap, filename: String) = withContext(Dispatchers.IO) {
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

@Composable
fun MainScreen(
    imageList: List<Bitmap>,
    onSaveClick: (String, String) -> Unit
) {
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
            label = { Text("Введите ссылку на изображение") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = filename,
            onValueChange = { filename = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Введите название файла") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSaveClick(url, filename)
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Сохранить")
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(imageList) { bitmap ->
                ImageItem(bitmap = bitmap)
            }
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
            contentDescription = "Downloaded Image",
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp)
        )
        Divider()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    Pr10Theme {
        MainScreen(
            imageList = emptyList(),
            onSaveClick = { _, _ -> }
        )
    }
}
