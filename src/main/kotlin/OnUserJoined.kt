import GuildIds.FISH
import GuildIds.FUGU
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Member
import discord4j.rest.util.Color
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull

private lateinit var dra: Member

private object GuildIds {
    const val FISH = 286443116326682624
    const val FUGU = 854345025647017984
}

suspend fun onUserJoined(member: Member) {
    val guild = member.guild.awaitSingle()
    dra = guild.getMemberById(Snowflake.of(286443116326682624)).awaitSingle()
    when (guild.id.asLong()) {
        FISH -> isFish(member)
        FUGU -> isFugu(member)
    }
    if (guild.id.equals(Snowflake.of(333569887135858689)))
        isFish(member)
    else if (guild.id.equals(Snowflake.of(854345025647017984)))
        isFugu(member)
}

suspend fun isFugu(member: Member) {
    userJoined(
        member, roleId = 857300685140525056, embedColor = Color.MOON_YELLOW, """
        |Bienvenue à toi sur le Discord AnotherFugu !
        |
        |Tu es maintenant un <@&857300685140525056>, tu peux commencer à intéragir avec le reste de notre monde ! :O
        |(Fais pas gaffe, on a perdu la laisse de Mélo, il mord un peu mais il est gentil)
        """
    )
}

suspend fun isFish(member: Member) {
    userJoined(
        member, roleId = 597367398261194757, embedColor = Color.MAGENTA, """
                        |Tu viens de rejoindre le serveur Discord de la guilde de FISH ! 
                        |Bienvenue à toi !
                        |
                        |Pour pouvoir accéder au reste du serveur je t'invite à accepter le règlement présent ici <#693089094313574480> !

    """
    )
}

private suspend fun userJoined(member: Member, roleId: Long, embedColor: Color, joinText: String) {
    member.addRole(Snowflake.of(roleId)).awaitSingleOrNull()
    val privateChannel = member.privateChannel.awaitSingle()

    privateChannel.createEmbed {
        it.setColor(embedColor)
            .setDescription(
                """
                |Hey <@${member.id.asLong()}> !
                |$joinText
                """.trimMargin()
            )
            .setFooter("Programmed with ❤️ by Dra", dra.avatarUrl)
    }.awaitSingle()
}