package swarfarm_models

import com.google.gson.annotations.SerializedName

class Skill(
    val id: Int,
    val name: String,
    val description: String,
    val slot: Long,
    private val aoe: Boolean,
    @SerializedName("max_level") val maxLevel: Long,
    @SerializedName("level_progress_description") val upgrades: List<String>,
    @SerializedName("multiplier_formula") val multiplierFormula:String,
    @SerializedName("scales_with") val scales:List<String>,
    private val effects: List<SkillEffect>?
){


    fun getPositiveEffects() = createEffectsList(isBuff = true)
    fun getNegativeEffects() = createEffectsList(isBuff = false)

    private fun createEffectsList(isBuff:Boolean): List<SkillEffect> {
       val list = ArrayList<SkillEffect>()
        effects!!.stream()
            .filter{ it.effect.isBuff == isBuff}
            .forEach { list.add(it) }
        return list
    }


    fun targetType():String{
        if (aoe)
            return "AoE skill"
        return "Single target skill"
    }

    fun showScales(): String {

        val scales = ArrayList(this.scales)
        val sb = StringBuilder(scales.removeAt(0))
        scales.forEach { sb.append(", $it") }
        return sb.toString()
    }

    fun showUpgrades():String{
        val upgrades = ArrayList(this.upgrades)
        val sb = StringBuilder(upgrades.removeAt(0))
        upgrades.forEach { sb.append(",\n$it") }
        return sb.toString()
    }

    fun showBuffs():String{
        val effectsAL = ArrayList<SkillEffect>(getPositiveEffects())
        return handleEffectList(effectsAL)
    }

    fun showDebuffs():String{
        val effectsAL = ArrayList<SkillEffect>(getNegativeEffects())
        return handleEffectList(effectsAL)
    }

    companion object {
        fun handleEffectList(effectsAL: ArrayList<SkillEffect>):String{
            val sb = StringBuilder(effectsAL.removeAt(0).toString())
            effectsAL.forEach { sb.append(", $it") }
            return sb.toString()
        }
    }
}
