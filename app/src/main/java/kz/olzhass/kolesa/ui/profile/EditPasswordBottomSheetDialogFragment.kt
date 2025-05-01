package kz.olzhass.kolesa.ui.profile

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kz.olzhass.kolesa.R
import kz.olzhass.kolesa.databinding.FragmentEditPasswordBottomSheetBinding

class EditPasswordBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentEditPasswordBottomSheetBinding? = null
    private val binding get() = _binding!!
    private var onPasswordSaved: ((String) -> Unit)? = null
    var isPasswordVisible = false
    var isPassword1Visible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPasswordBottomSheetBinding.inflate(inflater, container, false)

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
        binding.buttonLogin.setOnClickListener {
            val password = binding.etPassword.text.toString()
            val confirm = binding.etPassword1.text.toString()

            if (password.isEmpty()) {
                binding.etPassword.error = "Please enter a password"
                return@setOnClickListener
            }
            if (confirm.isEmpty()) {
                binding.etPassword1.error = "Please confirm the password"
                return@setOnClickListener
            }
            if (password != confirm) {
                binding.etPassword1.error = "Passwords do not match"
                return@setOnClickListener
            }


            if (password.isBlank()) {
                Toast.makeText(requireContext(), "Enter Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            onPasswordSaved?.invoke(password)
            dismiss()
        }


        with(binding) {
            ivTogglePassword.setOnClickListener {
                // Слушатель для первой иконки
                isPasswordVisible = !isPasswordVisible  // переключаем флаг
                togglePasswordVisibility(
                    binding.etPassword,
                    binding.ivTogglePassword,
                    isPasswordVisible
                )

                // Слушатель для второй иконки
                ivTogglePassword1.setOnClickListener {
                    isPassword1Visible = !isPassword1Visible
                    togglePasswordVisibility(
                        binding.etPassword1,
                        binding.ivTogglePassword1,
                        isPassword1Visible
                    )
                }
            }
        }
    }
    private fun togglePasswordVisibility(
        editText: EditText,
        icon: ImageView,
        isVisible: Boolean
    ) {
        if (isVisible) {
            // Показываем пароль
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            icon.setImageResource(R.drawable.ic_eye_on) // иконка «глаз открыт»
        } else {
            // Скрываем пароль
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            icon.setImageResource(R.drawable.ic_eye_off) // иконка «глаз закрыт»
        }
        // Переводим курсор в конец поля, чтобы не сбивался текст
        editText.setSelection(editText.text.length)
    }

    fun setOnPasswordSavedListener(listener: (String) -> Unit) {
        onPasswordSaved = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}