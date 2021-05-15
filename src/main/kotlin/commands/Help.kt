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
                        |**?help** -> Displays this
                        |**?ping** -> A bit of a useless command, returns "Pong?"
                        |**?embed** -> Shows an embed message, if a channel is specified (id or #), sends it to this channel
                        |**?copy** -> Copies a message, the message copied is the reply if there's one, else it will be a message which the id as been provided in the command (needs to be fixed for replies)
                        |**?melody** -> Shows something about a friend :eyes:
                        |
                        |Aliases :
                        |<@!827605045485764650> -> alias of `?help`
                    """.trimMargin()
                    )
            }
    }.awaitSingle()
}
