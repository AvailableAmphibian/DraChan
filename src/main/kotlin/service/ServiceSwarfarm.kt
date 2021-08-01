package service

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import swarfarm_models.Monster
import swarfarm_models.MonsterPage
import swarfarm_models.Skill

interface ServiceSwarfarm {
    @GET("monsters")
    fun getMonsterList(): Call<MonsterPage>

    @GET("monsters")
    fun getMonsterList(@Query("page") page: Int): Call<MonsterPage>

    @GET("monsters/{id}")
    fun getMonster(@Path("id") id: Int): Call<Monster>

    @GET("skills/{id}")
    fun getSkill(@Path("id") id: Long): Call<Skill>
}