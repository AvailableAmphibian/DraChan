import commands.*
import discord4j.core.DiscordClient
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono

@InternalCoroutinesApi
fun main(args: Array<String>) {
    val token = args[0]
    val client = DiscordClient.create(token)

    client.gateway()
        .setInitialPresence {
            Presence.online(Activity.watching("Albedra programming me."))
        }.withGateway {
            mono {
                it.on(MessageCreateEvent::class.java)
                    .asFlow()
                    .collect {
                        println(it.message.content)
                        if (it.message.content == "<@!827605045485764650>") {
                            help(message = it.message)
                        }
                        val message = it.message.content.substringBefore(" ")
                        val end = it.message.content.substringAfter(" ")
                        when (message) {
                            "?help" -> help(message = it.message)
                            "?ping" -> ping(message = it.message)
                            "?embed" -> embed(event = it, end = end)
                            "?bunch" -> bunch(channel = it.message.channel.awaitSingle())
                            "?copy" -> copyMessage(message = it.message)
                            "?melody" -> callMelody(channel = it.message.channel.awaitSingle())
                        }
                    }

            }
        }
        .block()
}


