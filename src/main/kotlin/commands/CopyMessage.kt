package commands

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Color
import kotlinx.coroutines.reactor.awaitSingle
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.*
import java.util.function.Consumer

suspend fun copyMessage(message: Message) {
    val channel = message.channel.awaitSingle()


    //TODO Fix replies
    if (message.referencedMessage.isPresent) {
        val author = message.authorAsMember.awaitSingle()
        val color = author.color.awaitSingle()
        val timestamp = message.timestamp.atOffset(ZoneOffset.UTC)
        channel.createMessage {
            it.setMessageReference(message.referencedMessage.get().id)
                .setEmbed(createEmbedCopy(message.content, author, color, timestamp))
        }.awaitSingle()
        return
    }


    try {
        val messageId = message.content.substringAfter("?copy ")

        val messageToCopy = message.client.getMessageById(channel.id, Snowflake.of(messageId)).awaitSingle()

        val author = messageToCopy.authorAsMember.awaitSingle()
        val color = author.color.awaitSingle()
        val timestamp = messageToCopy.timestamp.atOffset(ZoneOffset.UTC)
        channel.createMessage {
            it.setEmbed(createEmbedCopy(messageToCopy.content, author, color, timestamp))
            messageToCopy.referencedMessage.ifPresent { _ ->
                it.setMessageReference(Snowflake.of(messageId))
            }
        }.awaitSingle()
    } catch (e: Exception) {
        channel.createMessage("Nothing can be done here").awaitSingle()
    }
}

private fun createEmbedCopy(
    message: String,
    author: Member,
    color: Color,
    timestamp: OffsetDateTime
): Consumer<in EmbedCreateSpec> {
    return Consumer {
        EmbedCreateSpec().apply {
            it.setAuthor(author.tag, author.avatarUrl, author.avatarUrl)
                .setColor(color)
                .setDescription(message)
                .setFooter(
                    "${timestamp.month} ${timestamp.year}, ${
                        timestamp.dayOfWeek.getDisplayName(
                            TextStyle.FULL,
                            Locale.UK
                        )
                    } ${timestamp.dayOfMonth}, ${timestamp.hour}:${
                        String.format(
                            "%02d",
                            timestamp.minute
                        )
                    }\nAuthor's id : ${author.id.asLong()}", null
                )
        }
    }
}


