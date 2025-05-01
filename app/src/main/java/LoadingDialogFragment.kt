import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import kz.olzhass.kolesa.R

class LoadingDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Инфлейтим наш layout для диалога
        return inflater.inflate(R.layout.dialog_loading, container, false)
    }

    override fun onStart() {
        super.onStart()
        // Задаём размер диалога по содержимому
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // Убираем заголовок
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Делаем фон диалога прозрачным
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Если хотите убрать затемнение фона за диалогом:
         dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        return dialog
    }

}
