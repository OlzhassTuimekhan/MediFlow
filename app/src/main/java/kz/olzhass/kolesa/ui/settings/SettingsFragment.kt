package kz.olzhass.kolesa.ui.settings


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kz.olzhass.kolesa.databinding.FragmentSettingsBinding
import kz.olzhass.kolesa.R


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val languages = listOf("English", "Русский", "Қазақша")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languages
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerLanguage.adapter = adapter

        // Чтение сохранённого значения языка из SharedPreferences
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val currentLang = prefs.getString("app_language", "en")
        // Устанавливаем выбранное значение в Spinner
        val selectedIndex = when (currentLang) {
            "ru" -> 1
            "kk" -> 2
            else -> 0
        }
        binding.spinnerLanguage.setSelection(selectedIndex)

        binding.spinnerLanguage.setSelection(selectedIndex)

        binding.btnApply.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.dialog_title_confirmation))
                .setMessage(getString(R.string.dialog_message_are_you_sure))
                .setPositiveButton(getString(R.string.dialog_button_yes)) { dialog, _ ->
                    val selectedLanguage = binding.spinnerLanguage.selectedItem as String
                    val langCode = when (selectedLanguage) {
                        "Русский" -> "ru"
                        "Қазақша" -> "kk"
                        else -> "en"
                    }
                    setLocale(langCode)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.dialog_button_no)) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
        binding.imageButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_profileFragment)
        }
    }

    private fun setLocale(language: String) {
        // Сохраняем выбранный язык в SharedPreferences
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", language).apply()

        // Перезапускаем Activity для применения изменений
        activity?.recreate()
    }

    private fun restartApp() {
        // Получаем Intent для запуска главного Activity (Launcher)
        val intent = requireContext().packageManager.getLaunchIntentForPackage(requireContext().packageName)
        // Добавляем флаг для очистки стека Activity
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        // Запускаем Activity
        intent?.let { startActivity(it) }
        // Завершаем все Activity в текущем таске
        activity?.finishAffinity()
        // Завершаем процесс (опционально)
        Runtime.getRuntime().exit(0)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
