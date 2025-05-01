package kz.olzhass.kolesa.ui.assistant

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kz.olzhass.kolesa.databinding.FragmentAiAssistantBinding

class AiAssistant : Fragment() {

    private var _binding: FragmentAiAssistantBinding? = null
    private val binding get() = _binding!!
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var viewModel: AiAssistantViewModel
    //
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiAssistantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatAdapter(messages)
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewChat.adapter = adapter

        viewModel = ViewModelProvider(this)[AiAssistantViewModel::class.java]
        viewModel.aiResponse.observe(viewLifecycleOwner) { answer ->
            answer?.let {
                val index = messages.indexOfFirst { !it.isUser && it.isLoading }
                if (index != -1) {
                    messages[index].message = it
                    messages[index].isLoading = false
                    adapter.notifyItemChanged(index)
                } else {
                    messages.add(ChatMessage(it, isUser = false))
                    adapter.notifyItemInserted(messages.size - 1)
                }
                binding.recyclerViewChat.scrollToPosition(messages.size - 1)
            }
        }

        setGradientTextColor(binding.tvWelcomeAi, Color.parseColor("#64B5F6"), Color.parseColor("#E91E63"))

        if (messages.isNotEmpty()) {
            binding.tvWelcomeAi.visibility = View.GONE
            binding.recyclerViewChat.visibility = View.VISIBLE
        } else {
            binding.tvWelcomeAi.visibility = View.VISIBLE
            binding.recyclerViewChat.visibility = View.GONE
        }

        binding.btnSend.setOnClickListener {
            val inputText = binding.etMessage.text.toString().trim()
            if (inputText.isNotEmpty()) {

                if (messages.isEmpty()) {
                    binding.tvWelcomeAi.visibility = View.GONE
                    binding.recyclerViewChat.visibility = View.VISIBLE
                }
                messages.add(ChatMessage(inputText, isUser = true))
                adapter.notifyItemInserted(messages.size - 1)
                binding.etMessage.text.clear()
                binding.recyclerViewChat.scrollToPosition(messages.size - 1)

                // Добавляем placeholder для загрузки ответа от AI
                val placeholder = ChatMessage(message = "", isUser = false, isLoading = true)
                messages.add(placeholder)
                adapter.notifyItemInserted(messages.size - 1)
                binding.recyclerViewChat.scrollToPosition(messages.size - 1)

                // Отправляем запрос через ViewModel
                viewModel.askAI(inputText)
            }
        }

    }

    fun setGradientTextColor(textView: TextView, startColor: Int, endColor: Int) {
        val paint: Paint = textView.paint
        val width: Float = textView.width.toFloat()

        if (width > 0) {
            val shader: Shader = LinearGradient(
                0f, 0f, width, 0f,
                intArrayOf(startColor, endColor),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            textView.setTextColor(Color.WHITE)
            paint.shader = shader
        } else {
            textView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    textView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val currentWidth = textView.width.toFloat()
                    val currentShader: Shader = LinearGradient(
                        0f, 0f, currentWidth, 0f,
                        intArrayOf(startColor, endColor),
                        floatArrayOf(0f, 1f),
                        Shader.TileMode.CLAMP
                    )
                    textView.setTextColor(Color.WHITE)
                    paint.shader = currentShader
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
