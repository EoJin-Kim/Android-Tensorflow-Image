package com.ej.tensorflowimage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val galleryBtn = findViewById<Button>(R.id.galleryBtn)
        galleryBtn.setOnClickListener {
            val i = Intent(this, GalleryActivity::class.java)
            startActivity(i)
        }

        val cameraBtn = findViewById<Button>(R.id.cameraBtn)
        cameraBtn.setOnClickListener {
            val i = Intent(this, CameraActivity::class.java)
            startActivity(i)
        }
    }
}