package kz.olzhass.kolesa.ui.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kz.olzhass.kolesa.databinding.FragmentPreviousBinding
import kz.olzhass.kolesa.ui.appointment.Appointment
import kz.olzhass.kolesa.ui.appointment.PreviousAppointmentsAdapter


class PreviousFragment : Fragment() {

    private var _binding: FragmentPreviousBinding? = null
    private val binding get() = _binding!!
    private lateinit var appointments: MutableList<Appointment>
    private lateinit var adapter: PreviousAppointmentsAdapter
    private val calendarViewModel: CalendarViewModel by activityViewModels()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPreviousBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarViewModel.previousAppointments.observe(viewLifecycleOwner, Observer { appointmentsList ->
            if (appointmentsList != null) {
                binding.tvWelcomeAppointment.visibility = View.GONE
                binding.recyclerViewPrevious.visibility = View.VISIBLE
                appointments = appointmentsList.toMutableList()
                Log.d("ActivesFragment", "Appointments size: ${appointments.size}")

                adapter = PreviousAppointmentsAdapter(requireContext(), appointments)
                binding.recyclerViewPrevious.adapter = adapter
                binding.recyclerViewPrevious.layoutManager = LinearLayoutManager(context)
            } else {
                binding.tvWelcomeAppointment.visibility = View.VISIBLE
                binding.recyclerViewPrevious.visibility = View.GONE
            }
        })
    }


}

