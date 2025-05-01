package kz.olzhass.kolesa.ui.documents

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kz.olzhass.kolesa.FullScreenImageActivity
import kz.olzhass.kolesa.R
import kz.olzhass.kolesa.databinding.ItemDocumentBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class DocumentsAdapter(private val documents: List<DocumentData>) :
    RecyclerView.Adapter<DocumentsAdapter.DocumentViewHolder>() {

    inner class DocumentViewHolder(val binding: ItemDocumentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val binding = ItemDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DocumentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val item = documents[position]

        holder.binding.apply {
            tvDoctorName.text = ("Given by: Dr. " + item.doctorName) ?: "Неизвестно"
            tvDocumentType.text = item.documentType ?: "Тип не указан"
            tvDocumentDate.text = item.documentDate ?: "Дата не указана"

            if (item.documentData != null) {
                try {
                    val bitmap = BitmapFactory.decodeByteArray(item.documentData, 0, item.documentData.size)
                    Log.d("DocumentsAdapter", "Bitmap success for ${item.doctorName}")

                    if (bitmap != null) {
                        documentPreview.setImageBitmap(bitmap)
                        documentPreview.visibility = View.VISIBLE
                        documentData.visibility = View.GONE
                    } else {
                        documentPreview.setImageResource(R.drawable.ic_hospital)
                        documentPreview.visibility = View.VISIBLE
                        documentData.text = "Не удалось создать изображение"
                        documentData.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    Log.e("DocumentsAdapter", "Не удалось загрузить изображение: ${e.message}")
                    documentPreview.setImageResource(R.drawable.ic_hospital)
                    documentPreview.visibility = View.VISIBLE
                    documentData.text = "Ошибка при загрузке изображения"
                    documentData.visibility = View.VISIBLE
                }
            } else {
                documentPreview.setImageResource(R.drawable.ic_hospital)
                documentPreview.visibility = View.VISIBLE
                documentData.text = "Документ не найден"
                documentData.visibility = View.VISIBLE
            }
            documentPreview.setOnClickListener {
                try {
                    val file = File.createTempFile("document_", ".jpg", it.context.cacheDir)
                    file.writeBytes(item.documentData ?: return@setOnClickListener)

                    val intent = Intent(it.context, FullScreenImageActivity::class.java)
                    intent.putExtra("image_path", file.absolutePath)
                    it.context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("DocumentsAdapter", "Error writing image to temp file: ${e.message}")
                }
            }
            btnDownload.setOnClickListener {
                downloadDocument(item, it.context)
            }


        }
    }


    fun downloadDocument(document: DocumentData, context: Context) {
        // Проверяем наличие данных документа
        val data = document.documentData
        if (data == null || data.isEmpty()) {
            Toast.makeText(context, "Документ пустой или недоступен", Toast.LENGTH_SHORT).show()
            return
        }

        // Преобразуем MIME-тип в корректное расширение. Например, если документ.documentType содержит "image/jpeg"
        var extension = document.documentType ?: "jpg"
        // Если расширение содержит "/", значит это MIME-тип и его нужно преобразовать
        if (extension.contains("/")) {
            extension = when (extension.lowercase()) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "application/pdf" -> "pdf"
                else -> "dat"  // Подставьте значение по умолчанию или реализуйте другие случаи
            }
        }

        // Формируем имя файла с уникальным идентификатором
        val fileName = "document_${System.currentTimeMillis()}.$extension"

        // Получаем директорию для загрузок из внешнего хранилища
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (downloadsDir == null) {
            Toast.makeText(context, "Не удалось найти директорию для загрузок", Toast.LENGTH_SHORT).show()
            return
        }

        // Убедимся, что директория существует, если нет — создаем ее
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        val file = File(downloadsDir, fileName)

        try {
            FileOutputStream(file).use { fos ->
                fos.write(data)
            }
            Toast.makeText(context, "Документ сохранён: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            Log.d("DocumentsAdapter", "Документ сохранён: ${file.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("DocumentsAdapter", "Ошибка при сохранении документа: ${e.message}")
            Toast.makeText(context, "Ошибка при сохранении: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    override fun getItemCount(): Int = documents.size
}

