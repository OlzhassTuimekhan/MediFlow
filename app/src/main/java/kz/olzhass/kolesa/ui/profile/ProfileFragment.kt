package kz.olzhass.kolesa.ui.profile

import LoadingDialogFragment
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kz.olzhass.kolesa.GlobalData
import kz.olzhass.kolesa.R
import kz.olzhass.kolesa.databinding.FragmentProfileBinding
import kz.olzhass.kolesa.ui.login.MainPage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    val client = OkHttpClient()
    private var loadingDialog: LoadingDialogFragment? = null
    private lateinit var viewModel: ProfileViewModel
    private var userId: Int = -1
    private val profileViewModel: ProfileViewModel by activityViewModels()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        Log.d("ProfileFragment", "onCreateView called")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ProfileFragment", "onViewCreated called")

        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        userId = sharedPreferences.getInt("user_id", -1)
        viewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)

        profileViewModel.profileData.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                binding.tvName.text = profile.name
                binding.tvNumber.text = profile.phoneNumber
                binding.tvLocation.text = profile.location
            } else {
                binding.tvErrorMessage.text = "User not Found"
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                binding.tvErrorMessage.text = it
                binding.tvErrorMessage.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                if (loadingDialog == null) {
                    loadingDialog = LoadingDialogFragment()
                    loadingDialog?.show(childFragmentManager, "loading")
                }
            } else {
                if (loadingDialog != null && loadingDialog?.isAdded == true) {
                    loadingDialog?.dismissAllowingStateLoss()
                    loadingDialog = null
                }
            }
        }


        val savedUriString = sharedPreferences.getString("profile_image_uri_$userId", null)
        if (savedUriString != null) {
            binding.imageProfile.setImageURI(Uri.parse(savedUriString))

        } else {
            val savedIndex = sharedPreferences.getInt("profile_image_index_$userId", -1)
            val imageIndex = if (savedIndex != -1) {
                savedIndex
            } else {
                val newIndex = (0..7).random()
                sharedPreferences.edit().putInt("profile_image_index_$userId", newIndex).apply()
                newIndex
            }
            GlobalData.randomImageIndex = imageIndex
            setProfileImage(imageIndex)
        }


//        binding.swipeRefresh.setOnRefreshListener {
//            refreshData(userId)
//        }




        with(binding) {
            btChangePicture.setOnClickListener {

                val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                // Проверяем, есть ли разрешение (оно должно было быть запрошено в MyDocuments)
                if (ContextCompat.checkSelfPermission(requireContext(), galleryPermission) == PackageManager.PERMISSION_GRANTED) {
                    // Разрешение есть – открываем галерею
                    galleryLauncher.launch("image/*")
                } else {
                    Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }

            //                val newIndex = (0..7).random()
//                sharedPreferences.edit().putInt("profile_image_index_$userId", newIndex).apply()
//                setProfileImage(newIndex)
            }
            tvLogOut.setOnClickListener {
                val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
//                    clear()
                    remove("auth_token")
                    GlobalData.token = null
                    apply()
                }
                val intent = Intent(requireActivity(), MainPage::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            tvDocuments.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_profile_to_documentsFragment)
            }
            tvChangeContact.setOnClickListener {
                val bottomSheet = EditNumberBottomSheetDialogFragment()
                bottomSheet.setOnNumberSavedListener { newNumber ->
                    updateContactNumber(userId, newNumber)
                }
                bottomSheet.show(childFragmentManager, "EditNumberBottomSheet")
            }
            btEditName.setOnClickListener {
                val bottomSheet = EditNameBottomSheetDialogFragment()
                bottomSheet.setOnNameSavedListener { newName ->
                    updateName(userId, newName)
                }
                bottomSheet.show(childFragmentManager, "EditNameBottomSheet")
            }
            tvChangePassword.setOnClickListener {
                val bottomSheet = EditPasswordBottomSheetDialogFragment()
                bottomSheet.setOnPasswordSavedListener { newPassword ->
                    updatePassword(userId, newPassword)
                }
                bottomSheet.show(childFragmentManager, "EditPasswordBottomSheet")
            }
            tvSettings.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
            }
            tvMyAppointments.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_calendarFragment)
            }
        }

        // Сохранение глобальных данных и установка изображения
//        GlobalData.randomImageIndex = imageIndex
//        setProfileImage(imageIndex)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun updateContactNumber(userId: Int, newNumber: String) {
        showLoading() // показываем диалог "Loading..."

        // 1. Формируем URL для PUT-запроса
        val url = "http://${GlobalData.ip}:3000/profile/$userId/number"

        // 2. Создаем JSON c новым номером
        val json = JSONObject().apply {
            put("number", newNumber)
        }
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        // 3. Формируем PUT-запрос
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        // 4. Отправляем запрос
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Если запрос не отправился или сервер не ответил
                requireActivity().runOnUiThread {
                    hideLoading()
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    hideLoading()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonResp = JSONObject(responseBody ?: "")
                        if (jsonResp.optBoolean("success", false)) {
                            // Успешно обновили номер
                            Toast.makeText(requireContext(), "Contact number updated!", Toast.LENGTH_LONG).show()

                            // Обновляем UI, если нужно
                            binding.tvNumber.text = newNumber
                        } else {
                            // Сервер вернул success = false, смотрим сообщение
                            val msg = jsonResp.optString("message", "Unknown error")
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Сервер вернул статус 4xx или 5xx
                        Toast.makeText(requireContext(), "Update failed: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun updateName(userId: Int, newName: String) {
        showLoading()

        // Формируем URL для PUT-запроса
        val url = "http://${GlobalData.ip}:3000/profile/$userId/name"

        // Создаем JSON с новым именем
        val json = JSONObject().apply {
            put("name", newName)
        }
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        // Формируем PUT-запрос
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        // Отправляем запрос
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    hideLoading()
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    hideLoading()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonResp = JSONObject(responseBody ?: "")
                        if (jsonResp.optBoolean("success", false)) {
                            // Успешно обновили имя
                            Toast.makeText(requireContext(), "Name updated successfully!", Toast.LENGTH_LONG).show()
                            binding.tvName.text = newName
                        } else {
                            val msg = jsonResp.optString("message", "Unknown error")
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Update failed: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun updatePassword(userId: Int, newPassword: String) {
        showLoading() // показываем диалог "Loading..."

        // Формируем URL для PUT-запроса
        val url = "http://${GlobalData.ip}:3000/client/$userId/password"

        // Создаем JSON с новым паролем
        val json = JSONObject().apply {
            put("password", newPassword)
        }
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        // Формируем PUT-запрос
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        // Отправляем запрос
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    hideLoading()
                    Toast.makeText(requireContext(), "Password update failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    hideLoading()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonResp = JSONObject(responseBody ?: "")
                        if (jsonResp.optBoolean("success", false)) {
                            Toast.makeText(requireContext(), "Password updated successfully!", Toast.LENGTH_LONG).show()
                        } else {
                            val msg = jsonResp.optString("message", "Unknown error")
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Password update failed: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }



    private fun setProfileImage(index: Int) {
        val imageResources = listOf(
            R.drawable.image_1,
            R.drawable.image_2,
            R.drawable.image_3,
            R.drawable.image_4,
            R.drawable.image_5,
            R.drawable.image_6,
            R.drawable.image_7,
            R.drawable.image_8,
        )

        binding.imageProfile.setImageResource(imageResources[index])
    }

    fun showLoading() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialogFragment()
            loadingDialog?.show(parentFragmentManager, "loading")
        }
    }

    fun hideLoading() {
        if (loadingDialog != null && loadingDialog?.isAdded == true) {
            loadingDialog?.dismissAllowingStateLoss()
            loadingDialog = null
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            binding.imageProfile.setImageURI(it)
            // Сохраняем URI в SharedPreferences
            val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("profile_image_uri_$userId", it.toString()).apply()
            // Если нужно постоянное разрешение, сохраняем его (если поддерживается)
            try {
                requireContext().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }



}