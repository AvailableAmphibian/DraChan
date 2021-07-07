import discord4j.core.`object`.entity.Message

fun isSentByOwner(message: Message) = message.author.get().id.asLong() == 286443116326682624

const val REACTION_ROLE_GIVE = 1
const val REACTION_ROLE_REMOVE = 2
const val REACTION_ROLE_GIVE_NOT_RETROACTIVE = 3
const val REACTION_ROLE_REMOVE_NOT_RETROACTIVE = 4
