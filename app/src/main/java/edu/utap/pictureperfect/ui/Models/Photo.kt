package edu.utap.pictureperfect.ui.Models

class Photo {
    var caption: String = ""
        get() = field
        set(value) {
            field = value
        }

    var date_created: String = ""
        get() = field
        set(value) {
            field = value
        }

    var image_path: String = ""
        get() = field
        set(value) {
            field = value
        }

    var photo_id: String = ""
        get() = field
        set(value) {
            field = value
        }

    var user_id: String = ""
        get() = field
        set(value) {
            field = value
        }

    var tags: String = ""
        get() = field
        set(value) {
            field = value
        }

    var comments: ArrayList<String> = ArrayList()
        get() = field
        set(value) {
            field = value
        }

    var likes: Long = 0
        get() = field
        set(value) {
            field = value
        }

    var liked_users: ArrayList<String> = ArrayList()
        get() = field
        set(value) {
            field = value
        }
}
