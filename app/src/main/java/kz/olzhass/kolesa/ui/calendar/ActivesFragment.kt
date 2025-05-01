package kz.olzhass.kolesa.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kz.olzhass.kolesa.R
import kz.olzhass.kolesa.databinding.FragmentActivesBinding
import kz.olzhass.kolesa.ui.appointment.Appointment
import kz.olzhass.kolesa.ui.appointment.AppointmentsAdapter

class ActivesFragment : Fragment() {

    private var _binding: FragmentActivesBinding? = null
    private val binding get() = _binding!!

    private var allAppointments: List<Appointment> = emptyList()

    private val calendarViewModel: CalendarViewModel by activityViewModels()
    private lateinit var adapter: AppointmentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentActivesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AppointmentsAdapter(requireContext(), mutableListOf()) { appointment ->
            val bundle = Bundle().apply {
                putInt("appointment_id", appointment.appointment_id)
                putString("doctor_name", appointment.doctor_name)
                putString("appointment_date", appointment.appointment_date)
                putString("appointment_time", appointment.appointment_time)
                putString("appointment_reason", appointment.appointment_reason)
            }
            findNavController().navigate(R.id.action_activesFragment_to_appointmentRescheduleFragment, bundle)
        }

        binding.recyclerViewActives.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewActives.adapter = adapter

        calendarViewModel.activeAppointments.observe(viewLifecycleOwner, Observer { appointmentsList ->
            if (appointmentsList != null) {
                binding.tvWelcomeAppointment.visibility = View.GONE
                binding.recyclerViewActives.visibility = View.VISIBLE
                allAppointments = appointmentsList.toMutableList()
                adapter.updateList(allAppointments)
            } else {
                binding.tvWelcomeAppointment.visibility = View.VISIBLE
                binding.recyclerViewActives.visibility = View.GONE
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
