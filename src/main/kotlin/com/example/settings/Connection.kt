package com.example.connection

import io.ktor.http.cio.websocket.*
import java.util.concurrent.atomic.AtomicInteger

class Connection(val session: DefaultWebSocketSession, username: String?){
    /*companion object{
        var lastId = AtomicInteger(0)
    }*/
    val name = username
}