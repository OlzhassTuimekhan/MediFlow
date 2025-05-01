package kz.olzhass.kolesa.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kz.olzhass.kolesa.databinding.FragmentSearchBinding
import kz.olzhass.kolesa.ui.appointment.Appointment
import kz.olzhass.kolesa.ui.appointment.AppointmentsAdapter
import kz.olzhass.kolesa.ui.appointment.PreviousAppointmentsAdapter


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var appointments: MutableList<Appointment>
    private val calendarViewModel: CalendarViewModel by activityViewModels()
    private lateinit var adapter: AppointmentsAdapter
    private lateinit var adapterPrevious: PreviousAppointmentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        // Initialize the adapters for both RecyclerViews
//        adapter = AppointmentsAdapter(mutableListOf())
//        binding.recyclerViewActives.adapter = adapter
//        binding.recyclerViewActives.layoutManager = LinearLayoutManager(context)
//
//        // Use PreviousAppointmentsAdapter for previous appointments
//        adapterPrevious = PreviousAppointmentsAdapter(mutableListOf())
//        binding.recyclerViewPrevious.adapter = adapterPrevious
//        binding.recyclerViewPrevious.layoutManager = LinearLayoutManager(context)
//
//        // Observe active appointments from the ViewModel
//        calendarViewModel.activeAppointments.observe(viewLifecycleOwner, Observer { activeAppointments ->
//            if (activeAppointments != null) {
//                appointments = activeAppointments.toMutableList() // Get active appointments and convert them to a list
//                adapter.notifyDataSetChanged()
//            }
//        })
//
//        // Observe previous appointments from the ViewModel
//        calendarViewModel.previousAppointments.observe(viewLifecycleOwner, Observer { previousAppointments ->
//            if (previousAppointments != null) {
//                appointments.addAll(previousAppointments) // Add previous appointments to the same list
//                adapterPrevious.notifyDataSetChanged()
//            }
//        })
//
//        // Implementing the search functionality
//        binding.searchView1.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                query?.let { filterAppointments(it) }
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                newText?.let { filterAppointments(it) }
//                return true
//            }
//        })
    }

    // Filter appointments for active and previous based on the search query
//    private fun filterAppointments(query: String) {
//        // Filter active appointments
//        val filteredAppointments = appointments.filter { appointment ->
//            appointment.doctor_name.contains(query, ignoreCase = true) ||
//                    appointment.appointment_date.contains(query) ||
//                    appointment.appointment_phone.contains(query)
//        }
//
//        // Update the adapter with filtered results for active appointments
//        adapter = AppointmentsAdapter(filteredAppointments)
//        binding.recyclerViewActives.adapter = adapter
//        adapter.notifyDataSetChanged()
//
//        // Filter previous appointments (if any)
//        val filteredPreviousAppointments = appointments.filter { appointment ->
//            appointment.doctor_name.contains(query, ignoreCase = true) ||
//                    appointment.appointment_date.contains(query) ||
//                    appointment.appointment_phone.contains(query)
//        }
//        adapterPrevious = PreviousAppointmentsAdapter(filteredPreviousAppointments)
//        binding.recyclerViewPrevious.adapter = adapterPrevious
//        adapterPrevious.notifyDataSetChanged()
//    }
}
