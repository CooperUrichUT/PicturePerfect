package edu.utap.pictureperfect.ui.Utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ImageDownloadTask(private val callback: (Bitmap?) -> Unit) : AsyncTask<String, Void, Bitmap?>() {

    override fun doInBackground(vararg params: String): Bitmap? {
        val imageURL = params[0]

        try {
            val url = URL(imageURL)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            return BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e("ImageDownloadTask", "Error downloading image from URL: $imageURL", e)
        }
        return null
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)
        callback.invoke(result)
    }
}
