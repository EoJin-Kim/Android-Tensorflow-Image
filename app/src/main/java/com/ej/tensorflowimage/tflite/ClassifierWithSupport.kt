package com.ej.tensorflowimage.tflite

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ColorSpaceType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteOrder


class ClassifierWithSupport constructor(
    val context: Context
) {

    lateinit var interpreter : Interpreter

    lateinit var modelInputShape : IntArray
    var modelInputWidth : Int = 0
    var modelInputHeight : Int = 0
    var modelInputChannel : Int = 0
    lateinit var modelInputDataType : DataType
    lateinit var inputImage : TensorImage
    lateinit var outputBuffer : TensorBuffer
    private lateinit var labels : List<String>

    @Throws(IOException::class)
    fun init() {
        val model = FileUtil.loadMappedFile(context, MODEL_NAME)
        model.order(ByteOrder.nativeOrder())
        interpreter = Interpreter(model)
        initModelShape()
        labels = FileUtil.loadLabels(context, LABEL_FILE)
    }

    private fun initModelShape() {
        val inputTensor : Tensor = interpreter.getInputTensor(0)
        modelInputShape = inputTensor.shape()
        modelInputChannel = modelInputShape[0]
        modelInputWidth = modelInputShape[1]
        modelInputHeight = modelInputShape[2]
        modelInputDataType = inputTensor.dataType()
        inputImage = TensorImage(inputTensor.dataType())

        val outputTensor = interpreter.getOutputTensor(0)
        outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), outputTensor.dataType())

    }


    private fun loadImage(bitmap: Bitmap) : TensorImage {
        if (bitmap.config != Bitmap.Config.ARGB_8888) {
            inputImage.load(convertBitmapToARGB8888(bitmap))
        } else {
            inputImage.load(bitmap)
        }

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(modelInputWidth, modelInputHeight, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(NormalizeOp(0.0f, 255.0f))
            .build()

        return imageProcessor.process(inputImage)
    }

    fun classify(image: Bitmap) : Pair<String,Float>{
        inputImage = loadImage(image)
        interpreter.run(inputImage.buffer, outputBuffer.buffer.rewind())

        val output : Map<String, Float> = TensorLabel(labels,outputBuffer).mapWithFloatValue
        return argmax(output)
    }

    private fun argmax(map: Map<String, Float>): Pair<String, Float> {
        var maxKey = ""
        var maxVal = -1f
        for (entry in map.entries) {
            val f = entry.value
            if (f > maxVal) {
                maxKey = entry.key
                maxVal = f
            }
        }
        return Pair(maxKey, maxVal)
    }

    private fun convertBitmapToARGB8888(bitmap: Bitmap): Bitmap {
        return bitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

    fun finish() {
        interpreter.close()
    }



    companion object {
        val MODEL_NAME = "mobilenet_imagenet_model.tflite"
        val LABEL_FILE = "labels.txt"

    }
}