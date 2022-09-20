package com.example.ulaugh.controller

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Pair
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.example.ulaugh.R
import com.example.ulaugh.databinding.ActivityCameraBinding
import com.example.ulaugh.ml.ImageUtils
import com.example.ulaugh.ml.SortingHelper
import com.example.ulaugh.ml.TFLiteImageClassifier
import com.google.firebase.database.core.view.Change
import com.google.firebase.database.core.view.View
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {
    private var _binding: ActivityCameraBinding? = null
    private val binding get() = _binding!!
    private val MODEL_FILE_NAME = "simple_classifier.tflite"
    private val SCALED_IMAGE_BIGGEST_SIZE = 480
    private var mClassifier: TFLiteImageClassifier? = null
    private var mImageView: ImageView? = null
    private var mClassificationResult: HashMap<String, ArrayList<Pair<String, String>>>? = null
    lateinit var photoFile: File
    private var currentPhotoPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mClassifier = TFLiteImageClassifier(
            this.assets,
            MODEL_FILE_NAME,
            resources.getStringArray(R.array.emotions)
        )

        mClassificationResult = LinkedHashMap()

        mImageView = findViewById(R.id.image_view)
        takePicture()
//        binding.cameraOk.setOnClickListener({ takePicture() })


//        fullScreen()
//        if (allPermissionsGranted()) {
//            startCamera(isFront)
//        } else {
//            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
//        }
//        binding.cameraOk.setOnClickListener(this)
//        binding.flipCamera.setOnClickListener(this)
//        outputDirectory = getOutputDirectory()
    }

    // Function to create an intent to take a photo
    private fun takePicture() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = createImageFile()
        val uri =
            FileProvider.getUriForFile(this, "com.example.ulaugh.fileprovider", photoFile)
        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        resultLauncher.launch(pictureIntent)
//        startActivityForResult(pictureIntent, 100)
    }

    // Create a temporary file for the image
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            .apply { currentPhotoPath = absolutePath }
//        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply
//        { currentPhotoPath = absolutePath }
    }

    override fun onDestroy() {
        super.onDestroy()
        mClassifier!!.close()
        val picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        for (tempFile in picturesDir!!.listFiles()!!) {
            tempFile.delete()
        }
    }
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // There are no request codes

                val imageUri =
                    FileProvider.getUriForFile(this, "com.example.ulaugh.fileprovider", photoFile)
                processImageRequestResult(imageUri)
//                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            }
        }

    // Function to handle successful new image acquisition
    private fun processImageRequestResult(resultImageUri: Uri) {
        val scaledResultImageBitmap = getScaledImageBitmap(resultImageUri)
        mImageView!!.setImageBitmap(scaledResultImageBitmap)

        // Clear the result of a previous classification
        mClassificationResult!!.clear()
        setCalculationStatusUI(true)
        detectFaces(scaledResultImageBitmap)
    }

    private fun getScaledImageBitmap(imageUri: Uri): Bitmap {
        var scaledImageBitmap: Bitmap? = null
        try {
            val imageBitmap = MediaStore.Images.Media.getBitmap(
                this.contentResolver,
                imageUri
            )
            val scaledHeight: Int
            val scaledWidth: Int

            // How many times you need to change the sides of an image
            val scaleFactor: Float

            // Get larger side and start from exactly the larger side in scaling
            if (imageBitmap.height > imageBitmap.width) {
                scaledHeight = SCALED_IMAGE_BIGGEST_SIZE
                scaleFactor = scaledHeight / imageBitmap.height.toFloat()
                scaledWidth = (imageBitmap.width * scaleFactor).toInt()
            } else {
                scaledWidth = SCALED_IMAGE_BIGGEST_SIZE
                scaleFactor = scaledWidth / imageBitmap.width.toFloat()
                scaledHeight = (imageBitmap.height * scaleFactor).toInt()
            }
            scaledImageBitmap = Bitmap.createScaledBitmap(
                imageBitmap,
                scaledWidth,
                scaledHeight,
                true
            )

            // An image in memory can be rotated
            scaledImageBitmap = ImageUtils.rotateToNormalOrientation(
                contentResolver,
                scaledImageBitmap,
                imageUri
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return scaledImageBitmap!!
    }

    private fun detectFaces(imageBitmap: Bitmap) {
        val faceDetectorOptions = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
            .setMinFaceSize(0.1f)
            .build()
        val faceDetector = FirebaseVision.getInstance()
            .getVisionFaceDetector(faceDetectorOptions)
        val firebaseImage = FirebaseVisionImage.fromBitmap(imageBitmap)
        val result = faceDetector.detectInImage(firebaseImage)
            .addOnSuccessListener { faces ->

                // When the search for faces was successfully completed
                val imageBitmap = firebaseImage.bitmap
                // Temporary Bitmap for drawing
                val tmpBitmap = Bitmap.createBitmap(
                    imageBitmap.width,
                    imageBitmap.height,
                    imageBitmap.config
                )

                // Create an image-based canvas
                val tmpCanvas = Canvas(tmpBitmap)
                tmpCanvas.drawBitmap(
                    imageBitmap, 0f, 0f,
                    null
                )
                val paint = Paint()
                paint.color = Color.GREEN
                paint.strokeWidth = 2f
                paint.textSize = 48f

                // Coefficient for indentation of face number
                val textIndentFactor = 0.1f

                // If at least one face was found
                if (!faces.isEmpty()) {
                    // faceId ~ face text number
                    var faceId = 1
                    for (face in faces) {
                        val faceRect: Rect = getInnerRect(
                            face.boundingBox,
                            imageBitmap.width,
                            imageBitmap.height
                        )!!

                        // Draw a rectangle around a face
                        paint.style = Paint.Style.STROKE
                        tmpCanvas.drawRect(faceRect, paint)

                        // Draw a face number in a rectangle
                        paint.style = Paint.Style.FILL
                        tmpCanvas.drawText(
                            Integer.toString(faceId),
                            faceRect.left +
                                    faceRect.width() * textIndentFactor,
                            faceRect.bottom -
                                    faceRect.height() * textIndentFactor,
                            paint
                        )

                        // Get subarea with a face
                        val faceBitmap = Bitmap.createBitmap(
                            imageBitmap,
                            faceRect.left,
                            faceRect.top,
                            faceRect.width(),
                            faceRect.height()
                        )
                        classifyEmotions(faceBitmap, faceId)
                        faceId++
                    }

                    // Set the image with the face designations
                    mImageView!!.setImageBitmap(tmpBitmap)

                    // If single face, then immediately open the list
                    Log.d("lsdagj", "detectFaces: ${mClassificationResult.toString()} ${mClassificationResult!!.size}")
                    Toast.makeText(
                        this@CameraActivity,
                        mClassificationResult!!["Face 1"]?.get(0)!!.first,
                        Toast.LENGTH_LONG
                    ).show()
//                    if (faces.size == 1) {
//                        mClassificationExpandableListView!!.expandGroup(0)
//                    }
                    // If no faces are found
                } else {
                    Toast.makeText(
                        this@CameraActivity,
                        getString(R.string.faceless),
                        Toast.LENGTH_LONG
                    ).show()
                }
                setCalculationStatusUI(false)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                setCalculationStatusUI(false)
            }
    }

    private fun classifyEmotions(imageBitmap: Bitmap, faceId: Int) {
        val result = mClassifier!!.classify(imageBitmap, true)

        // Sort by increasing probability
        val sortedResult = SortingHelper.sortByValues(result) as LinkedHashMap<String?, Float>
        val reversedKeys = ArrayList(sortedResult.keys)
//         Change the order to get a decrease in probabilities
        reversedKeys.reverse()
        val faceGroup = ArrayList<Pair<String, String>>()
        for (key in reversedKeys) {
            val percentage = String.format("%.1f%%", sortedResult[key]!! * 100)
            faceGroup.add(Pair(key, percentage))
        }
        val groupName = getString(R.string.face) + " " + faceId
        mClassificationResult!!.put(groupName, faceGroup)
    }

    //Change the interface depending on the status of calculations
    private fun setCalculationStatusUI(isCalculationRunning: Boolean) {
        if (isCalculationRunning) {
            binding.classificationProgressBar.visibility = ProgressBar.VISIBLE
        } else {
            binding.classificationProgressBar.visibility = ProgressBar.INVISIBLE
        }
    }

    private fun setEmotions(emotion:String){
        when(emotion ) {
            "angry" ->{
                binding.reactDetail.text = "You're feeling angry"
            }
            "sad" ->{
                binding.reactDetail.text = "You're so sad"
            }
            "happy" ->{
                binding.reactDetail.text = "HAHA You're Happy"
            }
            "fear" ->{
                binding.reactDetail.text = "You're in fear"
            }
            "surprise" ->{
                binding.reactDetail.text = "You're surprising"
            }
            "neutral" ->{
                binding.reactDetail.text = "Your expressions are emotionless"
            }
            "disgust" ->{
                binding.reactDetail.text = "You're feeling disgusting"
            }
        }
    }

    // Get a rectangle that lies inside the image area
    private fun getInnerRect(rect: Rect, areaWidth: Int, areaHeight: Int): Rect {
        val innerRect = Rect(rect)
        if (innerRect.top < 0) {
            innerRect.top = 0
        }
        if (innerRect.left < 0) {
            innerRect.left = 0
        }
        if (rect.bottom > areaHeight) {
            innerRect.bottom = areaHeight
        }
        if (rect.right > areaWidth) {
            innerRect.right = areaWidth
        }
        return innerRect
    }

    private fun visibleViews(vararg views: android.view.View) {
        for (v in views) {
            v.visibility = android.view.View.VISIBLE
        }
    }

    private fun invisibleViews(vararg views: android.view.View){
        for (view in views)
            view.visibility = android.view.View.GONE
    }

}