package commands

import REACTION_ROLE_GIVE
import REACTION_ROLE_REMOVE_NOT_RETROACTIVE
import discord4j.common.util.Snowflake
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.jetbrains.exposed.sql.transactions.transaction
import reaction_role.ReactionRoles
import java.util.*

object Reaction {
    var isCreatingReactionRole = false
    lateinit var createRRMessage: Message
    lateinit var message: Message
    var role: Long = 0
    var rrType: Int = 0
}

suspend fun reactionRole(slashCommandEvent: ChatInputInteractionEvent) {
    val permissions = slashCommandEvent.interaction.member.get().basePermissions.awaitSingle()

    if (slashCommandEvent.interaction.guildId.isEmpty ||
        !permissions.containsAll(PermissionSet.of(Permission.MANAGE_CHANNELS, Permission.MANAGE_ROLES))
    ) {
        slashCommandEvent.reply("Sorry you don't have the required permissions mate").awaitSingle()
        return
    }

    val channelId = slashCommandEvent.getOption("channel").flatMap(ApplicationCommandInteractionOption::getValue)
        .map(ApplicationCommandInteractionOptionValue::asSnowflake).get()
    val messageId = slashCommandEvent.getOption("message_id").flatMap(ApplicationCommandInteractionOption::getValue)
        .map(ApplicationCommandInteractionOptionValue::asString).get()
    val role = slashCommandEvent.getOption("role").flatMap(ApplicationCommandInteractionOption::getValue)
        .map(ApplicationCommandInteractionOptionValue::asRole).get().awaitSingle()
    val rrType = slashCommandEvent.getOption("rr_type").flatMap(ApplicationCommandInteractionOption::getValue)
        .map(ApplicationCommandInteractionOptionValue::asLong).orElse(1L)

    if (rrType !in REACTION_ROLE_GIVE..REACTION_ROLE_REMOVE_NOT_RETROACTIVE) {
        slashCommandEvent.reply(
            InteractionApplicationCommandCallbackSpec.builder()
                .content("Please provide a good id")
                .addEmbed(
                    EmbedCreateSpec.builder()
                        .addField("REACTION_ROLE_GIVE", "1", true)
                        .addField("REACTION_ROLE_REMOVE", "2", true)
                        .addField("REACTION_ROLE_GIVE_NOT_RETROACTIVE", "3", true)
                        .addField("REACTION_ROLE_REMOVE_NOT_RETROACTIVE", "4", true)
                        .build()
                )
                .build()
        ).awaitSingleOrNull()
        return
    }

    Reaction.isCreatingReactionRole = true
    Reaction.message =
        slashCommandEvent.client.getMessageById(Snowflake.of(channelId.asLong()), Snowflake.of(messageId)).awaitSingle()

    Reaction.role = role.id.asLong()
    Reaction.rrType = rrType.toInt()

    slashCommandEvent.reply("Please react to this message with the emoji you want !").awaitSingleOrNull()
}

//fixme : Verify if this emoji is available in this guild
suspend fun finishReactionRoleCreation(reactionAddEvent: ReactionAddEvent) {
    Reaction.isCreatingReactionRole = false

    val emoji = reactionAddEvent.emoji
    val guild = reactionAddEvent.guild.awaitSingle().id.asLong()
    val rrMessageId = Reaction.message.id.asLong()
    val role = Reaction.role

    val emojiAsString = when (emoji) {
        is ReactionEmoji.Custom -> emoji.id.asString()
        is ReactionEmoji.Unicode -> emoji.raw
        else -> ""
    }

    transaction {
        ReactionRoles.new {
            reactionRoleId = Objects.hash(guild, rrMessageId, role, emojiAsString)
            guildId = guild
            messageId = rrMessageId
            roleId = role
            reaction = emojiAsString
            rrType = Reaction.rrType
        }
    }
    Reaction.message.addReaction(emoji).awaitSingleOrNull()
}
