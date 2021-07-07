package reaction_role

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ReactionRoles(
    id: EntityID<Int>,
) : IntEntity(id) {
    companion object : IntEntityClass<ReactionRoles>(ReactionRolesTable)

    var reactionRoleId by ReactionRolesTable.reactionRoleId
    var guildId by ReactionRolesTable.guildId
    var messageId by ReactionRolesTable.messageId
    var roleId by ReactionRolesTable.roleId
    var reaction by ReactionRolesTable.emoji
    var rrType by ReactionRolesTable.rrType
}
