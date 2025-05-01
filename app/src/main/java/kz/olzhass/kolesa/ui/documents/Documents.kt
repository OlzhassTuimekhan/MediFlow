package kz.olzhass.kolesa.ui.documents

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kz.olzhass.kolesa.R
import kz.olzhass.kolesa.databinding.FragmentDocumentsBinding
import kz.olzhass.kolesa.ui.profile.ProfileViewModel


class Documents : Fragment() {

    private var _binding: FragmentDocumentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var documentsAdapter: DocumentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sampleDocuments = mutableListOf(
            DocumentData("Dr.", null, "Specialization", "2024-01-01"),
        )
        documentsAdapter = DocumentsAdapter(sampleDocuments)
        binding.recyclerViewDocuments.adapter = documentsAdapter
        binding.recyclerViewDocuments.layoutManager = LinearLayoutManager(requireContext())

        Log.d("DocumentsFragment", "Setting up observer")
        profileViewModel.documentData.observe(viewLifecycleOwner) { documents ->
            Log.d("DocumentsFragment", "Observed documents: $documents")
            if (documents != null) {
                binding.tvWelcomeAi.visibility = View.GONE
                binding.recyclerViewDocuments.visibility = View.VISIBLE
                documentsAdapter = DocumentsAdapter(documents)
                binding.recyclerViewDocuments.adapter = documentsAdapter
                binding.recyclerViewDocuments.layoutManager = LinearLayoutManager(requireContext())
            } else {
                binding.tvWelcomeAi.visibility = View.VISIBLE
                binding.recyclerViewDocuments.visibility = View.GONE
            }
        }

        binding.imageButton.setOnClickListener {
            findNavController().navigate(R.id.action_documentsFragment_to_profileFragment)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}