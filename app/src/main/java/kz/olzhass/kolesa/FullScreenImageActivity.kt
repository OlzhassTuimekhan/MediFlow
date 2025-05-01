package kz.olzhass.kolesa

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.chrisbanes.photoview.PhotoView

class FullScreenImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        val photoView = PhotoView(this)
        setContentView(photoView)

        val imagePath = intent.getStringExtra("image_path")
        if (!imagePath.isNullOrEmpty()) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            photoView.setImageBitmap(bitmap)
        }

        photoView.setOnClickListener {
            finish()
        }
    }
}
