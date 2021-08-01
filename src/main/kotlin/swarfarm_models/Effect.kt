package swarfarm_models

import com.google.gson.annotations.SerializedName

data class Effect(val name: String, @SerializedName("is_buff") val isBuff: Boolean)
