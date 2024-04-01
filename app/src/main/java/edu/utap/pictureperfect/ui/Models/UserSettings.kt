package edu.utap.pictureperfect.ui.Models

class UserSettings(
    var user: User?,
    var settings: UserAccountSettings?
) {
    constructor() : this(null, null)

    override fun toString(): String {
        return "UserSettings{user=$user, settings=$settings}"
    }
}