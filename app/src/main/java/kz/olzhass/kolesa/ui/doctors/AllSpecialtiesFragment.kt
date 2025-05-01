package kz.olzhass.kolesa.ui.doctors

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kz.olzhass.kolesa.R
import kz.olzhass.kolesa.databinding.FragmentSpecificDoctorsBinding

class AllSpecialtiesFragment : Fragment() {

    private var _binding: FragmentSpecificDoctorsBinding? = null
    private val binding get() = _binding!!
    private lateinit var specialtiesAdapter: SpecialtiesAdapter
    private lateinit var viewModel: DoctorsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpecificDoctorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireParentFragment()).get(DoctorsViewModel::class.java)

        specialtiesAdapter = SpecialtiesAdapter(emptyList()) { specialty ->
            showDoctorsFragment(specialty.id)
        }
        Log.d("AllSpecialities", "specialtiesAdapter created")

        binding.recyclerViewSpecialties.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = specialtiesAdapter
        }

        viewModel.allSpecialties.observe(viewLifecycleOwner) { specialtiesList ->
            specialtiesAdapter.updateData(specialtiesList)
        }
        binding.searchView1.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterSpecialties(newText)
                return true
            }
        })
    }

    private fun showDoctorsFragment(specialtyId: Int) {
        val bundle = Bundle().apply {
            putInt("specialtyId", specialtyId)
        }
        findNavController().navigate(R.id.action_allSpecialties_to_doctorsBySpecialty, bundle)
    }

    private fun filterSpecialties(query: String?) {
        if (query.isNullOrEmpty()) {
            viewModel.allSpecialties.value?.let { specialtiesAdapter.updateData(it) }
            return
        }

        val filteredList = viewModel.allSpecialties.value?.filter { specialty ->
            specialty.name.contains(query, ignoreCase = true)
        } ?: emptyList()

        specialtiesAdapter.updateData(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}