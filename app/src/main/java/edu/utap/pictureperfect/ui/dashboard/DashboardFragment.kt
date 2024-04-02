package edu.utap.pictureperfect.ui.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.databinding.FragmentDashboardBinding
import edu.utap.pictureperfect.ui.Utils.SectionsPagerAdapter

class DashboardFragment : Fragment() {
    private val TAG = "DaashboardFragment"
    private var _binding: FragmentDashboardBinding? = null
    private lateinit var tabLayout: TabLayout
    private lateinit var mViewPager: ViewPager
    private var galleryFragment = GalleryFragment()
    private var photoFragment = PhotoFragment()


    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("StringFormatInvalid", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
//        setupViewPager()


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set custom title for the action bar
        requireActivity().title = "Dashboard"

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


//    private fun setupViewPager() {
//        tabLayout = binding.tabsBottom
//
//        // Add your desired tab names
//        tabLayout.addTab(tabLayout.newTab().setText("Gallery"))
//        tabLayout.addTab(tabLayout.newTab().setText("Photo"))
//
//        // Set a listener for tab selection
//        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                // When a tab is selected, replace the fragment
//                when (tab?.position) {
//                    0 -> replaceFragment(galleryFragment)
//                    1 -> replaceFragment(photoFragment)
//                }
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {}
//            override fun onTabReselected(tab: TabLayout.Tab?) {}
//        })
//
//        // Select the first tab by default
//        tabLayout.getTabAt(0)?.select()
//    }
//
//    // Function to replace fragment in viewpager_container
//
//    private fun replaceFragment(fragment: Fragment) {
//        val transaction = childFragmentManager.beginTransaction()
//        transaction.replace(R.id.fragment_container, fragment)
//        transaction.commit()
//    }


}

