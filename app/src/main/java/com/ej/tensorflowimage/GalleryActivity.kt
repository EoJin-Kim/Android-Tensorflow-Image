package com.ej.tensorflowimage

import android.R.attr
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ej.tensorflowimage.tflite.ClassifierWithSupport
import java.io.IOException
import java.util.*


class GalleryActivity : AppCompatActivity() {
    val TAG = "[IC]GalleryActivity"
    val GALLERY_IMAGE_REQUEST_CODE = 1

    lateinit var cls : ClassifierWithSupport
    lateinit var imageView : ImageView
    lateinit var textView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val selectBtn = findViewById<Button>(R.id.selectBtn)
        selectBtn.setOnClickListener {
            getImageFromGallery()
        }

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)

        cls = ClassifierWithSupport(this)

        try {
            cls.init()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === RESULT_OK && requestCode === GALLERY_IMAGE_REQUEST_CODE) {
            if (attr.data == null) {
                return
            }
            val selectedImage: Uri = data!!.data!!
            var bitmap: Bitmap? = null
            try {
                bitmap = if (Build.VERSION.SDK_INT >= 29) {
                    val src: ImageDecoder.Source =
                        ImageDecoder.createSource(contentResolver, selectedImage)
                    ImageDecoder.decodeBitmap(src)
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                }
            } catch (ioe: IOException) {
                Log.e(TAG, "Failed to read Image", ioe)
            }
            if (bitmap != null) {
                val (first, second) = cls.classify(bitmap)
                val resultStr: String = String.format(
                    Locale.ENGLISH,
                    "class : %s, prob : %.2f%%",
                    first, second * 100
                )
                textView.text = resultStr
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun getImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
        //        Intent intent = new Intent(Intent.ACTION_PICK,
//                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE)
    }
}