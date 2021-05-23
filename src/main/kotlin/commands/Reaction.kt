package commands

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.rest.util.Color
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import kotlinx.coroutines.reactor.awaitSingle
import org.jetbrains.exposed.sql.transactions.transaction
import reaction_role.ReactionRole

object Reaction {
    var isCreatingReactionRole = false
    lateinit var createRRMessage: Message
    lateinit var message: Message
    lateinit var role: String
}

suspend fun reactionRole(message: Message) {
    val permissions = message.authorAsMember.awaitSingle().basePermissions.awaitSingle()
    val channel = message.channel.awaitSingle()

    if (message.guildId.isEmpty ||
        !permissions.containsAll(PermissionSet.of(Permission.MANAGE_CHANNELS, Permission.MANAGE_ROLES))
    ) {
        channel.createMessage {
            it.setContent("Sorry you don't have the required permissions mate")
            it.setMessageReference(message.id)
        }.awaitSingle()
        return
    }
    //TODO Remove reaction role
    val content = message.content.split(" ")
    if (content.size != 4) {
        channel.createMessage {
            val start = if (content.size > 4) "Too many arguments" else "Not enough arguments"
            it.setContent("$start, please provide the message with the following pattern :")
                .setEmbed { spec ->
                    spec.setDescription("?reaction <channelId> <messageId> <@role> <reaction>")
                        .setColor(Color.RED)
                }
                .setMessageReference(message.id)
        }.awaitSingle()
        return
    }

    val channelId = content[1]
    val messageId = content[2]
    val role = content[3]

    try {
        Reaction.isCreatingReactionRole = true
        Reaction.message = message.client.getMessageById(Snowflake.of(channelId), Snowflake.of(messageId)).awaitSingle()

        Reaction.role = role

        Reaction.createRRMessage =
            channel.createMessage("Please react to this message with the emoji you want !").awaitSingle()

    } catch (e: NumberFormatException) {
        channel.createMessage("The message doesn't follow the good pattern, please use the following : `?reaction <channelId> <messageId> <@role> <reaction>`").awaitSingle()

        e.printStackTrace()
        Reaction.isCreatingReactionRole = false
    }
}


suspend fun finishReactionRoleCreation(reactionAddEvent: ReactionAddEvent) {
    Reaction.isCreatingReactionRole = false

    val emoji = reactionAddEvent.emoji
    val guild = reactionAddEvent.guildId.get()
    val rrMessageId = Reaction.message.id
    val role = Reaction.role.substringAfter("<@&").substringBefore('>')

    val emojiAsString = when (emoji) {
        is ReactionEmoji.Custom -> emoji.id.asString()
        is ReactionEmoji.Unicode -> emoji.raw
        else -> ""
    }
    try {
        transaction {
            ReactionRole.new {
                reactionRoleId = hashCode()
                guildId = guild.asLong()
                messageId = rrMessageId.asLong()
                roleId = role.toLong()
                reaction = emojiAsString
            }

        }
        Reaction.message.addReaction(emoji).awaitSingle()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}