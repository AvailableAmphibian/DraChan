import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Member
import discord4j.rest.util.Color
import kotlinx.coroutines.reactor.awaitSingle

suspend fun onUserJoined(member: Member){
    //TODO Update for FISH
    val guild = member.guild.awaitSingle()
    val dra = guild.getMemberById(Snowflake.of(286443116326682624)).awaitSingle()
    if (guild.id.equals(Snowflake.of(333569887135858689))) {
        member.addRole(Snowflake.of(597367398261194757)).awaitSingle()
        val privateChannel = member.privateChannel.awaitSingle()

        privateChannel.createEmbed {
            it.setColor(Color.MAGENTA)
                .setDescription(
                    """
                |Hey <@${member.id.asLong()} !
                |Tu viens de rejoindre le serveur Discord de la guilde de FISH ! 
                |Bienvenue à toi !
                |
                |Pour pouvoir accéder au reste du serveur je t'invite à accepter le règlement présent ici <#693089094313574480> !
            """.trimMargin()
                )
                .setFooter("Programmed with :heart: by Dra",dra.avatarUrl)
        }.awaitSingle()
    }
}