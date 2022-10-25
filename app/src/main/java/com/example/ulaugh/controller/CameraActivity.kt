package com.example.ulaugh.controller

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.databinding.ActivityCameraBinding
import com.example.ulaugh.interfaces.PictureCapturingListener
import com.example.ulaugh.ml.ImageUtils
import com.example.ulaugh.ml.SortingHelper
import com.example.ulaugh.ml.TFLiteImageClassifier
import com.example.ulaugh.model.HomeRecyclerViewItem
import com.example.ulaugh.model.Notification
import com.example.ulaugh.model.Reactions
import com.example.ulaugh.service.APictureCapturingService
import com.example.ulaugh.service.PictureCapturingServiceImpl
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.Constants.TAG
import com.example.ulaugh.utils.Helper
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class CameraActivity : AppCompatActivity(), PictureCapturingListener,
    ActivityCompat.OnRequestPermissionsResultCallback {
    private var _binding: ActivityCameraBinding? = null
    private val binding get() = _binding!!
    private val MODEL_FILE_NAME = "simple_classifier.tflite"
    private val SCALED_IMAGE_BIGGEST_SIZE = 480
    private var mClassifier: TFLiteImageClassifier? = null
    private var mImageView: ImageView? = null
    private var mClassificationResult: HashMap<String, ArrayList<Pair<String, String>>>? = null
    private var postDetail: HomeRecyclerViewItem.SharePostData? = null
    @Inject
    lateinit var sharePref: SharePref

    private val requiredPermissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )
    private val MY_PERMISSIONS_REQUEST_ACCESS_CODE = 1

    //The capture service
    private var pictureService: APictureCapturingService? = null
    private var postRef: DatabaseReference? = null
    private lateinit var notificationRef:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        clickEvents()

        checkPermissions()
        // getting instance of the Service from PictureCapturingServiceImpl

        mClassifier = TFLiteImageClassifier(
            this.assets,
            MODEL_FILE_NAME,
            resources.getStringArray(R.array.emotions)
        )

        mClassificationResult = LinkedHashMap()

        mImageView = findViewById(R.id.image_view)
    }

    private fun clickEvents() {
        binding.crossBtn.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        sharePref.writeBoolean(Constants.EMOTION_UPDATE, true)//api home call
        if (intent != null)
            postDetail =
                Gson().fromJson(
                    intent.getStringExtra(Constants.POST),
                    object : TypeToken<HomeRecyclerViewItem.SharePostData>() {}.type
                )
        if (postDetail != null) {
            Glide.with(this)
                .load(postDetail!!.image_url)
                .centerCrop()
                .fitCenter()
                .thumbnail()
                .placeholder(R.drawable.seokangjoon)
                .into(binding.imageView)
            postRef = FirebaseDatabase.getInstance().getReference(Constants.POST_SHARE_REF)
                .child(postDetail!!.firebase_id).child(postDetail!!.post_id)
                .child(Constants.REACTION)
            binding.postDetail.text = postDetail!!.description
            binding.tagsTv.text = postDetail!!.tagsList
        }

//        Log.d("lsdagj", "detectFaces: ${mClassificationResult.toString()} ${mClassificationResult!!.size}")
    }

    private fun setUserReaction(reaction: String) {
        postRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postRef!!.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
//                        val inte = reaction.toInt()
//                        Log.d(TAG, "Emoji: $inte")
                        val reaction =
                            Reactions(reaction, FirebaseAuth.getInstance().currentUser!!.uid)
                        var lastKey = "-1"
                        for (child in mutableData.children) {
                            lastKey = child.key!!
                        }
                        val nextKey = lastKey.toInt() + 1
                        mutableData.child("" + nextKey).value = reaction

                        // Set value and report transaction success
                        return Transaction.success(mutableData)
                    }

                    override fun onComplete(
                        databaseError: DatabaseError?, b: Boolean,
                        dataSnapshot: DataSnapshot?
                    ) {
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@CameraActivity,
                    "onDataChange: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

//        postRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val hashMap = HashMap<String, String>()
//                hashMap[Constants.REACTION_TYPE] = reaction
//                postRef!!.setValue(hashMap)
////                val item = snapshot.getValue(HomeRecyclerViewItem.SharePostData::class.java)
//                Log.d(TAG, "onDataChange: $snapshot")
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(
//                    this@CameraActivity,
//                    "onDataChange: ${error.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
////                Log.d(TAG)
//            }
//
//        })
    }

    private fun postNotification(receiverId:String) {
        val time = Helper().localToGMT()
        notificationRef.child(receiverId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notification = Notification(
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        receiverId,
                        Constants.REACTED,
                        "Reaction",
                        "${sharePref.readString(Constants.FULL_NAME, "")} reacted your post", time,
                        sharePref.readString(Constants.PROFILE_PIC, "")!!
                    )
                    notificationRef.child(receiverId).push().setValue(notification)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CameraActivity, "Error ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }

    private fun showToast(text: String) {
        runOnUiThread { Toast.makeText(this, text, Toast.LENGTH_SHORT).show() }
    }

    // Function to handle successful new image acquisition
    private fun processImageRequestResult(scaledResultImageBitmap: Bitmap) {
//        val scaledResultImageBitmap = getScaledImageBitmap(resultImageUri)
//        mImageView!!.setImageBitmap(scaledResultImageBitmap)

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
                if (faces.isNotEmpty()) {
                    // faceId ~ face text number
                    var faceId = 1
                    for (face in faces) {
                        val faceRect: Rect = getInnerRect(
                            face.boundingBox,
                            imageBitmap.width,
                            imageBitmap.height
                        )

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
//                    mImageView!!.setImageBitmap(tmpBitmap)

                    // If single face, then immediately open the list
//                    Log.d(
//                        "lsdagj",
//                        "detectFaces: ${mClassificationResult.toString()} ${mClassificationResult!!.size}"
//                    )
                    postRef!!.child(postDetail!!.firebase_id).child(postDetail!!.post_id)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val item =
                                    snapshot.getValue(HomeRecyclerViewItem.SharePostData::class.java)
                                Log.d(TAG, "onDataChange: $item")
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }

                        })
//                    Toast.makeText(
//                        this@CameraActivity,
//                        mClassificationResult!!["Face 1"]?.get(0)!!.first,
//                        Toast.LENGTH_LONG
//                    ).show()
                    setUserReaction(mClassificationResult!!["Face 1"]?.get(0)!!.first)
                    setEmotions(mClassificationResult!!["Face 1"]?.get(0)!!.first)
                    binding.containerLayout.invalidate()
//                    finish()
//                    if (faces.size == 1) {
//                        mClassificationExpandableListView!!.expandGroup(0)
//                    }
                    // If no faces are found
                } else {
                    setUserReaction("neutral")
//                    setEmotions("neutral")
//                    Toast.makeText(
//                        this@CameraActivity,
//                        getString(R.string.faceless),
//                        Toast.LENGTH_LONG
//                    ).show()
//                    finish()
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
//            binding.classificationProgressBar.visibility = ProgressBar.VISIBLE
        } else {
            binding.classificationProgressBar.visibility = ProgressBar.INVISIBLE
        }
    }

    private fun setEmotions(emotion: String) {
        when (emotion) {
            "angry" -> {
                binding.reactDetail.text = "You're feeling angry"
                binding.reactIv.setImageDrawable(getDrawable(R.drawable.anger_emotion))
                binding.reactDetail.visibility = View.VISIBLE
                binding.reactIv.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.postDetail.visibility = View.GONE
                binding.tagsTv.visibility = View.GONE
            }
            "sad" -> {
                binding.reactDetail.text = "You're so sad"
                binding.reactIv.setImageDrawable(getDrawable(R.drawable.sad_ic))
                binding.reactDetail.visibility = View.VISIBLE
                binding.reactIv.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.postDetail.visibility = View.GONE
                binding.tagsTv.visibility = View.GONE
            }
            "happy" -> {
                binding.reactDetail.text = "HAHA You're Happy"
                binding.reactIv.setImageDrawable(getDrawable(R.drawable.haha_ic))
                binding.reactDetail.visibility = View.VISIBLE
                binding.reactIv.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.postDetail.visibility = View.GONE
                binding.tagsTv.visibility = View.GONE
            }
            "fear" -> {
                binding.reactDetail.text = "You're in fear"
                binding.reactIv.setImageDrawable(getDrawable(R.drawable.fear_ic))
                binding.reactDetail.visibility = View.VISIBLE
                binding.reactIv.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.postDetail.visibility = View.GONE
                binding.tagsTv.visibility = View.GONE
            }
            "surprise" -> {
                binding.reactDetail.text = "You're surprising"
                binding.reactIv.setImageDrawable(getDrawable(R.drawable.fear_ic))
                binding.reactDetail.visibility = View.VISIBLE
                binding.reactIv.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.postDetail.visibility = View.GONE
                binding.tagsTv.visibility = View.GONE
            }
            "neutral" -> {
                binding.reactDetail.text = "Your expressions are emotionless"
                binding.reactIv.setImageDrawable(getDrawable(R.drawable.neutral_ic))
                binding.reactDetail.visibility = View.VISIBLE
                binding.reactIv.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.postDetail.visibility = View.GONE
                binding.tagsTv.visibility = View.GONE
            }
            "disgust" -> {
                binding.reactDetail.text = "You're feeling disgusting"
                binding.reactIv.setImageDrawable(getDrawable(R.drawable.sad_ic))
                binding.reactDetail.visibility = View.VISIBLE
                binding.reactIv.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.postDetail.visibility = View.GONE
                binding.tagsTv.visibility = View.GONE
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

    /**
     * Displaying the pictures taken.
     */
    override fun onCaptureDone(pictureUrl: String?, pictureData: ByteArray?) {
        if (pictureData != null) {
            runOnUiThread {
                val bitmap = byteToBitmap(pictureData)
//                val bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.size)
//                val nh = (bitmap.height * (512.0 / bitmap.width)).toInt()
//                val scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true)

//                val target = File(pictureUrl)
//                val uri = Uri.parse(path)
                CoroutineScope(Dispatchers.Main).launch {
                    processImageRequestResult(bitmap)
                }
                //                Log.d(" target_path", "" + pictureUrl);
//                if (target.exists() && target.isFile && target.canWrite()) {
//                    target.delete()
//                    Log.d("d_file", "" + target.name)
//                }
            }
//            showToast("Picture saved to $pictureUrl")
        }
    }

    private fun byteToBitmap(pictureData: ByteArray): Bitmap {
//        val file = File(this.getApplicationContext().filesDir, "name")
        try {
//            FileOutputStream(file).use { output ->
//                output.write(pictureData)
//                picturesTaken!!.put(file.path, bytes)
//                Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show()
//            }
        } catch (e: IOException) {
//            Toast.makeText(this, "Exception occurred while saving picture to external storage $e", Toast.LENGTH_LONG).show()
        }
        val bmp = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.size)
//        binding.imageView.setImageBitmap(bmp)
        return bmp
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_CODE -> {
                if (!(grantResults.isNotEmpty()
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    showToast("Please give permission")
                } else {
                    pictureService = PictureCapturingServiceImpl.getInstance(this)
                    pictureService!!.startCapturing(this)
                }
            }
        }
    }

    /**
     * checking  permissions at Runtime.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun checkPermissions() {
        val neededPermissions: ArrayList<String> = ArrayList()
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    permission
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                neededPermissions.add(permission)
            } else {
                pictureService = PictureCapturingServiceImpl.getInstance(this)
//        showToast("Starting capture!")
                pictureService!!.startCapturing(this)
            }

        }
        if (neededPermissions.isNotEmpty()) {
            requestPermissions(
                neededPermissions.toArray(arrayOf<String>()),
                MY_PERMISSIONS_REQUEST_ACCESS_CODE
            )
        }
    }

    private fun visibleViews(vararg views: android.view.View) {
        for (v in views) {
            v.visibility = android.view.View.VISIBLE
        }
    }

    private fun invisibleViews(vararg views: android.view.View) {
        for (view in views)
            view.visibility = android.view.View.GONE
    }

}