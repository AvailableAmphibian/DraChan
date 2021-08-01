package swarfarm_models

data class LeaderSkill(val id: Long, val attribute: String, val amount: Long, val area: String, val element: String?) {
    fun display(): String {
        val sb = StringBuilder()

        if (element != null)
            sb.append("**$element**\n")
        else
            sb.append("**$area**\n")

        sb.append("$amount% $attribute")

        return sb.toString()
    }
}
