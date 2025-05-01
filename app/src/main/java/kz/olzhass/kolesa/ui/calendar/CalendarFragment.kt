package kz.olzhass.kolesa.ui.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kz.olzhass.kolesa.R
import kz.olzhass.kolesa.databinding.FragmentCalendarBinding

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private lateinit var viewModel: CalendarViewModel
    private val calendarViewModel: CalendarViewModel by activityViewModels()
    private var userId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(CalendarViewModel::class.java)
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        userId = sharedPreferences.getInt("user_id", -1)

        Log.d("FETCHING", "FETCHING Appointments STARTED: $userId")
        calendarViewModel.fetchAppointments(userId)


        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout1)
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager1)

        val adapter = CalendarTabsAdapter(this, viewModel)
        viewPager.adapter = adapter



        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Actives"
                1 -> tab.text = "Previous"
            }
        }.attach()


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}