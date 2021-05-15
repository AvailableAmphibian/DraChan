import commands.*
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.event.domain.guild.MemberJoinEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono

@InternalCoroutinesApi
fun main(args: Array<String>) {
    val token = args[0]
    val client = DiscordClient.create("ODQzMDM3NzAxNzczOTgzNzg2.YJ-CCg.9n8bWu9dfvJoSamxTAOdRotRQSg")

    client.gateway()
        .setEnabledIntents(
            IntentSet.of(
                Intent.GUILDS,
                Intent.GUILD_MEMBERS,
                Intent.GUILD_VOICE_STATES,
                Intent.GUILD_MESSAGE_REACTIONS,
                Intent.GUILD_MESSAGES,
                Intent.DIRECT_MESSAGES
            )
        )
        .setInitialPresence {
            Presence.online(Activity.watching("Albedra programming me."))
        }.withGateway {
            mono {
                launch {
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
                                "?copy" -> copyMessage(message = it.message)
                                "?melody" -> callMelody(channel = it.message.channel.awaitSingle())
                                "?dice" -> dice(message = it.message)
                            }
                        }
                }

                launch {
                    it.on(MemberJoinEvent::class.java)
                        .asFlow()
                        .collect {
                            println("======= User joined ! =======")
                            onUserJoined(member = it.member)
                        }
                }
            }
        }.block()
}


