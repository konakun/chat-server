package com.example.settings

import io.ktor.http.cio.websocket.*
import java.util.concurrent.atomic.AtomicInteger

class PublicChatConnection(val session: DefaultWebSocketSession, user:UserData, room: Int?){
    val name = user.userName
    val userCode = user.userCode
    val chatRoom = room
}

class PrivateChatConnection(val session: DefaultWebSocketSession, user:UserData, userCode: Int?){
    val name = user.userName
    val userCode = user.userCode
    val sendTo = userCode
}