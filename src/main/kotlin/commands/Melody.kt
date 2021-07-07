package commands

import discord4j.core.`object`.entity.channel.MessageChannel
import kotlinx.coroutines.reactor.awaitSingle

suspend fun callMelody(channel: MessageChannel) {
    channel.createMessage {
        it.setContent("Let her sleep scum.")
    }.awaitSingle()
}