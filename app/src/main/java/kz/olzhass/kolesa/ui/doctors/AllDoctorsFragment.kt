package kz.olzhass.kolesa.ui.doctors

import LoadingDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kz.olzhass.kolesa.databinding.FragmentAllDoctorsBinding
import okhttp3.OkHttpClient

class AllDoctorsFragment : Fragment() {

    private var _binding: FragmentAllDoctorsBinding? = null
    private val binding get() = _binding!!
    private lateinit var doctorAdapter: DoctorAdapter

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _doctorsData = MutableLiveData<List<Doctor>>()
    val doctorsData: LiveData<List<Doctor>> get() = _doctorsData
    private var loadingDialog: LoadingDialogFragment? = null
    private lateinit var viewModel: DoctorsViewModel

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Важно: использовать правильный биндинг для нового XML
        _binding = FragmentAllDoctorsBinding.inflate(inflater, container, false)
        return binding.root // Это возвращает корневой элемент в разметке
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireParentFragment()).get(DoctorsViewModel::class.java)

        binding.recyclerViewDoctors.layoutManager = LinearLayoutManager(requireContext())
        doctorAdapter = DoctorAdapter(emptyList())
        binding.recyclerViewDoctors.adapter = doctorAdapter
        val searchView = binding.searchView1

        searchView.setOnClickListener {
            searchView.isIconified = false
        }

        searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterDoctors(newText)
                return true
            }
        })

        viewModel.allDoctors.observe(viewLifecycleOwner) { doctorsList ->
            doctorAdapter.updateDoctors(doctorsList)
        }
    }

    fun filterDoctors(query: String?) {
        val filteredList = viewModel.allDoctors.value?.filter { doctor ->
            val searchQuery = query?.toLowerCase() ?: ""
            doctor.nameSurname.toLowerCase().contains(searchQuery) ||
                    doctor.phone.contains(searchQuery)
        }
        doctorAdapter.updateDoctors(filteredList ?: emptyList())
    }


    fun showLoading() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialogFragment()
            loadingDialog?.show(parentFragmentManager, "loading")
        }
    }

    fun hideLoading() {
        if (loadingDialog != null && loadingDialog?.isAdded == true) {
            loadingDialog?.dismissAllowingStateLoss()
            loadingDialog = null
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}