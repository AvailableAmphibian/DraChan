import commands.*
import discord4j.core.DiscordClient
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.event.domain.guild.MemberJoinEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import reaction_role.ReactionRoleTable
import reaction_role.giveRoleOnReaction
import reaction_role.removeRoleOnReaction
import java.sql.Connection


fun main(args: Array<String>) {
    val token = args[0]
    val client = DiscordClient.create(token)

    Database.connect(
        url = "jdbc:sqlite:drachan.sqlite",
        driver = "org.sqlite.JDBC",
        user = "root",
        password = ""
    )

    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    transaction {
        addLogger(StdOutSqlLogger)
        println("ReactionRole: ${ReactionRoleTable.selectAll()}")
    }

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
            Presence.online(Activity.watching("Dra  programming me."))
        }.withGateway {
            println("===== Connected =====")
            mono {
                launch {
                    it.on(MessageCreateEvent::class.java)
                        .asFlow()
                        .collect {
                            println(it.message.content)
                            if (it.message.content == "<@843037701773983786>") {
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
                                "?reactionRole" -> reactionRole(message = it.message)
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

                launch {
                    it.on(ReactionAddEvent::class.java)
                        .asFlow()
                        .collect {
                            if (Reaction.isCreatingReactionRole) {
                                println("==== Finishing RR ! ====")
                                finishReactionRoleCreation(it)
                            } else
                                giveRoleOnReaction(it)
                        }
                }

                launch {
                    it.on(ReactionRemoveEvent::class.java)
                        .asFlow()
                        .collect {
                            removeRoleOnReaction(it)
                        }
                }
            }
        }.block()
    println("==== Disconnected ====")
}
