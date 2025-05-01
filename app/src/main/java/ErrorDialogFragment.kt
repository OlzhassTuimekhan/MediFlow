import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import kz.olzhass.kolesa.R

class ErrorDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_MESSAGE = "arg_message"

        /**
         * Создаём новый экземпляр диалога, передавая сообщение через аргументы
         */
        fun newInstance(message: String): ErrorDialogFragment {
            val fragment = ErrorDialogFragment()
            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Создаём "чистый" диалог без заголовка
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_error, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val message = arguments?.getString(ARG_MESSAGE) ?: "Something went wrong"

        val tvErrorMessage = view.findViewById<TextView>(R.id.tvErrorMessage)
        val btnOk = view.findViewById<Button>(R.id.btnOk)

        tvErrorMessage.text = message

        // Закрываем диалог по нажатию "OK"
        btnOk.setOnClickListener {
            dismiss()
        }
    }

    /**
     * Опционально: если хотите прозрачный фон или отключить затемнение,
     * можно настроить в onStart или через стили.
     */
    override fun onStart() {
        super.onStart()
        // Пример: делаем размер окна "по контенту"
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
