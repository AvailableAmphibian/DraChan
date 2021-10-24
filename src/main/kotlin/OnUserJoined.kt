import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Member
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Color
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull

suspend fun onUserJoined(member: Member) {
    val guild = member.guild.awaitSingle()
    when (guild.id.asLong()) {
        FISH -> isFish(member)
        FUGU -> isFugu(member)
    }
}

suspend fun isFugu(member: Member) = userJoined(
    member, roleId = 857300685140525056, embedColor = Color.MOON_YELLOW, """
        |Bienvenue à toi sur le Discord AnotherFugu !
        |
        |Tu es maintenant un <@&857300685140525056>, tu peux commencer à intéragir avec le reste de notre monde ! :O
        |(Fais pas gaffe, on a perdu la laisse de Mélo, il mord un peu mais il est gentil)
        """
)


suspend fun isFish(member: Member) = userJoined(
    member, roleId = 597367398261194757, embedColor = Color.MAGENTA, """
                        |Tu viens de rejoindre le serveur Discord de la guilde de FISH ! 
                        |Bienvenue à toi !
                        |
                        |Pour pouvoir accéder au reste du serveur je t'invite à accepter le règlement présent ici <#693089094313574480> !

    """
)


private suspend fun userJoined(member: Member, roleId: Long, embedColor: Color, joinText: String) {
    member.addRole(Snowflake.of(roleId)).awaitSingleOrNull()
    val privateChannel = member.privateChannel.awaitSingle()

    privateChannel.createMessage(
        EmbedCreateSpec.builder()
            .color(embedColor)
            .description(
                """
                |Hey <@${member.id.asLong()}> !
                |$joinText
                """.trimMargin()
            ).setFooter("")
            .build()
    ).awaitSingleOrNull()
}