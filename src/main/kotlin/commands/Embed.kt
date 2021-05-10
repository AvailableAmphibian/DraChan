package commands

import discord4j.common.util.Snowflake
import discord4j.core.`object`.Embed
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.discordjson.json.EmbedData
import discord4j.rest.util.Color
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.awaitSingle
import java.lang.NumberFormatException

suspend fun embed(event: MessageCreateEvent, end: String?) {
    val channel = event.message.channel.awaitSingle()
    if (end != null) {

        try {
            val id = end.substringBefore(">").substringAfter("<#")
            println(id)
            val anotherChannel = event.client.getChannelById(Snowflake.of(id)).awaitSingle()
            val author = event.message.author.get()
            val embed = EmbedCreateSpec()
                .setAuthor(author.username, null, author.avatarUrl)
                .setDescription("Hello from $end")
                .setImage(author.avatarUrl)
                .setColor(Color.HOKI)
                .setFooter(event.message.data.timestamp(), author.avatarUrl)

            channel.createMessage("Sending the embed to <#${anotherChannel.id.asLong()}>").awaitSingle()

            println("Sending to other channel")
            anotherChannel.restChannel.createMessage("Message coming from <#${channel.id.asLong()}>, sent by <@${event.member.get().id.asLong()}>").awaitSingle()

            println("======= Sending embed =======")
            anotherChannel.restChannel.createMessage(embed.asRequest()).awaitSingle()
        } catch (e: NumberFormatException) {
            println(end)
            channel.createEmbed {
                val author = event.message.author.get()
                it.setAuthor(author.username, null, author.avatarUrl)
                    .setDescription("They said :\n$end  ")
                    .setImage(author.avatarUrl)
                    .setColor(Color.HOKI)
                    .setFooter(event.message.data.timestamp(), author.avatarUrl)
            }.awaitSingle()
        } catch (e: Exception) {
            e.printStackTrace()
            channel.createMessage("$e. scum.").awaitSingle()
        }
    }

}