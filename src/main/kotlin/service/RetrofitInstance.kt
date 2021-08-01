package service

import BASE_URL_SWARFARM
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val SW_API: ServiceSwarfarm by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_SWARFARM)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServiceSwarfarm::class.java)
    }
}