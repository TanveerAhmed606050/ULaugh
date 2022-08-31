package com.example.ulaugh.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.ulaugh.utils.Constants.EMAIL
import com.example.ulaugh.utils.Constants.PREFS_TOKEN_FILE
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharePref @Inject constructor(@ApplicationContext context: Context){

    private var prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_TOKEN_FILE, Context.MODE_PRIVATE)

//    fun saveEmail(email: String) {
//        val editor = prefs.edit()
//        editor.putString(EMAIL, email)
//        editor.apply()
//    }

    fun writeBoolean(context: Context?, key: String?, value: Boolean) {
        prefs.edit().putBoolean(key, value).commit()
    }

    fun readBoolean(
        context: Context?, key: String?,
        defValue: Boolean
    ): Boolean {

        return prefs.getBoolean(key, defValue)
    }

    fun writeInteger(key: String?, value: Int) {
        prefs.edit().putInt(key, value).commit()
    }

    fun readInteger(key: String?, defValue: Int): Int {
        return prefs.getInt(key, defValue)
    }

    fun writeString(key: String?, value: String?) {
        prefs.edit().putString(key, value).commit()
    }

    fun readString(key: String?, defValue: String?): String? {
        return prefs.getString(key, defValue)
    }

//    fun getEmail(): String? {
//        return prefs.getString(EMAIL, null)
//    }
//
//    fun saveUserName(name:String){
//        prefs.edit().putString(USER_NAME, name).apply()
//    }
//    fun getUserName():String?{
//        return prefs.getString(USER_NAME, null)
//    }
//    fun saveFullName(fullName:String){
//        prefs.edit().putString(FULL_NAME, fullName).apply()
//    }
//    fun getFullName():String?{
//        return prefs.getString(FULL_NAME, null)
//    }
//    fun savePhone(phone:String){
//        prefs.edit().putString(PHONE_NO, phone).apply()
//    }
//    fun getPhone():String?{
//        return prefs.getString(PHONE_NO, null)
//    }
//    fun saveImage(image:String){
//        prefs.edit().putString(IMAGE, image).apply()
//    }
//    fun getImage():String?{
//        return prefs.getString(IMAGE, null)
//    }
//
//    fun saveFirebaseId(userId: String?) {
//        prefs.edit().putString(FIREBASE_ID, userId).apply()
//    }
//
//    fun getFirebaseId(): String? {
//        return prefs.getString(FIREBASE_ID, null)
//    }
}