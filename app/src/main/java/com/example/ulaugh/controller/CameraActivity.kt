package com.example.ulaugh.controller

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.databinding.ActivityCameraBinding
import com.example.ulaugh.interfaces.CameraImageReceiver
import com.example.ulaugh.ml.SortingHelper
import com.example.ulaugh.ml.TFLiteImageClassifier
import com.example.ulaugh.model.HomeRecyclerViewItem
import com.example.ulaugh.model.Notification
import com.example.ulaugh.model.Reactions
import com.example.ulaugh.service.CamService
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.Helper
import com.example.ulaugh.utils.SharePref
import com.example.ulaugh.utils.isServiceRunning
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
import java.io.IOException
import javax.inject.Inject
import kotlin.math.log


@AndroidEntryPoint
class CameraActivity : AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback, CameraImageReceiver {
    private var _binding: ActivityCameraBinding? = null
    private val binding get() = _binding!!
    private val MODEL_FILE_NAME = "simple_classifier.tflite"

    //    private val SCALED_IMAGE_BIGGEST_SIZE = 480
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
//    private var pictureService: APictureCapturingService? = null
    private var postRef: DatabaseReference? = null
    private lateinit var notificationRef: DatabaseReference
    var angryReaction = 0
    var fearReaction = 0
    var happyReaction = 0
    var neutralReaction = 0
    var sadReaction = 0
    var surpriseReaction = 0
    var disgustReaction = 0
    var topReactions = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CamService.getInstance(this, this)

        initViews()
        clickEvents()

        checkPermissions()

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
        notificationRef = FirebaseDatabase.getInstance().getReference(Constants.NOTIFICATION)
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

    private fun setUserReaction(userReaction: String) {
        postRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postRef!!.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
//                        val inte = reaction.toInt()
//                        Log.d(TAG, "Emoji: $inte")
                        val reaction =
                            Reactions(userReaction, FirebaseAuth.getInstance().currentUser!!.uid)
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

    private fun postNotification(receiverId: String) {
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
                        sharePref.readString(Constants.PROFILE_PIC, "")!!,
                        "",
                        false,
                        sharePref.readString(Constants.FULL_NAME, "")!!
                    )
                    notificationRef.child(receiverId).push().setValue(notification)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Error ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun showToast(text: String) {
        runOnUiThread { Toast.makeText(this, text, Toast.LENGTH_SHORT).show() }
    }

    // Function to handle successful new image acquisition
//    private fun processImageRequestResult(scaledResultImageBitmap: Bitmap) {
//        val scaledResultImageBitmap = getScaledImageBitmap(resultImageUri)
//        mImageView!!.setImageBitmap(scaledResultImageBitmap)
//
//        // Clear the result of a previous classification
//    }

//    private fun getScaledImageBitmap(imageUri: Uri): Bitmap {
//        var scaledImageBitmap: Bitmap? = null
//        try {
//            val imageBitmap = MediaStore.Images.Media.getBitmap(
//                this.contentResolver,
//                imageUri
//            )
//            val scaledHeight: Int
//            val scaledWidth: Int
//
//            // How many times you need to change the sides of an image
//            val scaleFactor: Float
//
//            // Get larger side and start from exactly the larger side in scaling
//            if (imageBitmap.height > imageBitmap.width) {
//                scaledHeight = SCALED_IMAGE_BIGGEST_SIZE
//                scaleFactor = scaledHeight / imageBitmap.height.toFloat()
//                scaledWidth = (imageBitmap.width * scaleFactor).toInt()
//            } else {
//                scaledWidth = SCALED_IMAGE_BIGGEST_SIZE
//                scaleFactor = scaledWidth / imageBitmap.width.toFloat()
//                scaledHeight = (imageBitmap.height * scaleFactor).toInt()
//            }
//            scaledImageBitmap = Bitmap.createScaledBitmap(
//                imageBitmap,
//                scaledWidth,
//                scaledHeight,
//                true
//            )
//
//            // An image in memory can be rotated
//            scaledImageBitmap = ImageUtils.rotateToNormalOrientation(
//                contentResolver,
//                scaledImageBitmap,
//                imageUri
//            )
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return scaledImageBitmap!!
//    }

    private fun detectFaces(imageBitmap: Bitmap) {
        Log.d("ksdahgsd", "bitmap ${imageBitmap.width}")
        val faceDetectorOptions = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
            .setMinFaceSize(0.1f)
            .build()
        val faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(faceDetectorOptions)
        val firebaseImage = FirebaseVisionImage.fromBitmap(imageBitmap)
        faceDetector.detectInImage(firebaseImage).addOnSuccessListener { faces ->
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

            //Coefficient for indentation of face number
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
                        faceId.toString(),
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
                    Log.d("ksdahgsd", "in:")
                    classifyEmotions(faceBitmap, faceId)
                    faceId++
//                    return@addOnSuccessListener
                }

//                postRef!!.child(postDetail!!.firebase_id).child(postDetail!!.post_id)
//                    .addListenerForSingleValueEvent(object : ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            val item =
//                                snapshot.getValue(HomeRecyclerViewItem.SharePostData::class.java)
//                            Log.d(TAG, "onDataChange: $item")
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//                        }
//                    })
                setEmotions(mClassificationResult!!["Face 1"]?.get(0)!!.first)
                binding.containerLayout.invalidate()
//                    finish()
//                    if (faces.size == 1) {
//                        mClassificationExpandableListView!!.expandGroup(0)
//                    }
                // If no faces are found
            } else {
//                postNotification(postDetail!!.firebase_id)
//                setUserReaction("neutral")
            }
        }
            .addOnFailureListener { e ->
                e.printStackTrace()
//                setCalculationStatusUI(false)
            }
    }

    private fun findMeanEmotion() {
        if (happyReaction > fearReaction && happyReaction > disgustReaction && happyReaction > angryReaction && happyReaction > neutralReaction && happyReaction > sadReaction && happyReaction > surpriseReaction)
            topReactions = "happy"
        else if (fearReaction > happyReaction && fearReaction > disgustReaction && fearReaction > angryReaction && fearReaction > neutralReaction && fearReaction > sadReaction && fearReaction > surpriseReaction)
            topReactions = "fear"
        else if (disgustReaction > happyReaction && disgustReaction > fearReaction && disgustReaction > angryReaction && disgustReaction > neutralReaction && disgustReaction > sadReaction && disgustReaction > surpriseReaction)
            topReactions = "disgust"
        else if (angryReaction > happyReaction && angryReaction > disgustReaction && angryReaction > fearReaction && angryReaction > neutralReaction && angryReaction > sadReaction && angryReaction > surpriseReaction)
            topReactions = "angry"
        else if (neutralReaction > happyReaction && neutralReaction > disgustReaction && neutralReaction > angryReaction && neutralReaction > fearReaction && neutralReaction > sadReaction && neutralReaction > surpriseReaction)
            topReactions = "neutral"
        else if (sadReaction > happyReaction && sadReaction > disgustReaction && sadReaction > angryReaction && sadReaction > neutralReaction && sadReaction > fearReaction && sadReaction > surpriseReaction)
            topReactions = "sad"
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


    private fun setEmotions(emotion: String) {
        Log.d("ksdahgsd", "Byte: $emotion")
        when (emotion) {
                "angry" -> {
                angryReaction++
                binding.reactDetail.text = "You're feeling angry"
                binding.reactIv.setImageDrawable(getDrawable(R.drawable.anger_emotion))
                binding.reactDetail.visibility = View.VISIBLE
                binding.reactIv.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.postDetail.visibility = View.GONE
                binding.tagsTv.visibility = View.GONE
            }
            "sad" -> {
                sadReaction++
                binding.reactDetail.text = "You're so sad"
                binding.reactIv.setImageDrawable(getDrawable(R.drawable.sad_ic))
                binding.reactDetail.visibility = View.VISIBLE
                binding.reactIv.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.postDetail.visibility = View.GONE
                binding.tagsTv.visibility = View.GONE
            }
            "happy" -> {
                happyReaction++
                binding.reactDetail.text = "HAHA You're Happy"
                binding.reactIv.setImageDrawable(getDrawable(R.drawable.haha_ic))
                binding.reactDetail.visibility = View.VISIBLE
                binding.reactIv.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.postDetail.visibility = View.GONE
                binding.tagsTv.visibility = View.GONE
            }
            "fear" -> {
                fearReaction++
                binding.reactDetail.text = "You're in fear"
                binding.reactIv.setImageDrawable(getDrawable(R.drawable.fear_ic))
                binding.reactDetail.visibility = View.VISIBLE
                binding.reactIv.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.postDetail.visibility = View.GONE
                binding.tagsTv.visibility = View.GONE
            }
            "surprise" -> {
                surpriseReaction++
                binding.reactDetail.text = "You're surprising"
                binding.reactIv.setImageDrawable(getDrawable(R.drawable.fear_ic))
                binding.reactDetail.visibility = View.VISIBLE
                binding.reactIv.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.postDetail.visibility = View.GONE
                binding.tagsTv.visibility = View.GONE
            }
            "neutral" -> {
                neutralReaction++
                binding.reactDetail.text = "Your expressions are emotionless"
                binding.reactIv.setImageDrawable(getDrawable(R.drawable.neutral_ic))
                binding.reactDetail.visibility = View.VISIBLE
                binding.reactIv.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.postDetail.visibility = View.GONE
                binding.tagsTv.visibility = View.GONE
            }
            "disgust" -> {
                disgustReaction++
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
    /*override fun onCaptureDone(pictureUrl: String?, pictureData: ByteArray?) {
        if (pictureData != null) {
            runOnUiThread {
                val bitmap = byteToBitmap(pictureData)
//                val bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.size)
//                val nh = (bitmap.height * (512.0 / bitmap.width)).toInt()
//                val scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true)

//                val target = File(pictureUrl)
//                val uri = Uri.parse(path)
                Log.i("skaldhglasd", "done taking picture from camera Activity")
                CoroutineScope(Dispatchers.Main).launch {
//                    processImageRequestResult(bitmap)
                    mClassificationResult!!.clear()
//                    setCalculationStatusUI(true)
//                    detectFaces(bitmap)
                }
                //                Log.d(" target_path", "" + pictureUrl);
//                if (target.exists() && target.isFile && target.canWrite()) {
//                    target.delete()
//                    Log.d("d_file", "" + target.name)
//                }
            }
//            showToast("Picture saved to $pictureUrl")
        }
    }*/

    private fun notifyService(action: String) {

        val intent = Intent(this, CamService::class.java)
        intent.action = action
        startService(intent)
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
                    if (!isServiceRunning(this, CamService::class.java)) {
                        notifyService(CamService.ACTION_START)
                    }
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
                    this,
                    permission
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                neededPermissions.add(permission)
            } else {
                if (!isServiceRunning(this, CamService::class.java)) {
                    notifyService(CamService.ACTION_START)
                }
            }

        }
        if (neededPermissions.isNotEmpty()) {
            requestPermissions(
                neededPermissions.toArray(arrayOf<String>()),
                MY_PERMISSIONS_REQUEST_ACCESS_CODE
            )
        }
    }

    override fun onImageReceived(imageList: ArrayList<Bitmap>) {
        val job = CoroutineScope(Dispatchers.Main).launch {
            Log.d("ksdahgsd", "imageList ${imageList}")
            for (bytes in imageList) {
//                val buffer = image.planes[0].buffer
//                val bytes = ByteArray(buffer.capacity())
//                buffer[bytes]
//                buffer.get(bytes)
//                val bitmapImage = byteToBitmap(bytes)
//                Log.d("ksdahgsd", "bitmap ${bitmapImage.width}")
//                val decodeResponse: ByteArray = Base64.decode(bytes, Base64.DEFAULT or Base64.NO_WRAP)
//                val options = BitmapFactory.Options()
//                val bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                mClassificationResult!!.clear()
                detectFaces(bytes)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            job.join()
            findMeanEmotion()
            setUserReaction(topReactions)
            postNotification(postDetail!!.firebase_id)
        }
    }

    override fun onResume() {
        super.onResume()
    }

//    private fun visibleViews(vararg views: android.view.View) {
//        for (v in views) {
//            v.visibility = android.view.View.VISIBLE
//        }
//    }
//
//    private fun invisibleViews(vararg views: android.view.View) {
//        for (view in views)
//            view.visibility = android.view.View.GONE
//    }

}