package com.ej.tensorflowimage

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.ej.tensorflowimage.tflite.ClassifierWithModel
import java.io.File
import java.io.IOException
import java.util.*


class CameraActivity : AppCompatActivity() {

    val TAG = "[IC]CameraActivity"
    val CAMERA_IMAGE_REQUEST_CODE  = 1
    private val KEY_SELECTED_URI = "KEY_SELECTED_URI"

    lateinit var cls : ClassifierWithModel
    lateinit var imageView : ImageView
    lateinit var textView : TextView

    lateinit var selectedImageUri : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)


        val takeBtn = findViewById<Button>(R.id.takeBtn)
        takeBtn.setOnClickListener {
            getImageFromCamera()
        }

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)

        cls = ClassifierWithModel(this)

        try {
            cls.init()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }

        if (savedInstanceState != null) {
            val uri: Uri? = savedInstanceState.getParcelable(KEY_SELECTED_URI)
            if (uri != null) selectedImageUri = uri
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK &&
            requestCode == CAMERA_IMAGE_REQUEST_CODE
        ) {
            var bitmap: Bitmap? = null
            try {
                bitmap = if (Build.VERSION.SDK_INT >= 29) {
                    val src: ImageDecoder.Source = ImageDecoder.createSource(
                        contentResolver, selectedImageUri
                    )
                    ImageDecoder.decodeBitmap(src)
                } else {
                    MediaStore.Images.Media.getBitmap(
                        contentResolver, selectedImageUri
                    )
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
                imageView.setImageBitmap(bitmap)
                textView.text = resultStr
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)

        outState.putParcelable(KEY_SELECTED_URI, selectedImageUri)
    }
    private fun getImageFromCamera() {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "picture.jpg")
        selectedImageUri = FileProvider.getUriForFile(this, packageName, file)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri)
        startActivityForResult(intent, CAMERA_IMAGE_REQUEST_CODE)
    }
}