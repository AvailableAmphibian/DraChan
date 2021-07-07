package reaction_role

import REACTION_ROLE_GIVE
import REACTION_ROLE_GIVE_NOT_RETROACTIVE
import REACTION_ROLE_REMOVE
import REACTION_ROLE_REMOVE_NOT_RETROACTIVE
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

private data class HandleReactions(
    val guildId: Snowflake,
    val messageId: Snowflake,
    val member: Member,
    val emoji: String,
)

suspend fun handleReactionRemovedEvent(removeEvent: ReactionRemoveEvent) {
    handleEvent(
        HandleReactions(
            guildId = removeEvent.guildId.get(),
            messageId = removeEvent.messageId,
            member = removeEvent.guild.awaitSingle().getMemberById(removeEvent.userId).awaitSingle(),
            emoji = when (val emoji = removeEvent.emoji) {
                is ReactionEmoji.Unicode -> emoji.raw
                is ReactionEmoji.Custom -> emoji.id.asString()
                else -> ""
            }
        )
    )
}


suspend fun handleReactionAddedEvent(reactionAddEvent: ReactionAddEvent) {
    handleEvent(
        HandleReactions(
            guildId = reactionAddEvent.guildId.get(),
            messageId = reactionAddEvent.messageId,
            member = reactionAddEvent.guild.awaitSingle().getMemberById(reactionAddEvent.userId).awaitSingle(),
            emoji = when (val emoji = reactionAddEvent.emoji) {
                is ReactionEmoji.Unicode -> emoji.raw
                is ReactionEmoji.Custom -> emoji.id.asString()
                else -> ""
            }
        )
    )
}

private suspend fun handleEvent(handle: HandleReactions) {
    val rr = getReactionRole(handle) ?: return

    rr.forEach {
        val roleSnowflake = Snowflake.of(it.roleId)
        val member = handle.member

        when (it.rrType) {
            REACTION_ROLE_GIVE, REACTION_ROLE_REMOVE -> giveOrRemoveRole(roleSnowflake, member)
            REACTION_ROLE_GIVE_NOT_RETROACTIVE -> give(roleSnowflake, member)
            REACTION_ROLE_REMOVE_NOT_RETROACTIVE -> remove(roleSnowflake, member)
        }
    }
}

private fun getReactionRole(handle: HandleReactions): List<ReactionRoles>? {
    try {
        return transaction {
            ReactionRoles.find {
                ReactionRolesTable.guildId.eq(handle.guildId.asLong())
                    .and(ReactionRolesTable.messageId.eq(handle.messageId.asLong()))
                    .and(ReactionRolesTable.emoji.eq(handle.emoji))
            }.copy().toList()
        }
    } catch (e: Exception) {
        println("Can't retrieve a role")
    }
    return null
}

private suspend fun giveOrRemoveRole(roleId: Snowflake, member: Member) {
    val roleCommand = if (!member.roleIds.contains(roleId)) member.addRole(roleId) else member.removeRole(roleId)
    roleCommand.awaitSingleOrNull()
}

private suspend fun give(roleId: Snowflake, member: Member) = member.addRole(roleId).awaitSingleOrNull()
private suspend fun remove(roleId: Snowflake, member: Member) = member.removeRole(roleId).awaitSingleOrNull()
