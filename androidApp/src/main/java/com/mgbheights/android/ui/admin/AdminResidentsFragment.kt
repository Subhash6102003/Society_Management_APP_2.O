package com.mgbheights.android.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.databinding.FragmentAdminResidentsBinding
import com.mgbheights.android.ui.adapter.UserAdapter
import com.mgbheights.shared.domain.model.User

class AdminResidentsFragment : Fragment() {

    private var _binding: FragmentAdminResidentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: UserAdapter
    private var residentList = mutableListOf<User>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminResidentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadResidents()
        setupSearch()
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter { user ->
            val action = AdminResidentsFragmentDirections.actionAdminResidentsToAdminResidentDetail(user.id)
            findNavController().navigate(action)
        }
        binding.rvResidents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResidents.adapter = adapter
    }

    private fun loadResidents() {
        binding.progressBar.visibility = View.VISIBLE
        FirebaseFirestore.getInstance().collection("residents")
            .get()
            .addOnSuccessListener { querySnapshot ->
                binding.progressBar.visibility = View.GONE
                residentList.clear()
                for (doc in querySnapshot.documents) {
                    val user = doc.toObject(com.mgbheights.shared.domain.model.User::class.java)?.copy(id = doc.id)
                    if (user != null) residentList.add(user)
                }
                adapter.submitList(residentList)
                binding.tvEmpty.visibility = if (residentList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = if (newText.isNullOrBlank()) {
                    residentList
                } else {
                    residentList.filter { 
                        it.name.contains(newText, ignoreCase = true) || 
                        it.flatNumber.contains(newText, ignoreCase = true) 
                    }
                }
                adapter.submitList(filtered)
                return true
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
