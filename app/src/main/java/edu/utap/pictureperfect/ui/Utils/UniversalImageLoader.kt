// UniversalImageLoader.kt
package edu.utap.pictureperfect.ui.Utils

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener

class UniversalImageLoader(private val mContext: Context) {

    private val defaultImage = android.R.drawable.ic_menu_gallery

    fun getConfig(): ImageLoaderConfiguration {
        val defaultOptions = DisplayImageOptions.Builder()
            .showImageOnLoading(defaultImage)
            .showImageForEmptyUri(defaultImage)
            .showImageOnFail(defaultImage)
            .considerExifParams(true)
            .cacheOnDisk(true)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .resetViewBeforeLoading(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .displayer(FadeInBitmapDisplayer(300))
            .build()

        return ImageLoaderConfiguration.Builder(mContext)
            .defaultDisplayImageOptions(defaultOptions)
            .memoryCache(WeakMemoryCache())
            .diskCacheSize(100 * 1024 * 1024)
            .build()
    }

    fun setImage(imgURL: String, image: ImageView, mProgressBar: ProgressBar?, append: String) {
        val imageLoader = ImageLoader.getInstance()
        imageLoader.displayImage(append + imgURL, image, object : ImageLoadingListener {
            override fun onLoadingStarted(imageUri: String, view: View) {
                mProgressBar?.visibility = View.VISIBLE
            }

            override fun onLoadingFailed(imageUri: String, view: View, failReason: FailReason) {
                mProgressBar?.visibility = View.GONE
            }

            override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
                mProgressBar?.visibility = View.GONE
            }

            override fun onLoadingCancelled(imageUri: String, view: View) {
                mProgressBar?.visibility = View.GONE
            }
        })
    }
}
