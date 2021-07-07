package commands

import discord4j.core.`object`.entity.Message
import discord4j.rest.util.Color
import isSentByOwner
import kotlinx.coroutines.reactor.awaitSingle

suspend fun help(message: Message) {
    val channel = message.channel.awaitSingle()

    channel.createMessage { spec ->
        spec.setContent(if (isSentByOwner(message)) "${message.author.get().mention} thinks you need help, here it is :" else "Here's the help panel ${message.author.get().mention}, take a nice look at it !")
            .setEmbed {
                it.setTitle("Help panel")
                    .setColor(Color.CYAN)
                    .setDescription(
                        """
                        |**`?help`** -> Displays this
                        |**`?ping`** -> A bit of a useless command, returns "Pong?"
                        |**`?melody`** -> Shows something about a friend :eyes: (Needs improvement)
                        |**`?dice`** -> Returns a random number between 1 and 6 (both included). (Needs improvement)
                        |**`?reactionRole {channelId} {messageId} {roleMention or id} {Number between 1 and 4 included}`** -> Creates a new reaction role . Needs "Manage channel" and "Manage roles" permissions.
                        |
                        |Aliases :
                        |<@843037701773983786> -> alias of `?help`
                    """.trimMargin()
                    )
            }
    }.awaitSingle()
}
