package commands

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.EmbedCreateFields
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec
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

suspend fun getMonster(event: ChatInputInteractionEvent) {
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

            event.reply(
                InteractionApplicationCommandCallbackSpec.builder()
                    .addEmbed(
                        EmbedCreateSpec.builder()
                            .setHeader(monsterId = monsterId)
                            .addAllFields(generateFields(monster))
                            .build()
                    )
                    .build()
            ).block()
        }

        override fun onFailure(call: Call<Monster>, t: Throwable) {
            t.printStackTrace()
        }

    })
}

suspend fun swSkills(event: ChatInputInteractionEvent) {
    val name = event.getOption("name").flatMap(ApplicationCommandInteractionOption::getValue)
        .map(ApplicationCommandInteractionOptionValue::asString).orElse("~")
    val skill = event.getOption("skill_number").flatMap(ApplicationCommandInteractionOption::getValue)
        .map(ApplicationCommandInteractionOptionValue::asLong).orElse(0L)


    val monsterId = MonsterId.fetchMonsterList(name)

    if (monsterId == null) {
        event.reply("Couldn't find the monster...").awaitSingleOrNull()
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
    event.reply("Skill number provided does not match").awaitSingleOrNull()
}

private suspend fun displayAllSkills(monster: MonsterId, event: ChatInputInteractionEvent) {
    val skills = ArrayList<Skill>()

    monster.skills.forEach {
        val skill = RetrofitInstance.SW_API.getSkill(it).execute().body()!!
        skills.add(skill)
    }

    val fields = ArrayList<EmbedCreateFields.Field>()
    skills.forEach {
        fields.addSkillField(skill = it)
    }


    event.reply(
        InteractionApplicationCommandCallbackSpec.builder()
            .addEmbed(
                EmbedCreateSpec.builder()
                    .setHeader(monsterId = monster)
                    .addAllFields(fields)
                    .build()
            )
            .build()
    ).awaitSingleOrNull()

    //TODO: Delete comment
//    event.reply { spec ->
//        spec.addEmbed {
//            it.setHeader(monsterId = monster)
//            skills.forEach { skill ->
//                it.addSkillField(skill = skill)
//            }
//        }
//    }.awaitSingleOrNull()
}

private fun displaySingleSkill(monster: MonsterId, event: ChatInputInteractionEvent, skillNumber: Long) {
    RetrofitInstance.SW_API.getSkill(monster.skills[(skillNumber - 1).toInt()]).enqueue(object : Callback<Skill> {
        override fun onResponse(call: Call<Skill>, response: Response<Skill>) {
            val skill = response.body()!!

            event.reply(InteractionApplicationCommandCallbackSpec.builder()
                .addEmbed(EmbedCreateSpec.builder()
                    .setHeader(monsterId = monster)
                    .addUniqueSkillField(skill = skill)
                    .build())
                .build()
            ).block()

            //TODO: Delete comment
//            event.reply { spec ->
//                spec.addEmbed {
//                    it.setHeader(monsterId = monster)
//                    it.addUniqueSkillField(skill = skill)
//                }
//            }.block()
        }

        override fun onFailure(call: Call<Skill>, t: Throwable) {
            t.printStackTrace()
        }

    })

}

private fun ArrayList<EmbedCreateFields.Field>.addSkillField(skill: Skill) {
    this.add(EmbedCreateFields.Field.of("Skill ${skill.slot} : ${skill.name}", skill.description, false))

    if (skill.scales.isEmpty()) {
        this.add(EmbedCreateFields.Field.of("Skill information", "Support ${skill.targetType()}", true))
        if (skill.getPositiveEffects().isNotEmpty())
            this.add(EmbedCreateFields.Field.of("Positive effects", skill.showBuffs(), true))
        if (skill.getNegativeEffects().isNotEmpty())
            this.add(EmbedCreateFields.Field.of("Negative effects", skill.showDebuffs(), true))
    } else {
        this.add(
            EmbedCreateFields.Field.of(
                "Skill information",
                "${skill.targetType()}, scales on ${skill.showScales()}",
                true
            )
        )
        this.add(EmbedCreateFields.Field.of("Formula", "`${skill.multiplierFormula}`", true))
    }
}

private fun EmbedCreateSpec.Builder.addUniqueSkillField(skill: Skill): EmbedCreateSpec.Builder {
    this.title("Skill ${skill.slot} : ${skill.name}")
        .description(skill.description)

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

private fun generateFields(monster: Monster): ArrayList<EmbedCreateFields.Field> =
    ArrayList<EmbedCreateFields.Field>().apply {
        add(
            EmbedCreateFields.Field.of(
                "Base Stats (level 40)",
                """
            |**HP** : ${monster.maxLvlHp} ー **SPD** : ${monster.speed} 
            |**ATK** : ${monster.maxLvlAtk} ー **DEF** : ${monster.maxLvlDef} 
            |**CR** : ${monster.critRate} ー **CD** : ${monster.critDmg} 
            |**ACC** : ${monster.accuracy} ー **RES** : ${monster.resistance} 
            """.trimMargin(),
                true
            )
        )
        add(
            EmbedCreateFields.Field.of(
                "Miscellaneous",
                """
            |Type : **${monster.archetype}
            |Base stars : **${monster.naturalStars}
            """.trimMargin(), true
            )
        )

        if (monster.leaderSkill != null)
            add(
                EmbedCreateFields.Field.of(
                    "Leader Skill",
                    monster.leaderSkill.display(),
                    true
                )
            )
    }


private fun EmbedCreateSpec.Builder.setHeader(monsterId: MonsterId): EmbedCreateSpec.Builder =
    this.author(monsterId.name, null, monsterId.getMonsterIcon())
        .setFooter("Data fetched from SWarFarm's API !")
        .color(Element.getElement(monsterId.element).color)
