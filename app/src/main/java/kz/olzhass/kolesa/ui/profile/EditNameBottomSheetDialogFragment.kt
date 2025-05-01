package kz.olzhass.kolesa.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kz.olzhass.kolesa.R
import kz.olzhass.kolesa.databinding.FragmentEditNameBottomSheetBinding

class EditNameBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentEditNameBottomSheetBinding? = null
    private val binding get() = _binding!!
    private var onNameSaved: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditNameBottomSheetBinding.inflate(inflater, container, false)

        return binding.root
    }
    override fun getTheme(): Int = R.style.Theme_MyApp_BottomSheetDialog


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)

            behavior.isFitToContents = true

            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        binding.btnSaveName.setOnClickListener {
            val newNumber = binding.etName.text.toString().trim()
            if (newNumber.isBlank()) {
                Toast.makeText(requireContext(), "Enter Name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            onNameSaved?.invoke(newNumber)
            dismiss()
        }
    }

    fun setOnNameSavedListener(listener: (String) -> Unit) {
        onNameSaved = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}