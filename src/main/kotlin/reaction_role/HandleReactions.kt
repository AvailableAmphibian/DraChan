package reaction_role

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import kotlinx.coroutines.reactor.awaitSingle
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

data class HandleReactions(
    val guildId: Snowflake,
    val messageId: Snowflake,
    val member: Member,
    val emoji: String
)

suspend fun removeRoleOnReaction(removeEvent: ReactionRemoveEvent) {
    val handle = HandleReactions(
        guildId = removeEvent.guildId.get(),
        messageId = removeEvent.messageId,
        member = removeEvent.guild.awaitSingle().getMemberById(removeEvent.userId).awaitSingle(),
        emoji = when (val emoji = removeEvent.emoji) {
            is ReactionEmoji.Unicode -> emoji.raw
            is ReactionEmoji.Custom -> emoji.id.asString()
            else -> ""
        }
    )

    try {
        val reactionRole = getReactionRole(handle) ?: return

        val roleSnowflake = Snowflake.of(reactionRole.roleId)
        if (handle.member.roleIds.contains(roleSnowflake))
            handle.member.removeRole(roleSnowflake).awaitSingle()
        println("==== Role ${roleSnowflake.asString()} removed from ${handle.member.displayName} ====")
    } catch (e: Exception) {
        println("==== No role to remove ====")
    }
}

suspend fun giveRoleOnReaction(reactionAddEvent: ReactionAddEvent) {
    val handle = HandleReactions(
        guildId = reactionAddEvent.guildId.get(),
        messageId = reactionAddEvent.messageId,
        member = reactionAddEvent.guild.awaitSingle().getMemberById(reactionAddEvent.userId).awaitSingle(),
        emoji = when (val emoji = reactionAddEvent.emoji) {
            is ReactionEmoji.Unicode -> emoji.raw
            is ReactionEmoji.Custom -> emoji.id.asString()
            else -> ""
        }
    )


    try {
        val reactionRole = getReactionRole(handle)?: return

        val roleSnowflake = Snowflake.of(reactionRole.roleId)

        if (!handle.member.roleIds.contains(roleSnowflake))
            handle.member.addRole(roleSnowflake).awaitSingle()
        println("==== Role ${roleSnowflake.asString()} given to ${handle.member.displayName} ====")
    } catch (e: Exception) {
        println("==== No role ====")
    }
}

fun getReactionRole(handle: HandleReactions): ReactionRole? {
    try {
        return transaction {
            ReactionRole.find {
                ReactionRoleTable.guildId.eq(handle.guildId.asLong())
                    .and(ReactionRoleTable.messageId.eq(handle.messageId.asLong()))
                    .and(ReactionRoleTable.emoji.eq(handle.emoji))
            }.first()
        }
    }catch(e:Exception){
        e.printStackTrace()
    }
    return null
}

