package reaction_role

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Key
import org.jetbrains.exposed.sql.Table

object ReactionRoleTable : IntIdTable() {
    val reactionRoleId = integer("ID").uniqueIndex()
    val guildId = long("guildId")
    val messageId = long("messageId")
    val roleId = long("roleId")
    val emoji = varchar("reaction", 20)
}
