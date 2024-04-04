import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import com.nostra13.universalimageloader.core.process.BitmapProcessor
import edu.utap.pictureperfect.R

class GridImageAdapter(
    context: Context,
    private val layoutResource: Int,
    private val mAppend: String,
    private val imgURLs: ArrayList<String>
) : ArrayAdapter<String>(context, layoutResource, imgURLs) {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    private class ViewHolder {
        lateinit var image: ImageView
        lateinit var mProgressBar: ProgressBar
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false)
            holder = ViewHolder()
            holder.mProgressBar = convertView.findViewById(R.id.gridProgressBar)
            holder.image = convertView.findViewById(R.id.gridImageView)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        val imgURL = getItem(position)

        val imageLoader = ImageLoader.getInstance()

        val options = DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.RGB_565)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .postProcessor(BitmapProcessor { bitmap ->
                // Resize the bitmap here
                // For example, resize to 200x200 pixels
                Bitmap.createScaledBitmap(bitmap, 200, 300, false)
            })
            .build()

        imageLoader.displayImage(
            mAppend + imgURL,
            holder.image,
            options,
            object : ImageLoadingListener {
                override fun onLoadingStarted(imageUri: String, view: View) {
                    holder.mProgressBar.visibility = View.VISIBLE
                }

                override fun onLoadingFailed(imageUri: String, view: View, failReason: FailReason) {
                    holder.mProgressBar.visibility = View.GONE
                }

                override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
                    holder.mProgressBar.visibility = View.GONE
                }

                override fun onLoadingCancelled(imageUri: String, view: View) {
                    holder.mProgressBar.visibility = View.GONE
                }
            }
        )

        return convertView!!
    }
}
