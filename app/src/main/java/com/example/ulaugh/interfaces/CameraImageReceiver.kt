package com.example.ulaugh.interfaces

import android.graphics.Bitmap
import android.media.Image

interface CameraImageReceiver {
    fun onImageReceived(image: ArrayList<Bitmap>)
}