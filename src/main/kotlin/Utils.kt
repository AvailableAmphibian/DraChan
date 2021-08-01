import discord4j.core.`object`.entity.Message
import discord4j.core.spec.EmbedCreateSpec
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest

fun isSentByOwner(message: Message) = message.author.get().id.asLong() == 286443116326682624

fun EmbedCreateSpec.setFooter(addedText: String): EmbedCreateSpec =
    this.setFooter(
        "Programmed from the ❤ by Dra !️ $addedText",
        createAvatarUrl(Main.owner.avatar().get(), Main.owner.id().asLong())
    )

fun createAvatarUrl(avatar: String, userId: Long): String {
    val url = "https://cdn.discordapp.com/avatars/$userId/$avatar"
    return if (avatar.startsWith("a_")) "$url.gif" else "$url.png"
}

fun createCommand(
    name: String,
    description: String,
    vararg options: ApplicationCommandOptionData
): ApplicationCommandRequest {
    val command = ApplicationCommandRequest.builder().name(name).description(description)

    options.forEach { command.addOption(it) }

    return command.build()
}

//Reaction types
const val REACTION_ROLE_GIVE = 1
const val REACTION_ROLE_REMOVE = 2
const val REACTION_ROLE_GIVE_NOT_RETROACTIVE = 3
const val REACTION_ROLE_REMOVE_NOT_RETROACTIVE = 4

//Guilds
const val FISH = 333569887135858689
const val FUGU = 854345025647017984

//URLs
const val BASE_URL_SWARFARM = "https://swarfarm.com/api/v2/"