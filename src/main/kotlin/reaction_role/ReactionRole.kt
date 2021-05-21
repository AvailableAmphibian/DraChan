package reaction_role

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.transactions.transaction

class ReactionRole(
    id: EntityID<Int>,
) : IntEntity(id) {
    companion object : IntEntityClass<ReactionRole>(ReactionRoleTable) {
        var counter = transaction { ReactionRole.all().last().reactionRoleId }
    }
    var reactionRoleId by ReactionRoleTable.reactionRoleId
    var guildId by ReactionRoleTable.guildId
    var messageId by ReactionRoleTable.messageId
    var roleId by ReactionRoleTable.roleId
    var reaction by ReactionRoleTable.emoji
}

