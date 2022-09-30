package com.example.ulaugh.utils
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import java.io.File


object DecodeImage {

    private fun getRealPathFromURI(contentURI: Uri, context: Context): String? {
//        val contentUri = Uri.parse(contentURI)
        val cursor: Cursor? = context.contentResolver.query(contentURI, null, null, null, null)
        return if (cursor == null) {
            contentURI.path
        } else {
            cursor.moveToFirst()
            val index: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(index)
        }
    }

    suspend fun compressImage(imageUri:Uri, context: Context):Uri{
        val imageUrl = getRealPathFromURI(imageUri, context)
        val compressedImageFile:File?
        val file = File(imageUrl!!)
        compressedImageFile = if (file.length() > 550000) {
            Compressor.compress(context, file) {
                resolution(400, 350)
                quality(100)
                format(Bitmap.CompressFormat.PNG)
    //                        size(2_097_152) // 2 MB
            }
        } else
            file
        return Uri.fromFile(compressedImageFile)
    }
}