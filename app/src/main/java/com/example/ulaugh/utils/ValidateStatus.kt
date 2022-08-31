package com.example.ulaugh.utils

import android.text.TextUtils
import kotlin.math.log

class ValidateStatus {

    fun validateCredentials(
        phoneNo: String, userName: String, fullName: String, email: String,
        isLogin: Boolean
    ): Pair<Boolean, String> {

        var result = Pair(true, "")
        if ((isLogin && TextUtils.isEmpty(phoneNo)) || (!isLogin && TextUtils.isEmpty(fullName)) || (!isLogin && TextUtils.isEmpty(
                userName
            )) || (!isLogin && !Helper.isValidEmail(email))
        ) {
            result = Pair(false, "Please provide the credentials")
        }
//        else if (!Helper.isValidEmail(email)) {
//            result = Pair(false, "Email is invalid") }
        else if (isLogin && phoneNo.length <= 5) {
            result = Pair(false, "Phone number is invalid")
        }


//        else if (!TextUtils.isEmpty(password) && password.length <= 5) {
//            result = Pair(false, "Password length should be greater than 5")
//        }
        return result
    }
}