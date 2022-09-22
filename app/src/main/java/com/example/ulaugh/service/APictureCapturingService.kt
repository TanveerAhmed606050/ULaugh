package com.example.ulaugh.service

import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.SparseIntArray
import android.view.OrientationEventListener
import android.view.Surface
import com.example.ulaugh.interfaces.PictureCapturingListener

/**
 * Abstract Picture Taking Service.
 *
 * @author hzitoun (zitoun.hamed@gmail.com)
 */
abstract class APictureCapturingService internal constructor(private val activity: Activity) {
    var context: Activity? = null
    var manager: CameraManager? = null
    companion object {
        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }
    /***
     * @return orientation
     */
    val orientation: Int
        get() {
            val rotation = activity.windowManager.defaultDisplay.rotation
            //        return rotation;
            return ORIENTATIONS[rotation]
        }

    fun getJpegOrientation(c: CameraCharacteristics, deviceOrientation: Int): Int {
        var deviceOrientation = deviceOrientation
        if (deviceOrientation == OrientationEventListener.ORIENTATION_UNKNOWN) return 0
        val sensorOrientation =
            c.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90

        // Reverse device orientation for front-facing cameras
//        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        deviceOrientation = -deviceOrientation

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (sensorOrientation + deviceOrientation + 360) % 360
    }

    /**
     * starts pictures capturing process.
     *
     * @param listener picture capturing listener
     */
    abstract fun startCapturing(listener: PictureCapturingListener?)

    init {
        context = activity
        manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
}