package com.example.ulaugh.utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

class Helper {
    companion object {
        fun isValidEmail(email: String): Boolean {
            return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun hideKeyboard(view: View) {
            try {
                val imm =
                    view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            } catch (e: Exception) {

            }
        }

        fun prettyCount(count: Int): String {
            val array = arrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
            val value = floor(log10(count.toDouble())).toInt()
            val base = value / 3
            return if (value >= 3 && base < array.size) {
                DecimalFormat("#0.0").format(count / 10.0.pow((base * 3).toDouble())) + array[base]
            } else {
                DecimalFormat("#,##0").format(count)
            }
        }

        fun convertToLocal(time: String, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): String {
            val dateFormat = "yyyy/MM/dd HH:mm:ss"
            val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
            parser.timeZone = timeZone
            val timeDate = parser.parse(time)
            val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
            return formatter.format(timeDate!!)
        }

        fun covertTimeToText(dataDate: String?): String? {
            var convTime: String? = null
            val prefix = ""
            val suffix = "ago"
            try {
                val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                val pasTime: Date = dateFormat.parse(dataDate)
                val nowTime = Date()
                val dateDiff: Long = nowTime.getTime() - pasTime.getTime()
                val second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
                val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
                val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
                val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)
                if (second < 60) {
                    convTime = "$second sec $suffix"
                } else if (minute < 60) {
                    convTime = "$minute min $suffix"
                } else if (hour < 24) {
                    convTime = "$hour hours $suffix"
                } else if (day >= 7) {
                    convTime = if (day > 360) {
                        (day / 360).toString() + " Years " + suffix
                    } else if (day > 30) {
                        (day / 30).toString() + " Mon " + suffix
                    } else {
                        (day / 7).toString() + " Week " + suffix
                    }
                } else if (day < 7) {
                    convTime = "$day Days $suffix"
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                Log.e("ConvTimeE", e.message!!)
            }
            return convTime
        }
    }

    fun localToGMT(): String {
        val date = Date()
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
//        Log.d("kdhgakds", "localToGMT: ${sdf.format(date)}")
        return sdf.format(date)
    }

}