package edu.utap.pictureperfect.ui.Models
import android.os.Parcel
import android.os.Parcelable

class User(
    var user_id: String? = null,
    var phone_number: Long = 0,
    var email: String? = null,
    var username: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user_id)
        parcel.writeLong(phone_number)
        parcel.writeString(email)
        parcel.writeString(username)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "User(user_id=$user_id, phone_number=$phone_number, email=$email, username=$username)"
    }
}
