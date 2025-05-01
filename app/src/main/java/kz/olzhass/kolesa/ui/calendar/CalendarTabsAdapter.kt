package kz.olzhass.kolesa.ui.calendar

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter


class CalendarTabsAdapter(
    fragment: Fragment,
    private val viewModel: CalendarViewModel
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ActivesFragment()
            1 -> PreviousFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
