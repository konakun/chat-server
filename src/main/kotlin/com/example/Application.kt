package com.example

import com.example.settings.*
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
        val connections = Collections.synchronizedSet<PublicChatConnection?>(LinkedHashSet())
        val connectionsPrivate = Collections.synchronizedSet<PrivateChatConnection?>(LinkedHashSet())
        val gson = Gson()
        webSocket("/chat/{room}/{username}"){
            val thisConnection: PublicChatConnection?
            println("Adding user!")
            val name = call.parameters["username"]
            val user = UserData(name, null)
            val room = call.parameters["room"]?.toInt()

            thisConnection = PublicChatConnection(this, user, room)
            connections += thisConnection
            try{
                if(user.userName == null){
                    send("No Username detected, connection can't continue")
                    throw Exception("noUser")
                }
                else if (room == null){
                    send("No room detected, connection can't continue")
                    throw Exception("noRoom")
                }
                send("You are connected as ${thisConnection.name} in room '${thisConnection.chatRoom}', "
                        + "your personal code now is: ${thisConnection.userCode}")
                connections.forEach{
                    if(it.name != thisConnection.name && it.chatRoom == thisConnection.chatRoom ){
                        it.session.send("User ${thisConnection.name}#${thisConnection.userCode} has connected")
                    }
                }
                for(frame in incoming){
                    frame as? Frame.Text ?: continue
                    val chat = gson.fromJson(frame.readText(), Chat::class.java)
                    connections.forEach{
                        if(it.name != thisConnection.name && it.chatRoom == thisConnection.chatRoom) {
                            it.session.send("[${thisConnection.name}]: ${chat.message}")
                        }
                    }
                }
            } catch (e: Exception){
                println(e.localizedMessage)
            } finally {
                println("User ${thisConnection.name}")
                connections.forEach{
                    if(thisConnection.chatRoom == it.chatRoom) {
                        it.session.send("User ${thisConnection.name} has disconnected")
                    }
                }
                connections -= thisConnection
            }
        }
        webSocket("/private/{usercode}/{personalcode}/{username}"){
            val thisConnection: PrivateChatConnection?
            val name = call.parameters["username"]
            val personalCode = call.parameters["personalCode"]?.toInt()
            val userCode = call.parameters["userCode"]?.toInt()
            val user = UserData(name, personalCode)

            thisConnection = PrivateChatConnection(this, user, userCode)
            connectionsPrivate += thisConnection
            connectionsPrivate.forEach(){
                println(it.userCode)
            }
            try{
                if(user.userName == null){
                    send("No Username detected, connection can't continue")
                    throw Exception("noUser")
                }
                else if (userCode == null){
                    send("No usercode detected, connection can't continue")
                    throw Exception("noRecipent")
                }
                for(frame in incoming){
                    frame as? Frame.Text ?: continue
                    val chat = gson.fromJson(frame.readText(), Chat::class.java)
                    connectionsPrivate.forEach{
                        if(it.userCode == userCode) {
                            it.session.send("[${thisConnection.name}]: ${chat.message}")
                            println(thisConnection.userCode.toString() + " to " + it.userCode.toString())
                        }
                    }
                }
            } catch (e: Exception){
                println(e.localizedMessage)
            }
        }
    }
}
