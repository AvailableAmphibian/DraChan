package commands

import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.awaitSingle

suspend fun ping(message: Message) {
    val channel = message.channel.awaitSingle()
    channel.createMessage("Pong?").awaitSingle()
}
