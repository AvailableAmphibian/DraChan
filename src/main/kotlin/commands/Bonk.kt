package commands

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull

suspend fun bonk(event:ChatInputInteractionEvent){
    val user = event.getOption("bonk_them").flatMap(ApplicationCommandInteractionOption::getValue)
        .map(ApplicationCommandInteractionOptionValue::asUser).get().awaitSingle()

    event.reply(":hammer: BONK <@${user.id.asString()}> ! :hammer:").awaitSingleOrNull()
}