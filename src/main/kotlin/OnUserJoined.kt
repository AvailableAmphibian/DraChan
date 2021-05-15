import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Member
import discord4j.rest.util.Color
import kotlinx.coroutines.reactor.awaitSingle

suspend fun onUserJoined(member: Member){
    member.addRole(Snowflake.of(842719376293888020))
    val privateChannel = member.privateChannel.awaitSingle()

    privateChannel.createEmbed {
        it.setColor(Color.MAGENTA)
            .setDescription("""
                |Hey <@${member.id.asLong()} !
                |You just joined Dra's Server, welcome !
            """.trimMargin())
    }.awaitSingle()
}