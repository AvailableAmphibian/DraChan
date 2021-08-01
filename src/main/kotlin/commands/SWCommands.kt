package commands

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.core.spec.EmbedCreateSpec
import kotlinx.coroutines.reactor.awaitSingleOrNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import service.RetrofitInstance
import setFooter
import swarfarm_models.Element
import swarfarm_models.Monster
import swarfarm_models.MonsterId
import swarfarm_models.Skill

suspend fun getMonster(event: SlashCommandEvent) {
    val name = event.getOption("name").flatMap(ApplicationCommandInteractionOption::getValue)
        .map(ApplicationCommandInteractionOptionValue::asString).orElse("~")

    val monsterId = MonsterId.fetchMonsterList(name)

    if (monsterId == null) {
        event.reply("Couldn't find the monster...").awaitSingleOrNull()
        return
    }

    RetrofitInstance.SW_API.getMonster(monsterId.id).enqueue(object : Callback<Monster> {
        override fun onResponse(call: Call<Monster>, response: Response<Monster>) {
            val monster = response.body()!!
            event.reply { spec ->
                spec.addEmbed {
                    it.setHeader(monsterId = monsterId)
                        .addField(
                            "Base Stats (level 40)",
                            """
                            |**HP** : ${monster.maxLvlHp} ー **SPD** : ${monster.speed} 
                            |**ATK** : ${monster.maxLvlAtk} ー **DEF** : ${monster.maxLvlDef} 
                            |**CR** : ${monster.critRate} ー **CD** : ${monster.critDmg} 
                            |**ACC** : ${monster.accuracy} ー **RES** : ${monster.resistance} 
                            """.trimMargin(),
                            true
                        )
                        .addField(
                            "Miscellaneous",
                            """
                            |Type : **${monster.archetype}
                            |Base stars : **${monster.naturalStars}
                            """.trimMargin(), true
                        )
                    if (monster.leaderSkill != null)
                        it.addField(
                            "Leader Skill",
                            monster.leaderSkill.display(),
                            true
                        )
                }
            }.block()
        }

        override fun onFailure(call: Call<Monster>, t: Throwable) {
            t.printStackTrace()
        }

    })
}

suspend fun swSkills(event: SlashCommandEvent) {
    val name = event.getOption("name").flatMap(ApplicationCommandInteractionOption::getValue)
        .map(ApplicationCommandInteractionOptionValue::asString).orElse("~")
    val skill = event.getOption("skill_number").flatMap(ApplicationCommandInteractionOption::getValue)
        .map(ApplicationCommandInteractionOptionValue::asLong).orElse(0L)


    val monsterId = MonsterId.fetchMonsterList(name)

    if (monsterId == null) {
        event.reply { it.setContent("Couldn't find the monster...") }.awaitSingleOrNull()
        return
    }

    if (skill == 0L) {
        displayAllSkills(monsterId, event)
        return
    }
    if (skill in 1..monsterId.skills.size) {
        displaySingleSkill(monsterId, event, skill)
        return
    }
    event.reply { it.setContent("Skill number provided does not match") }.awaitSingleOrNull()
}

private suspend fun displayAllSkills(monster: MonsterId, event: SlashCommandEvent) {
    val skills = ArrayList<Skill>()

    monster.skills.forEach {
        val skill = RetrofitInstance.SW_API.getSkill(it).execute().body()!!
        skills.add(skill)
    }

    event.reply { spec ->
        spec.addEmbed {
            it.setHeader(monsterId = monster)
            skills.forEach { skill ->
                it.addSkillField(skill = skill)
            }
        }
    }.awaitSingleOrNull()
}

private fun displaySingleSkill(monster: MonsterId, event: SlashCommandEvent, skillNumber: Long) {
    RetrofitInstance.SW_API.getSkill(monster.skills[(skillNumber - 1).toInt()]).enqueue(object : Callback<Skill> {
        override fun onResponse(call: Call<Skill>, response: Response<Skill>) {
            val skill = response.body()!!

            event.reply { spec ->
                spec.addEmbed {
                    it.setHeader(monsterId = monster)
                    it.addUniqueSkillField(skill = skill)
                }
            }.block()
        }

        override fun onFailure(call: Call<Skill>, t: Throwable) {
            t.printStackTrace()
        }

    })

}

private fun EmbedCreateSpec.addSkillField(skill: Skill): EmbedCreateSpec {
    this.addField("Skill ${skill.slot} : ${skill.name}", skill.description, false)

    if (skill.scales.isEmpty()) {
        this.addField("Skill information", "Support ${skill.targetType()}", true)
        if (skill.getPositiveEffects().isNotEmpty())
            this.addField("Positive effects", skill.showBuffs(), true)
        if (skill.getNegativeEffects().isNotEmpty())
            this.addField("Negative effects", skill.showDebuffs(), true)
    } else
        this.addField(
            "Skill information",
            "${skill.targetType()}, scales on ${skill.showScales()}",
            true
        ).addField("Formula", "`${skill.multiplierFormula}`", true)

    return this
}

private fun EmbedCreateSpec.addUniqueSkillField(skill: Skill): EmbedCreateSpec {
    this.setTitle("Skill ${skill.slot} : ${skill.name}")
        .setDescription(skill.description)

    if (skill.scales.isEmpty())
        this.addField(
            "Skill information",
            "Support ${skill.targetType()}",
            false
        )
    else
        this.addField(
            "Skill information",
            "${skill.targetType()}, scales on ${skill.showScales()} as **`${skill.multiplierFormula}`**",
            false
        )

    if (skill.upgrades.isNotEmpty())
        this.addField("Upgrades", skill.showUpgrades(), true)
    if (skill.getPositiveEffects().isNotEmpty())
        this.addField("Positive effects", skill.showBuffs(), true)
    if (skill.getNegativeEffects().isNotEmpty())
        this.addField("Negative effects", skill.showDebuffs(), true)

    return this
}

private fun EmbedCreateSpec.setHeader(monsterId: MonsterId): EmbedCreateSpec =
    this.setAuthor(monsterId.name, null, monsterId.getMonsterIcon())
        .setFooter("Data fetched from SWarFarm's API !")
        .setColor(Element.getElement(monsterId.element).color)
