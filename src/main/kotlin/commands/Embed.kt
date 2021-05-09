package commands

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Color
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle

suspend fun embed(event: MessageCreateEvent, end:String?){
    val channel = event.message.channel.awaitSingle()
    val guild = event.guild.awaitSingle()
    val receiver = guild.channels.filter{
        println("id : ${it.id}, name : ${it.name}")
        it.name == "L'océan" || it.id.asLong() == 598608052249296935
    }.awaitFirst()

    println("======= Sending embed =======")

    channel.createEmbed {
        val author = event.member.get()
        it.setAuthor(author.displayName,author.avatarUrl,author.avatarUrl)
        it.setColor(Color.CINNABAR)
        it.setDescription("Hello this is an embed !")
        it.setFooter(end?:"",null)
    }.awaitSingle()

}