package com.example

import com.google.gson.Gson
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
        val gson = Gson()
        webSocket("/chat"){
            val thisConnection: Connection?
            println("Adding user!")
            val username = call.request.header("username")
            thisConnection = Connection(this,username)
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
                    val chat = gson.fromJson(frame.readText(), Chat::class.java)
                    when (chat.type) {
                        "single" -> {
                            connections.forEach {
                                if (it.name == chat?.to) {
                                    it.session.send("[${thisConnection.name}]: ${chat.message}")
                                }
                            }
                        }
                        "all" -> {
                            connections.forEach{
                                if(it.name != thisConnection.name) {
                                    it.session.send("[${thisConnection.name}]: ${chat.message}")
                                }
                            }
                        }
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
