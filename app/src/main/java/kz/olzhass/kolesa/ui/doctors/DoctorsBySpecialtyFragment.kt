package kz.olzhass.kolesa.ui.doctors

import LoadingDialogFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kz.olzhass.kolesa.GlobalData
import kz.olzhass.kolesa.databinding.FragmentDoctorsBySpecialtyBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class DoctorsBySpecialtyFragment : Fragment() {

    private var _binding: FragmentDoctorsBySpecialtyBinding? = null
    private val binding get() = _binding!!
    private lateinit var doctorsAdapter: DoctorAdapterSpecialist
    private var loadingDialog: LoadingDialogFragment? = null

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage
    private val client = OkHttpClient()

    private lateinit var viewModel: DoctorsViewModel

    companion object {
        private const val ARG_SPECIALTY_ID = "specialty_id"

        fun newInstance(specialtyId: Int): DoctorsBySpecialtyFragment {
            val fragment = DoctorsBySpecialtyFragment()
            val args = Bundle()
            args.putInt(ARG_SPECIALTY_ID, specialtyId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("DOCTORS_BY_SPECIALTY", "onCreateView called")
        _binding = FragmentDoctorsBySpecialtyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireParentFragment()).get(DoctorsViewModel::class.java)

        var specialtyId = arguments?.getInt(ARG_SPECIALTY_ID) ?: 0
        arguments?.let {
            specialtyId = it.getInt("specialtyId", 0)
        }
        Log.d("DoctorsBySpecialty", "Specialty ID: $specialtyId")
        doctorsAdapter = DoctorAdapterSpecialist(emptyList())
        binding.recyclerViewDoctors.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = doctorsAdapter
        }
        binding.imageButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // Попытка получить данные из ViewModel
        val specialtyName = viewModel.allSpecialties.value?.find { it.id == specialtyId }?.name
        if (specialtyName != null) {
            val filteredDoctors = viewModel.allDoctors.value?.filter { doctor ->
                doctor.specialties?.contains(specialtyName) == true
            } ?: emptyList()

            if (filteredDoctors.isNotEmpty()) {
                doctorsAdapter.updateDoctors(filteredDoctors)
            } else {
                fetchDoctors(specialtyId) // Если в ViewModel нет данных, делаем запрос к серверу
            }
        } else {
            fetchDoctors(specialtyId) // Если specialtyName null, делаем запрос к серверу
        }
    }

    private fun fetchDoctors(specialtyId: Int) {
        showLoading()
        val url = "http://${GlobalData.ip}:3000/doctors-by-specialty/$specialtyId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _errorMessage.postValue("Doctors fetch failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    try {
                        val jsonResponse = JSONObject(responseBody ?: "{}")
                        if (jsonResponse.getBoolean("success")) {
                            val doctorsArray = jsonResponse.getJSONArray("doctors")
                            val doctorsList = mutableListOf<Doctor>()
                            for (i in 0 until doctorsArray.length()) {
                                val doctorJson = doctorsArray.getJSONObject(i)
                                val doctor = Doctor(
                                    id = doctorJson.getInt("id"),
                                    nameSurname = doctorJson.getString("name"),
                                    phone = doctorJson.getString("phone"),
                                    specialties = doctorJson.getString("specialty"),
                                    services = doctorJson.getString("services")
                                )
                                doctorsList.add(doctor)
                            }
                            Log.d("DoctorsBySpecialty", "Doctors fetched: ${doctorsList.get(0)}")
                            requireActivity().runOnUiThread {
                                doctorsAdapter.updateDoctors(doctorsList)
                            }
                            hideLoading()
                        } else {
                            _errorMessage.postValue("Doctors fetch failed: " + jsonResponse.getString("message"))
                            hideLoading()
                        }
                    } catch (e: Exception) {
                        _errorMessage.postValue("Error parsing doctors data: ${e.message}")
                        hideLoading()
                    }
                } else {
                    _errorMessage.postValue("Doctors fetch failed: ${response.message}")
                    hideLoading()
                }
            }
        })
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