package reaction_role

import org.jetbrains.exposed.dao.id.IntIdTable

object ReactionRolesTable : IntIdTable() {
    val reactionRoleId = integer("ID").uniqueIndex()
    val guildId = long("guildId")
    val messageId = long("messageId")
    val roleId = long("roleId")
    val emoji = varchar("reaction", 20)
    val rrType = integer("rrType")
}
