package swarfarm_models

import discord4j.rest.util.Color

enum class Element(val color: Color) {
    Fire(Color.RED), Water(Color.ENDEAVOUR), Wind(Color.YELLOW), Light(Color.DISCORD_WHITE), Dark(Color.DEEP_LILAC);

    companion object{
        fun getElement(str: String): Element {
            if (str.equals(Fire.name, ignoreCase = true))
                return Fire
            if (str.equals(Water.name, ignoreCase = true))
                return Water
            if (str.equals(Wind.name, ignoreCase = true))
                return Wind
            if (str.equals(Light.name, ignoreCase = true))
                return Light
            return Dark
        }
    }
}