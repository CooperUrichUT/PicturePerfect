package edu.utap.pictureperfect.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment
import edu.utap.pictureperfect.R
import edu.utap.pictureperfect.databinding.FragmentSearchBinding
import edu.utap.pictureperfect.ui.Models.User
import edu.utap.pictureperfect.ui.Utils.FirebaseMethods
import edu.utap.pictureperfect.ui.Utils.UserSearchAdapter

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private lateinit var firebaseMethods: FirebaseMethods
    private lateinit var userListView: ListView
    private lateinit var buttonSearch: Button
    private lateinit var searchUser: EditText
    private lateinit var filteredUserList: List<User>


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize FirebaseMethods
        firebaseMethods = FirebaseMethods()

        userListView = root.findViewById(R.id.usersListView)
        searchUser = root.findViewById(R.id.editTextSearch)
        buttonSearch = root.findViewById(R.id.buttonSearch)

        // Call getAllUsers method and populate the list view
        firebaseMethods.getAllUsers { userList ->
            if (userList != null) {
                filteredUserList = userList
            }
            if (userList != null) {
                // Create and set adapter
                val adapter = UserSearchAdapter(requireContext(), R.layout.layout_search_user, userList)
                userListView.adapter = adapter
            } else {
                // Handle error scenario
                println("Failed to retrieve users")
            }
        }

        buttonSearch.setOnClickListener {
            // Get the search query entered by the user
            val searchText = searchUser.text.toString().trim()

            // Filter the user list based on the search query
            val filteredUserList = filteredUserList?.filter { user ->
                user.username?.contains(searchText, ignoreCase = true) ?: false
            }

            // Update the ListView with the filtered user list
            if (filteredUserList != null) {
                val adapter = UserSearchAdapter(requireContext(), R.layout.layout_search_user, filteredUserList)
                userListView.adapter = adapter
            } else {
                // Handle scenario where no users match the search query
                println("No users found matching the search query")
            }
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
