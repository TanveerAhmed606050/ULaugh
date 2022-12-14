package com.example.ulaugh.utils

import java.util.HashMap

class FirebaseDataBaseHashmapFunctionClass {

    fun inboxConversationHashmapFun(
        messageId: String,
        otherFireBaseId: String,
        otherFirebaseName: String,
        dateAndTime: String,
        message: String,
        messageType: String,
        pic: String,
        messageToken:String,
        seenMsg:Boolean
    ): HashMap<String, Any> {
        val inboxHashmap = HashMap<String, Any>()
        inboxHashmap["conversation_id"] = messageId
        inboxHashmap["other_user_firebase_id"] = otherFireBaseId
        inboxHashmap["other_user_firebase_name"] = otherFirebaseName
        inboxHashmap[Constants.IS_SEEN] = seenMsg
        inboxHashmap["latest_message"] = latestMessageHashmapFun(dateAndTime, message, messageType)
        inboxHashmap[Constants.PROFILE_PIC] = pic
        inboxHashmap["message_token"] = messageToken
        return inboxHashmap
    }

    fun messageHashmapFun(
        messageId: String,
        messageType: String,
        contentMessage: String,
        dateAndTime: String,
        senderFirebaseId: String,
        senderName: String,
        receiverFirebaseId: String,
        receiverName: String,
        isSeen:Boolean
    ): HashMap<String, Any> {

        val messageHashMap = HashMap<String, Any>()
        messageHashMap["message_id"] = messageId
        messageHashMap["type"] = messageType
        messageHashMap[Constants.IS_SEEN] = isSeen
        messageHashMap["content_message"] = contentMessage
        messageHashMap["date"] = dateAndTime
        messageHashMap["sender_firebase_id"] = senderFirebaseId
        messageHashMap["sender_user_name"] = senderName
        messageHashMap["receiver_firebase_id"] = receiverFirebaseId
        messageHashMap["receiver_user_name"] = receiverName

        return messageHashMap
    }

    private fun latestMessageHashmapFun(
        dateAndTime: String,
        message: String,
        messageType: String
    ): HashMap<String, Any> {

        val latestMessageHashmap = HashMap<String, Any>()
        latestMessageHashmap["date"] = dateAndTime
        latestMessageHashmap["message"] = message
        latestMessageHashmap["type"] = messageType

        return latestMessageHashmap
    }


}