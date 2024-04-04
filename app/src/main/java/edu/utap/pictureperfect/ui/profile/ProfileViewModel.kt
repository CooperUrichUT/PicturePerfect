package edu.utap.pictureperfect.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.utap.pictureperfect.ui.Utils.TakePictureWrapper

class ProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is profile Fragment"
    }
    val text: LiveData<String> = _text
    private var _url: String = ""
    private var TAG = "ProfileViewModel"

    // Getter for url
    fun getUrl(): String {
        return _url
    }

    // Setter for url
    fun setUrl(url: String) {
        _url = url
    }

    private var pictureUUID = ""
    private val _lastPictureUri = MutableLiveData<Uri>()
    val lastPictureUri: LiveData<Uri> get() = _lastPictureUri

    // Only call this from TakePictureWrapper
    fun takePictureUUID(uuid: String) {
        pictureUUID = uuid
    }
    var pictureNameByUser = "" // String provided by the user
    // LiveData for entire note list, all images

//
//    private fun createPhotoMeta(pictureTitle: String, uuid : String,
//                                byteSize : Long) {
//        val currentUser = currentAuthUser
//        val photoMeta = PhotoMeta(
//            ownerName = currentUser.name,
//            ownerUid = currentUser.uid,
//            uuid = uuid,
//            byteSize = byteSize,
//            pictureTitle = pictureTitle,
//        )
//        Log.d("createPhotoMeta", "Created photoMeta: $photoMeta")
//        dbHelp.createPhotoMeta(sortInfo.value!!, photoMeta) {
//            photoMetaList.postValue(it)
//
//        }
//    }

    /////////////////////////////////////////////////////////////
    // We can't just schedule the file upload and return.
    // The problem is that our previous picture uploads can still be pending.
    // So a note can have a pictureFileName that does not refer to an existing file.
    // That violates referential integrity, which we really like in our db (and programming
    // model).
    // So we do not add the pictureFileName to the note until the picture finishes uploading.
    // That means a user won't see their picture updates immediately, they have to
    // wait for some interaction with the server.
    // You could imagine dealing with this somehow using local files while waiting for
    // a server interaction, but that seems error prone.
    // Freezing the app during an upload also seems bad.
    fun pictureSuccess() {
        val photoFile = TakePictureWrapper.fileNameToFile(pictureUUID)
        if (photoFile.exists()) {
            Log.e(TAG, "Picture success")
            // If the photo file exists, upload it to storage
//            storage.uploadImage(photoFile, pictureUUID) { _ ->
//                // Once upload is successful, create photo metadata in the database
//                createPhotoMeta(pictureNameByUser, pictureUUID, photoFile.length())
//                // Reset pictureUUID and pictureNameByUser to avoid accidental reuse
//                pictureUUID = ""
//                pictureNameByUser = ""
//
//                // Fetch updated photo metadata after upload
//                fetchPhotoMeta {}
//            }
        } else {
            Log.e(TAG, "Picture file does not exist")
        }
    }
    fun pictureFailure() {
        // Note, the camera intent will only create the file if the user hits accept
        // so I've never seen this called
        pictureUUID = ""
        pictureNameByUser = ""
    }

    // Method to update the last taken picture URI
    fun setLastPictureUri(uri: Uri) {
        _lastPictureUri.value = uri
    }

//    fun glideFetch(uuid: String, imageView: ImageView) {
//        Glide.fetch(storage.uuid2StorageReference(uuid),
//            imageView)
//    }
}
