package edu.utap.pictureperfect.ui.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.utap.pictureperfect.databinding.FragmentPhotoBinding

class PhotoFragment : Fragment() {
    private var _binding: FragmentPhotoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val TAG = "GalleryFragment"
    @SuppressLint("RestrictedApi")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Photo Fragment Entered")
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }
}