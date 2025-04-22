package com.example.aplikasideteksikanker.view

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aplikasideteksikanker.R
import com.example.aplikasideteksikanker.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    @SuppressLint("UseKtx")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            view.setPadding(0, top, 0, 0)
            insets
        }

        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)
        val imageUri = Uri.parse(imageUriString)
        val predictionResult = intent.getStringExtra("predictionResult")
        val confidenceScore = intent.getFloatExtra("confidenceScore", 0.0f)

        val percentage = (confidenceScore * 100).toInt()
        val formattedPercentage = "$percentage%"
        @Suppress("DEPRECATION") val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        binding.resultImage.setImageBitmap(bitmap)

        binding.resultText.text = getString(R.string.result, predictionResult, formattedPercentage)
    }

    companion object {
        const val EXTRA_IMAGE_URI = "imageUri"
        const val EXTRA_RESULT = "predictionResult"
        const val EXTRA_SCORE = "confidenceScore"
    }
}