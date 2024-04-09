package edu.utap.pictureperfect.ui.Models

data class Notification(
    var from: String = "",
    var type: String = "",
    var message: String = "",
    var date_created: String = "",
) {
    // Empty constructor required by Firebase
    constructor() : this("", "", "", "")
}
