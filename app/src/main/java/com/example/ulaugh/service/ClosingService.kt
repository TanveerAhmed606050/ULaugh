package com.example.ulaugh.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.ulaugh.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ClosingService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        onlineUser(true)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Destroy the service
        onlineUser(false)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        onlineUser(false)
        stopSelf()
    }

    private fun onlineUser(isOnline: Boolean) {
        val onlineStatusHashMap = HashMap<String, Any>()
        onlineStatusHashMap[Constants.IS_ONLINE] = isOnline
        val onLineFbRef: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child(Constants.USERS_REF).child(FirebaseAuth.getInstance().currentUser!!.uid)
        onLineFbRef.updateChildren(onlineStatusHashMap)
    //        if (isOnline) {
//            onLineFbRef.updateChildren(onlineStatusHashMap)
//        }
//        else {
//            onLineFbRef.child(FirebaseAuth.getInstance().currentUser!!.uid).removeValue()
//        }
    }
}