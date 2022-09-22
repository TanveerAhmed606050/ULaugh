package com.example.ulaugh.interfaces

import java.util.*

interface PictureCapturingListener {
    /**
     * a callback called when we've done taking a picture from a single camera
     * (use this method if you don't want to wait for ALL taken pictures to be ready @see onDoneCapturingAllPhotos)
     *
     * @param pictureUrl  taken picture's location on the device
     * @param pictureData taken picture's data as a byte array
     */
    fun onCaptureDone(pictureUrl: String?, pictureData: ByteArray?)

}