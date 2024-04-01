package edu.utap.pictureperfect.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is profile Fragment"
    }
    val text: LiveData<String> = _text
    private var _url: String = ""

    // Getter for url
    fun getUrl(): String {
        return _url
    }

    // Setter for url
    fun setUrl(url: String) {
        _url = url
    }
}
