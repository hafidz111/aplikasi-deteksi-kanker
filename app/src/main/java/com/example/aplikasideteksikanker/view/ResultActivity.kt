package com.example.aplikasideteksikanker.view

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikasideteksikanker.R
import com.example.aplikasideteksikanker.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)
        val imageUri = Uri.parse(imageUriString)
        val predictionResult = intent.getStringExtra("predictionResult")
        val confidenceScore = intent.getFloatExtra("confidenceScore", 0.0f)

        val percentage = (confidenceScore * 100).toInt()
        val formattedPercentage = "$percentage%"
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        binding.resultImage.setImageBitmap(bitmap)

        binding.resultText.text = getString(R.string.result, predictionResult, formattedPercentage)
    }

    companion object {
        const val EXTRA_IMAGE_URI = "imageUri"
        const val EXTRA_RESULT = "predictionResult"
        const val EXTRA_SCORE = "confidenceScore"
    }
}