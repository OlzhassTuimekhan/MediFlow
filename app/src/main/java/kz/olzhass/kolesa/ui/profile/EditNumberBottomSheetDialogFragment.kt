package kz.olzhass.kolesa.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kz.olzhass.kolesa.R
import kz.olzhass.kolesa.databinding.FragmentEditNumberBottomSheetBinding

class EditNumberBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentEditNumberBottomSheetBinding? = null
    private val binding get() = _binding!!
    private var onNumberSaved: ((String) -> Unit)? = null





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditNumberBottomSheetBinding.inflate(inflater, container, false)

        return binding.root
    }
    override fun getTheme(): Int = R.style.Theme_MyApp_BottomSheetDialog


    // Шаг 2.3: Здесь настраиваем логику (что делать при нажатии на кнопку)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)

            behavior.isFitToContents = true

            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        // Когда нажмём "Сохранить", берём введённый текст и...
        binding.btnSaveNumber.setOnClickListener {
            val newNumber = binding.etNumber.text.toString().trim()
            if (newNumber.isBlank()) {
                Toast.makeText(requireContext(), "Введите номер", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Вызываем коллбэк, передаем введённый номер
            onNumberSaved?.invoke(newNumber)

            // Закрываем BottomSheet
            dismiss()
        }

        binding.etNumber.addTextChangedListener(object : TextWatcher {
            private var mFormatting = false
            private var mClearing = false
            private var lastLength = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                lastLength = s?.length ?: 0
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (mFormatting || mClearing) {
                    return
                }

                mFormatting = true

                val formattedNumber = formatPhoneNumber(s.toString())
                s?.replace(0, s.length, formattedNumber)

                mFormatting = false
            }

            private fun formatPhoneNumber(number: String): String {
                var digits = number.replace("[^\\d]".toRegex(), "")

                if (digits.startsWith("7")) {
                    digits = "7" + digits.substring(1)
                }

                return when {
                    digits.length <= 1 -> "+" + digits
                    digits.length <= 4 -> "+" + digits.substring(0, 1) + " (" + digits.substring(1)
                    digits.length <= 7 -> "+" + digits.substring(0, 1) + " (" + digits.substring(1, 4) + ") " + digits.substring(4)
                    digits.length <= 9 -> "+" + digits.substring(0, 1) + " (" + digits.substring(1, 4) + ") " + digits.substring(4, 7) + "-" + digits.substring(7)
                    digits.length <= 11 -> "+" + digits.substring(0, 1) + " (" + digits.substring(1, 4) + ") " + digits.substring(4, 7) + "-" + digits.substring(7, 9) + "-" + digits.substring(9)
                    else -> "+" + digits.substring(0, 1) + " (" + digits.substring(1, 4) + ") " + digits.substring(4, 7) + "-" + digits.substring(7, 9) + "-" + digits.substring(9, 11)
                }
            }
        })

    }



    fun setOnNumberSavedListener(listener: (String) -> Unit) {
        onNumberSaved = listener
    }


}
