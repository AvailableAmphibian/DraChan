import commands.*
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.event.domain.guild.MemberJoinEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.discordjson.json.UserData
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import reaction_role.ReactionRolesTable
import reaction_role.handleReactionAddedEvent
import reaction_role.handleReactionRemovedEvent
import reactor.core.publisher.Mono
import commands.getMonster
import swarfarm_models.MonsterId
import commands.swSkills
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import java.sql.Connection

object Main {
    lateinit var owner: UserData
}

fun main(args: Array<String>) {
    val token = args[0]
    val client = DiscordClient.create(token)

    println("--- Verifying db ---")
    Database.connect(
        url = "jdbc:sqlite:drachan.sqlite",
        driver = "org.sqlite.JDBC",
        user = "root",
        password = ""
    )

    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    transaction {
        addLogger(StdOutSqlLogger)
        println("ReactionRole: {${ReactionRolesTable.selectAll().fetchSize}}")
    }

    println("--- Initiating monsterList ---")
    MonsterId.initMonsterList()

    println("--- Defining and retrieving discord relating things ---")
    val appId = Snowflake.asLong(client.applicationInfo.block()!!.id())

    /*Defining commands*/
    val newCommands = ArrayList<ApplicationCommandRequest>()
    newCommands.apply {
        //Ping command definition
        add(createCommand("ping", "Ping command."))
        //Help command definition
        add(createCommand("help", "Displays an help panel."))
        //Reaction Role command definition
        add(
            createCommand(
                "rr",
                "Creates a ReactionRole.",
                ApplicationCommandOptionData.builder().name("channel")
                    .description("The channel where the message is")
                    .type(ApplicationCommandOption.Type.CHANNEL.value).required(true).build(),
                ApplicationCommandOptionData.builder().name("message_id")
                    .description("The message's id. This is a long number (str)")
                    .type(ApplicationCommandOption.Type.STRING.value).required(true).build(),
                ApplicationCommandOptionData.builder().name("role").description("The role involved")
                    .type(ApplicationCommandOption.Type.ROLE.value).required(true).build(),
                ApplicationCommandOptionData.builder()
                    .name("rr_type")
                    .description("1 for a classic, 2 for a reversed, 3 for giving not retroactive, 4 for removing not retroactive.")
                    .type(ApplicationCommandOption.Type.INTEGER.value)
                    .required(false)
                    .build()
            )
        )
        //Swskills command definition
        add(
            createCommand(
                "swskills", "Displays skills of a monster.",
                ApplicationCommandOptionData.builder().name("name").description("The monster's name")
                    .type(ApplicationCommandOption.Type.STRING.value).required(true).build(),
                ApplicationCommandOptionData.builder().name("skill_number").description("A single skill")
                    .type(ApplicationCommandOption.Type.INTEGER.value).required(false).build()
            )
        )
        //Sw_monster command definition
        add(
            createCommand(
                "sw_monster", "Displays information about a monster.",
                ApplicationCommandOptionData.builder().name("name").description("The monster's name")
                    .type(ApplicationCommandOption.Type.STRING.value).required(true).build()
            )
        )
        //Bonk command definition
        add(
            createCommand(
                "bonk",
                "BONK!",
                ApplicationCommandOptionData.builder().name("bonk_them").description("The monster's name")
                    .type(ApplicationCommandOption.Type.USER.value).required(true).build()
            )
        )
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
            ClientPresence.online(ClientActivity.competing("on winning Dra's slimy heart ! ❤"))
        }.withGateway { gatewayDiscordClient ->
            println("~ Creating slashes ~")


            gatewayDiscordClient.restClient.applicationService.apply {
                val currentCommands = this.getGlobalApplicationCommands(appId).collectList().block()
                currentCommands?.forEach { deleteGlobalApplicationCommand(appId, it.id().toLong()) }

                bulkOverwriteGlobalApplicationCommand(
                    appId,
                    newCommands
                ).doOnError { it.printStackTrace() }.onErrorResume { Mono.empty() }.blockLast()
            }


            println("===== Connected =====")

            mono {
                Main.owner = gatewayDiscordClient.restClient.getMemberById(
                    Snowflake.of(854345025647017984),
                    Snowflake.of(286443116326682624)
                ).data.awaitSingle().user()

                launch {
                    gatewayDiscordClient.on(MemberJoinEvent::class.java)
                        .asFlow()
                        .collect {
                            println("""======= User joined ! =======
                                |Username : ${it.member.displayName}
                                |Guild : ${it.member.guild.awaitSingleOrNull()}
                            """.trimMargin())
                            onUserJoined(member = it.member)
                        }
                }

                launch {
                    gatewayDiscordClient.on(ReactionAddEvent::class.java)
                        .asFlow()
                        .collect {
                            if (Reaction.isCreatingReactionRole) {
                                println("==== Finishing RR ! ====")
                                finishReactionRoleCreation(it)
                            } else
                                handleReactionAddedEvent(it)
                        }
                }

                launch {
                    gatewayDiscordClient.on(ReactionRemoveEvent::class.java)
                        .asFlow()
                        .collect {
                            handleReactionRemovedEvent(it)
                        }
                }

                launch {
                    gatewayDiscordClient.on(ChatInputInteractionEvent::class.java)
                        .asFlow()
                        .collect {
                            try {
                                when (it.commandName) {
                                    "ping" -> it.reply("Pong\\").awaitSingleOrNull()
                                    "help" -> help(it)
                                    "rr" -> reactionRole(it)
                                    "swskills" -> swSkills(it)
                                    "sw_monster" -> getMonster(it)
                                    "bonk" -> bonk(it)
                                }
                                println("""Answered to :
                                    |${it.commandName}
                                    |${it.interaction.member.get().displayName } / ${it.interaction.member.get().nickname } #${it.interaction.member.get().discriminator}
                                """.trimMargin())
                            } catch (e: NoSuchElementException) {
                                it.reply ("Too much options provided, retry").awaitSingleOrNull()
                                e.printStackTrace()
                            }catch (e:Exception){
                                e.printStackTrace()
                            }
                        }
                }
            }
        }.block()

    println("==== Disconnected ====")
}
