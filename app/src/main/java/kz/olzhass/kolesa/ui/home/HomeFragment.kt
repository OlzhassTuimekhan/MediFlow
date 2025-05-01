package kz.olzhass.kolesa.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kz.olzhass.kolesa.databinding.FragmentHomeBinding
import kz.olzhass.kolesa.ui.documents.DocumentData
import kz.olzhass.kolesa.ui.documents.DocumentsAdapter
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var documentsAdapter: DocumentsAdapter
    private var userId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        userId = sharedPreferences.getInt("user_id", -1)

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        homeViewModel.fetchDocumentDetails(userId)

        val sampleDocuments = mutableListOf(
            DocumentData("Dr.", null, "Specialization", "2024-01-01"),
        )
        documentsAdapter = DocumentsAdapter(sampleDocuments)
        binding.recyclerViewDocuments.adapter = documentsAdapter
        binding.recyclerViewDocuments.layoutManager = LinearLayoutManager(requireContext())

        Log.d("HomeFragment", "Setting up observer")
        homeViewModel.documentData.observe(viewLifecycleOwner) { documents ->
            Log.d("HomeFragment", "Observed documents: $documents")
            if (!documents.isNullOrEmpty()) {
                binding.tvWelcomeAi.visibility = View.GONE
                binding.recyclerViewDocuments.visibility = View.VISIBLE
                documentsAdapter = DocumentsAdapter(documents)
                binding.recyclerViewDocuments.adapter = documentsAdapter
                binding.recyclerViewDocuments.visibility = View.VISIBLE
            } else {
                binding.tvWelcomeAi.visibility = View.VISIBLE
                binding.recyclerViewDocuments.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
