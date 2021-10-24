package commands

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec
import discord4j.rest.util.Color
import kotlinx.coroutines.reactor.awaitSingleOrNull
import setFooter

suspend fun help(slashCommandEvent: ChatInputInteractionEvent) = slashCommandEvent.reply(InteractionApplicationCommandCallbackSpec.builder()
        .content("Here's the help panel mate !")
        .addEmbed(EmbedCreateSpec.builder()
            .title("Help panel")
            .color(Color.PINK)
            .addField("help", "This command.",false)
            .addField("ping", "Command which returns \"Pong\".",false)
            .addField("reactionRole", "Creates a ReactionRole, see command's options.",false)
            .addField("swskills", "Displays the skills of a monster from Summoners War or if precised the details of the skill.", false)
            .addField("sw_monster","Displays the stats of a monster from Summoners War.", false)
            .addField("bonk", "BONK", false)
            .setFooter("")
            .build())
        .build()
    ).awaitSingleOrNull()

