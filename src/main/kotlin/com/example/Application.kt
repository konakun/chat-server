package com.example

import io.ktor.http.cio.websocket.*
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.websocket.*
import io.ktor.routing.*
import java.util.*
import kotlin.collections.LinkedHashSet

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    install(WebSockets)

    routing{
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/chat"){
            val username = call.request.header("username")
            println("Adding user!")
            val thisConnection = Connection(this, username)
            connections += thisConnection
            try{
                send("You are connected as [${thisConnection.name}]")
                connections.forEach{
                    if(it.name != thisConnection.name){
                        it.session.send("User [${thisConnection.name}] has connected")
                    }
                }
                for(frame in incoming){
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    val textWithUsername = "[${thisConnection.name}]: $receivedText"
                    connections.forEach{
                        it.session.send(textWithUsername)
                    }
                }
            } catch (e: Exception){
                println(e.localizedMessage)
            } finally {
                println("User ${thisConnection.name}")
                connections.forEach{
                    it.session.send("User ${thisConnection.name} has disconnected")
                }
                connections -= thisConnection
            }
        }
    }
}
