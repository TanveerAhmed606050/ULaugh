package com.example.ulaugh.service

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import com.example.ulaugh.interfaces.PictureCapturingListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class PictureCapturingServiceImpl
/***
 * private constructor, meant to force the use of [.getInstance]  method
 */
private constructor(activity: Activity) : APictureCapturingService(activity) {
    private var cameraDevice: CameraDevice? = null
    private var imageReader: ImageReader? = null

    /***
     * camera ids queue.
     */
    private var cameraIds: Queue<String>? = null

    //    private String currentCameraId;
    private var cameraClosed = false

    /**
     * stores a sorted map of (pictureUrlOnDisk, PictureData).
     */
    private var picturesTaken: TreeMap<String, ByteArray>? = null
    private var capturingListener: PictureCapturingListener? = null

    /**
     * Starts pictures capturing treatment.
     *
     * @param listener picture capturing listener
     */
    override fun startCapturing(listener: PictureCapturingListener?) {
        picturesTaken = TreeMap()
        capturingListener = listener
        cameraIds = LinkedList()
        try {
            val cameraIds = manager!!.cameraIdList
            if (cameraIds.isNotEmpty()) {
                (this.cameraIds as LinkedList<String>).addAll(listOf(*cameraIds))
                //                this.currentCameraId = this.cameraIds.poll();
                openCamera()
            } else {
                //No camera detected!
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Exception occurred while accessing the list of cameras", e)
        }
    }

    private fun openCamera() {
        Log.d(TAG, "opening camera " + "1")
        try {
            if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)
                === PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                === PackageManager.PERMISSION_GRANTED
            ) {
                manager!!.openCamera("1", stateCallback, null)
            } else {
                manager!!.openCamera("1", stateCallback, null)
                Log.e(TAG, " exception occurred while opening camera " + "1")
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, " exception occurred while opening camera " + "1", e)
        }
    }

    private val captureListener: CameraCaptureSession.CaptureCallback =
        object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(
                @NonNull session: CameraCaptureSession, @NonNull request: CaptureRequest,
                @NonNull result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
                if (picturesTaken!!.lastEntry() != null) {
                    capturingListener!!.onCaptureDone(
                        picturesTaken!!.lastEntry().key,
                        picturesTaken!!.lastEntry().value
                    )
                    Log.i(TAG, "done taking picture from camera " + cameraDevice!!.id)
                }
                closeCamera()
            }

            override fun onCaptureProgressed(
                session: CameraCaptureSession,
                request: CaptureRequest,
                partialResult: CaptureResult
            ) {
                Log.i(TAG, "done taking picture from camera " + cameraDevice!!.id)
            }
        }
    private val onImageAvailableListener =
        ImageReader.OnImageAvailableListener { imReader: ImageReader ->
            val image = imReader.acquireLatestImage()
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.capacity())
            buffer[bytes]
            saveImageToDisk(bytes)
            image.close()
        }
    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(@NonNull camera: CameraDevice) {
            cameraClosed = false
            Log.d(TAG, "camera " + camera.id + " opened")
            cameraDevice = camera
            Log.i(TAG, "Taking picture from camera " + camera.id)
            //Take the picture after some delay. It may resolve getting a black dark photos.
//            new Handler().postDelayed(() -> {
            try {
                takePicture()
            } catch (e: CameraAccessException) {
                Log.e(TAG, " exception occurred while taking picture from " + "1", e)
            }
            //            }, 5000);
        }

        override fun onDisconnected(@NonNull camera: CameraDevice) {
            Log.d(TAG, " camera " + camera.id + " disconnected")
            if (cameraDevice != null && !cameraClosed) {
                cameraClosed = true
                cameraDevice!!.close()
            }
        }

        override fun onClosed(@NonNull camera: CameraDevice) {
            cameraClosed = true
            Log.d(TAG, "camera " + camera.id + " closed")
            //once the current camera has been closed, start taking another picture
//            if (!cameraIds!!.isEmpty()) {
////                takeAnotherPicture();
//            } else {
//                capturingListener.onDoneCapturingAllPhotos(picturesTaken)
//            }
        }

        override fun onError(@NonNull camera: CameraDevice, error: Int) {
            Log.e(
                TAG,
                "camera in error, int code $error"
            )
            if (cameraDevice != null && !cameraClosed) {
                cameraDevice!!.close()
            }
        }
    }

    @Throws(CameraAccessException::class)
    private fun takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null")
            return
        }
        val characteristics = manager!!.getCameraCharacteristics(
            cameraDevice!!.id
        )
        var jpegSizes: Array<Size>? = null
        val streamConfigurationMap =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        if (streamConfigurationMap != null) {
            jpegSizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG)
        }
        val jpegSizesNotEmpty = jpegSizes != null && 0 < jpegSizes.size
        val width = if (jpegSizesNotEmpty) jpegSizes!![0].width else 640
        val height = if (jpegSizesNotEmpty) jpegSizes!![0].height else 480
        val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
        val outputSurfaces: MutableList<Surface> = ArrayList()
        outputSurfaces.add(reader.surface)
        val captureBuilder =
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureBuilder.addTarget(reader.surface)
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        //        int rotation = getOrientation();
        val orientation = getJpegOrientation(characteristics, 0)
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, orientation)
        reader.setOnImageAvailableListener(onImageAvailableListener, null)
        cameraDevice!!.createCaptureSession(
            outputSurfaces,
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(@NonNull session: CameraCaptureSession) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, null)
                    } catch (e: CameraAccessException) {
                        Log.e(TAG, " exception occurred while accessing " + "1", e)
                    }
                }

                override fun onConfigureFailed(@NonNull session: CameraCaptureSession) {}
            },
            null)
    }

    private fun saveImageToDisk(bytes: ByteArray) {
        val cameraId = if (cameraDevice == null) UUID.randomUUID().toString() else cameraDevice!!.id
        val file =
            File(Environment.getExternalStorageDirectory().toString() + "/" + cameraId + "_pic.jpg")
        try {
            FileOutputStream(file).use { output ->
                output.write(bytes)
                picturesTaken!!.put(file.path, bytes)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Exception occurred while saving picture to external storage ", e)
        }
    }

    private fun takeAnotherPicture() {
//        this.currentCameraId = this.cameraIds.poll();
//        openCamera();
    }

    private fun closeCamera() {
        Log.d(TAG, "closing camera " + cameraDevice!!.id)
        if (null != cameraDevice && !cameraClosed) {
            cameraDevice!!.close()
            cameraDevice = null
        }
        if (null != imageReader) {
            imageReader!!.close()
            imageReader = null
        }
    }

    companion object {
        private val TAG = PictureCapturingServiceImpl::class.java.simpleName

        /**
         * @param activity the activity used to get the app's context and the display manager
         * @return a new instance
         */
        fun getInstance(activity: Activity): APictureCapturingService {
            return PictureCapturingServiceImpl(activity)
        }
    }
}