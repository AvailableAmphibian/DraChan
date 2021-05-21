package commands

import discord4j.core.`object`.entity.Message
import discord4j.rest.util.Color
import kotlinx.coroutines.reactor.awaitSingle
import java.util.*

suspend fun dice(message: Message) {
    val channel = message.channel.awaitSingle()
    val random = Random()
    val author = message.author.get()
    val randNum = random.nextInt(6) + 1
    channel.createMessage { spec ->
        spec.setEmbed {
            it.setDescription(
                """
                |Hey <@!${author.id.asLong()}> !
                |You rolled a $randNum !
                """.trimMargin()
            ).setColor(Color.PINK)
        }
    }.awaitSingle()


}