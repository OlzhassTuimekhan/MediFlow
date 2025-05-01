package kz.olzhass.kolesa.ui.assistant

data class ChatMessage(
    var message: String = "",
    val isUser: Boolean,
    var isLoading: Boolean = false
)

