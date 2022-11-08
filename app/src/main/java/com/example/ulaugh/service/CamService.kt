package com.example.ulaugh.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.util.Size
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.view.ContentInfoCompat
import com.example.ulaugh.R
import com.example.ulaugh.controller.CameraActivity
import com.example.ulaugh.interfaces.CameraImageReceiver
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

/**
 * Copyright (c) 2019 by Roman Sisik. All rights reserved.
 */
class CamService : Service() {

    // UI
    private var wm: WindowManager? = null
    private var rootView: View? = null
    private var textureView: TextureView? = null
    var count = 0
    val imageArray: ArrayList<Bitmap> = ArrayList()

    // Camera2-related stuff
    private var cameraManager: CameraManager? = null
    private var previewSize: Size? = null
    private var cameraDevice: CameraDevice? = null
    private var captureRequest: CaptureRequest? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var picturesTaken: TreeMap<String, ByteArray>? = null

    // You can start service in 2 modes - 1.) with preview 2.) without preview (only bg processing)
    private var shouldShowPreview = true

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
        }
    }

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            initCam(width, height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}
    }

    private val imageListener = ImageReader.OnImageAvailableListener { reader ->
        val image = reader?.acquireLatestImage()

        Log.d(TAG, "Got image: " + image?.width + " x " + image?.height)
        count++
        val buffer = image!!.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
//        buffer[bytes]
//        buffer.get(bytes)
        if (count > 2) {
            saveImageToDisk(bytes)
//            val bitmap = byteToBitmap(bytes)
//            imageArray.add(bitmap)
        }
//        val intent = Intent()
//        intent.putExtra("sdaglsd", bytes)
        if (count == 5) {
//            sendBroadcast(Intent("intent"))
            mImageReceiver!!.onImageReceived(imageArray)
            stopCamera()
        }

        // Process image here..ideally async so that you don't block the callback
        // ..
        image!!.close()
    }

    private fun byteToBitmap(pictureData: ByteArray): Bitmap {
        try {
        } catch (e: IOException) {
        }
        val bmp = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.size)
        return bmp
    }

    private fun saveImageToDisk(bytes: ByteArray) {

        val cw = ContextWrapper(mContext)
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        val file = File(directory, "picture.jpg")
        try {
            val myBitmap = BitmapFactory.decodeFile(file.absolutePath)
            imageArray.add(myBitmap)
//            picturesTaken!![file.path] = bytes
//            }
        } catch (e: IOException) {
        }
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(currentCameraDevice: CameraDevice) {
            cameraDevice = currentCameraDevice
            createCaptureSession()
            Log.d(TAG, "onOpen")
        }

        override fun onDisconnected(currentCameraDevice: CameraDevice) {
            currentCameraDevice.close()
            cameraDevice = null
            Log.d(TAG, "onDisconnect")
        }

        override fun onError(currentCameraDevice: CameraDevice, error: Int) {
            currentCameraDevice.close()
            cameraDevice = null
            Log.d(TAG, "onError")
        }
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        picturesTaken = TreeMap()

        when (intent?.action) {
            ACTION_START -> start()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        startForeground()
        Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()

        stopCamera()
        Log.d(TAG, "onDestroy")

        if (rootView != null)
            wm?.removeView(rootView)

        sendBroadcast(Intent(ACTION_STOPPED))
    }

    private fun start() {
        shouldShowPreview = false
        initCam(320, 200)
        Log.d(TAG, "onstart")
    }

    private fun initCam(width: Int, height: Int) {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var camId: String? = null

        for (id in cameraManager!!.cameraIdList) {
            val characteristics = cameraManager!!.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                camId = id
                break
            }
        }

        previewSize = chooseSupportedSize(camId!!, width, height)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        cameraManager!!.openCamera(camId, stateCallback, null)
    }

    private fun chooseSupportedSize(
        camId: String,
        textureViewWidth: Int,
        textureViewHeight: Int
    ): Size {

        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // Get all supported sizes for TextureView
        val characteristics = manager.getCameraCharacteristics(camId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val supportedSizes = map!!.getOutputSizes(SurfaceTexture::class.java)

        // We want to find something near the size of our TextureView
        val texViewArea = textureViewWidth * textureViewHeight
        val texViewAspect = textureViewWidth.toFloat() / textureViewHeight.toFloat()

        val nearestToFurthestSz = supportedSizes.sortedWith(compareBy(
            // First find something with similar aspect
            {
                val aspect = if (it.width < it.height) it.width.toFloat() / it.height.toFloat()
                else it.height.toFloat() / it.width.toFloat()
                (aspect - texViewAspect).absoluteValue
            },
            // Also try to get similar resolution
            {
                (texViewArea - it.width * it.height).absoluteValue
            }
        ))


        if (nearestToFurthestSz.isNotEmpty())
            return nearestToFurthestSz[0]

        return Size(320, 200)
    }

    private fun startForeground() {

//        val pendingIntent: PendingIntent =
//            Intent(this, CameraActivity::class.java).let { notificationIntent ->
//                PendingIntent.getActivity(this, 0, notificationIntent, 1)
//            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE)
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

//        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle(getText(R.string.app_name))
//            .setContentText(getText(R.string.app_name))
//            .setSmallIcon(R.drawable.notification_template_icon_bg)
//            .setContentIntent(pendingIntent)
//            .setTicker(getText(R.string.app_name))
//            .build()
//
//        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    private fun createCaptureSession() {
        try {
            // Prepare surfaces we want to use in capture session
            val targetSurfaces = ArrayList<Surface>()

            // Prepare CaptureRequest that can be used with CameraCaptureSession
            val requestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {

                    if (shouldShowPreview) {
                        val texture = textureView!!.surfaceTexture!!
                        texture.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)
                        val previewSurface = Surface(texture)

                        targetSurfaces.add(previewSurface)
                        addTarget(previewSurface)
                    }

                    // Configure target surface for background processing (ImageReader)
                    imageReader = ImageReader.newInstance(
                        previewSize!!.getWidth(), previewSize!!.getHeight(),
                        ImageFormat.YUV_420_888, 2
                    )
                    imageReader!!.setOnImageAvailableListener(imageListener, null)

                    targetSurfaces.add(imageReader!!.surface)
                    addTarget(imageReader!!.surface)

                    // Set some additional parameters for the request
                    set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    )
                    set(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                    )
                }

            // Prepare CameraCaptureSession
            cameraDevice!!.createCaptureSession(
                targetSurfaces,
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // The camera is already closed
                        if (null == cameraDevice) {
                            return
                        }

                        captureSession = cameraCaptureSession
                        try {
                            // Now we can start capturing
                            captureRequest = requestBuilder!!.build()
                            captureSession!!.setRepeatingRequest(
                                captureRequest!!,
                                captureCallback,
                                null
                            )

                        } catch (e: CameraAccessException) {
                            Log.e(TAG, "createCaptureSession", e)
                        }

                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        Log.e(TAG, "createCaptureSession()")
                    }
                }, null
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "createCaptureSession", e)
        }
    }

    private fun stopCamera() {
        try {
            captureSession?.close()
            captureSession = null

            cameraDevice?.close()
            cameraDevice = null

            imageReader?.close()
            imageReader = null

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private var mImageReceiver: CameraImageReceiver? = null
        var mContext: Context? = null
        val TAG = "CamService1234"
        val ACTION_START = "eu.sisik.backgroundcam.action.START"
        val ACTION_START_WITH_PREVIEW = "eu.sisik.backgroundcam.action.START_WITH_PREVIEW"
        val ACTION_STOPPED = "eu.sisik.backgroundcam.action.STOPPED"
        fun getInstance(receivedData: CameraImageReceiver, context: Context) {
            mImageReceiver = receivedData
            mContext = context
        }

        val ONGOING_NOTIFICATION_ID = 6660
        val CHANNEL_ID = "cam_service_channel_id"
        val CHANNEL_NAME = "cam_service_channel_name"

    }
}