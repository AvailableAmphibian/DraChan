package swarfarm_models

import com.google.gson.annotations.SerializedName

data class SkillEffect(
    val effect: Effect,
    private val aoe: Boolean,
    @SerializedName("self_effect") private val selfEffect: Boolean,
    private val chance: Long?,
    @SerializedName("on_crit") private val onCrit: Boolean,
    @SerializedName("on_death") private val onDeath: Boolean,
    private val quantity: Long?,
    @SerializedName("self_hp") private val selfHp: Boolean,
    @SerializedName("target_hp") private val targetHp: Boolean,
){
    override fun toString(): String {
        val sb = StringBuilder()
        if (selfEffect) sb.append("Self-")

        if (aoe) sb.append("AoE ")
        sb.append("${effect.name} ")
        if (quantity != null)
            sb.append("$quantity ")
        if (chance != null)
            sb.append("for $chance% ")

        if (onCrit)
            sb.append("on crit ")
        else if (onDeath)
            sb.append("on death ")

        if (selfHp)
            sb.append("using self hp")
        if (targetHp)
            sb.append("on target's hp")

        return sb.toString()
    }
}

