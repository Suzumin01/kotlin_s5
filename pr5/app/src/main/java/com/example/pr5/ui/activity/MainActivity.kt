package com.example.pr5.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.pr5.R
import com.example.pr5.data.AppDatabase
import com.example.pr5.data.ProductEntity
import com.example.pr5.retrofit.api.MainApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val displayButton = findViewById<Button>(R.id.displayButton)
        displayButton.setOnClickListener {
            val intent = Intent(this, DisplayActivity::class.java)
            startActivity(intent)
        }

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "product_database"
        ).fallbackToDestructiveMigration().build()

        val tv = findViewById<TextView>(R.id.tv)
        val b = findViewById<Button>(R.id.button)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://dummyjson.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val mainApi = retrofit.create(MainApi::class.java)

        b.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val randomId = (1..100).random()
                val product = mainApi.getProductById(randomId)
                val productEntity = ProductEntity(
                    product.id,
                    product.title,
                    product.description,
                    product.price,
                    product.category
                )

                database.productDao().insertProduct(productEntity)

                runOnUiThread {
                    tv.text = "Product saved: ${product.title}"
                }
            }
        }
    }
}
