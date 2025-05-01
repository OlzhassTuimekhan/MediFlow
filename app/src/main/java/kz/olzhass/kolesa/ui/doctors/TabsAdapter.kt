package kz.olzhass.kolesa.ui.doctors

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabsAdapter(fragment: Fragment, private val viewModel: DoctorsViewModel) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2 // Две вкладки

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllSpecialtiesFragment()
            else -> AllDoctorsFragment()
        }
    }
}
