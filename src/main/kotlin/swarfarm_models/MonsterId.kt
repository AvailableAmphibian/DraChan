package swarfarm_models

import com.google.gson.annotations.SerializedName
import service.RetrofitInstance

data class MonsterId(
    val id: Int,
    val name: String,
    val element: String,
    @SerializedName("image_filename") val image: String,
    @SerializedName("awaken_level") private val awakenLevel: Int,
    val skills: ArrayList<Long>,
    private var secondaryName: String?
) {
    fun getMonsterIcon() = "https://swarfarm.com/static/herders/images/monsters/$image"

    companion object {
        var monsterList: List<MonsterId> = ArrayList()

        fun fetchMonsterList(name: String): MonsterId? = monsterList.stream().filter {
            it.name.equals(
                name.replace("_", " ").replace("-", " "),
                ignoreCase = true
            ) || it.secondaryName.equals(name, ignoreCase = true)

        }.findFirst().orElseGet { null }

        fun initMonsterList() {
            val monsterList = ArrayList<MonsterId>()
            val response = RetrofitInstance.SW_API.getMonsterList().execute()
            val body = response.body()!!

            monsterList.addAll(body.results)
            if (body.next == null)
                Companion.monsterList = monsterList
            else
                Companion.monsterList = completeMonsterList(monsterList, 2)

            setSecondaryNames()
        }

        private tailrec fun completeMonsterList(monsterList: ArrayList<MonsterId>, page: Int): List<MonsterId> {
            val response = RetrofitInstance.SW_API.getMonsterList(page).execute().body()!!
            monsterList.addAll(response.results)

            if (response.next != null)
                return completeMonsterList(monsterList, page + 1)

            return monsterList
        }

        private fun setSecondaryNames() {
            monsterList.forEach {
                if (it.awakenLevel == 2) {
                    it.secondaryName = "${it.name.split(" ")[0]}2A"
                    return@forEach
                }

                if (it.name.matches(Regex("^[A-Z]+$")) || it.awakenLevel == 0) {
                    it.secondaryName = "${it.name} ${it.element}"
                    return@forEach
                }

                it.secondaryName = it.name.replace(" ", "")
            }
        }
    }
}
