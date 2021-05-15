import discord4j.core.`object`.entity.Message

fun isSentByOwner(message: Message) = message.author.get().id.asLong() == 286443116326682624