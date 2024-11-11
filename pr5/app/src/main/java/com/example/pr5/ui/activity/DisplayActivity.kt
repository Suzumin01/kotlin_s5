package com.example.pr5.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.pr5.ui.adapter.ProductAdapter
import com.example.pr5.R
import com.example.pr5.data.AppDatabase

class DisplayActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "product_database"
        ).build()

        adapter = ProductAdapter(emptyList())
        recyclerView.adapter = adapter

        database.productDao().getAllProducts().observe(this, Observer { products ->
            adapter.updateProducts(products)
        })
    }
}
