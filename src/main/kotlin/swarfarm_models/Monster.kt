package swarfarm_models

import com.google.gson.annotations.SerializedName

data class Monster(
    val name: String,
    val element: String,
    val archetype: String,
    @SerializedName("base_stars") val baseStars: Long,
    @SerializedName("natural_stars") val naturalStars: Long,
    @SerializedName("can_awaken") val canAwaken: Boolean,
    @SerializedName("awaken_bonus") val awakenBonus: String,
    @SerializedName("leader_skill") val leaderSkill: LeaderSkill?,
    @SerializedName("base_hp") val baseHp:Long,
    @SerializedName("base_attack") val baseAtk:Long,
    @SerializedName("base_defense") val baseDef:Long,
    @SerializedName("speed") val speed:Long,
    @SerializedName("crit_rate") val critRate:Long,
    @SerializedName("crit_damage") val critDmg:Long,
    val resistance:Long,
    val accuracy:Long,
    @SerializedName("raw_hp") val rawHp:Long,
    @SerializedName("raw_attack") val rawAtk:Long,
    @SerializedName("raw_defense") val rawDef:Long,
    @SerializedName("max_lvl_hp") val maxLvlHp:Long,
    @SerializedName("max_lvl_attack") val maxLvlAtk:Long,
    @SerializedName("max_lvl_defense") val maxLvlDef:Long,
    @SerializedName("awakens_to") val awakensTo:Long?
) {
}
