package edu.utap.pictureperfect.ui.Models

import android.os.Parcel
import android.os.Parcelable

class UserAccountSettings(
    var user_bio: String? = null,
    var display_name: String? = null,
    var followers: Long = 0,
    var following: Long = 0,
    var posts: Long = 0,
    var profile_photo: String? = null,
    var username: String? = null,
    var user_id: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user_bio)
        parcel.writeString(display_name)
        parcel.writeLong(followers)
        parcel.writeLong(following)
        parcel.writeLong(posts)
        parcel.writeString(profile_photo)
        parcel.writeString(username)
        parcel.writeString(user_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserAccountSettings> {
        override fun createFromParcel(parcel: Parcel): UserAccountSettings {
            return UserAccountSettings(parcel)
        }

        override fun newArray(size: Int): Array<UserAccountSettings?> {
            return arrayOfNulls(size)
        }
    }
}
