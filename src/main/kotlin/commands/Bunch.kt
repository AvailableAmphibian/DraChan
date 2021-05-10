package commands

import discord4j.core.`object`.entity.channel.MessageChannel
import kotlinx.coroutines.reactor.awaitSingle
import kotlin.system.measureTimeMillis

suspend fun bunch(channel:MessageChannel){
    println("Starting")
    val elapsed = measureTimeMillis {
        for(i in 1..10){
            channel.createMessage("Message n$i").awaitSingle()
        }
    }
    println("Finished in $elapsed")
}