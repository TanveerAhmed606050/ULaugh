package com.example.ulaugh.controller

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.ulaugh.R
import com.example.ulaugh.databinding.ActivityReactDetailBinding
import com.example.ulaugh.databinding.ActivityUserReactBinding
import com.example.ulaugh.utils.SharePref
//import com.google.android.gms.vision.face.FaceDetector.ACCURATE_MODE
//import com.google.firebase.ml.vision.FirebaseVision
//import com.google.firebase.ml.vision.common.FirebaseVisionImage
//import com.google.firebase.ml.vision.face.FirebaseVisionFace
//import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserReactActivity : AppCompatActivity() {
    private var _binding: ActivityUserReactBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var tokenManager: SharePref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityUserReactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.included.headerTitle.text = getString(R.string.react)
        binding.included.backBtn.setOnClickListener {
            finish()
        }
    }

//    private fun configFirebaseOptions() {
//        firebaseOptions = FirebaseVisionFaceDetectorOptions.Builder()
//            .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
//            .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
//            .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
//            .setMinFaceSize(0.15f)
//            .setTrackingEnabled(true)
//            .build()
//    }
//    private fun faceDetect(image: Bitmap) {
//        val firebaseImage = FirebaseVisionImage.fromBitmap(image)
//        val detector = FirebaseVision.getInstance().getVisionFaceDetector(firebaseOptions)
//
//        val result = detector.detectInImage(firebaseImage)
//            .addOnSuccessListener {
//                processFaces(it)
//            }.addOnFailureListener {
//                it.printStackTrace()
//                Toast.makeText(this, "Something bad happended", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun processFaces(faces: List<FirebaseVisionFace>) {
//
//        if (faces.isEmpty()) {
//            Toast.makeText(this, "Is there a face here?", Toast.LENGTH_SHORT).show()
//        } else {
//            for (face in faces) {
//                if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
//                    val smileProb = face.smilingProbability
//                    if (smileProb > 0.5) {
//                        Toast.makeText(this, "Smily face :)", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Toast.makeText(this, "Not a Smily face :(", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Is there a face here?", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
}