package com.example.experiment

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.experiment.pojo.VO.NewsProfileVO
import com.example.experiment.pojo.VO.NewsDetailsVO
import java.util.UUID

class SubmissionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submission)

        val rootView = findViewById<android.view.View>(R.id.submissionRoot)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val titleInput = findViewById<EditText>(R.id.etTitle)
        val authorInput = findViewById<EditText>(R.id.etAuthor)
        val publishTimeInput = findViewById<EditText>(R.id.etPublishTime)
        val contentInput = findViewById<EditText>(R.id.etContent)
        val commentsInput = findViewById<EditText>(R.id.etComments)
        val imageLocalPathInput = findViewById<EditText>(R.id.etImageLocalPath)
        val imageUrlInput = findViewById<EditText>(R.id.etImageUrl)
        val submitButton = findViewById<Button>(R.id.btnSubmitReview)

        submitButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val author = authorInput.text.toString().trim()
            val publishTime = publishTimeInput.text.toString().trim()
            val content = contentInput.text.toString().trim()
            val commentsText = commentsInput.text.toString().trim()
            val imageLocalPath = imageLocalPathInput.text.toString().trim().ifBlank { null }
            val imageUrl = imageUrlInput.text.toString().trim().ifBlank { null }

            if (title.isBlank() || author.isBlank() || publishTime.isBlank() || content.isBlank()) {
                Toast.makeText(this, R.string.submission_validation_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val comments = commentsText
                .lines()
                .map { line -> line.trim() }
                .filter { line -> line.isNotEmpty() }

            AlertDialog.Builder(this)
                .setTitle(R.string.submission_confirm_title)
                .setMessage(R.string.submission_confirm_message)
                .setNegativeButton(R.string.submission_confirm_cancel, null)
                .setPositiveButton(R.string.submission_confirm_submit) { _, _ ->
                    val previewNewsProfileVO = NewsProfileVO(
                        title = title,
                        profile = content,
                        imageLocalPath = imageLocalPath,
                        imageUrl = imageUrl
                    )

                    val previewNewsDetailsVO = NewsDetailsVO(
                        id = UUID.randomUUID().toString(),
                        title = previewNewsProfileVO.title,
                        author = author,
                        publishTime = publishTime,
                        content = previewNewsProfileVO.profile,
                        comments = comments,
                        imageLocalPath = previewNewsProfileVO.imageLocalPath,
                        imageUrl = previewNewsProfileVO.imageUrl
                    )

                    handleSubmit(previewNewsProfileVO, previewNewsDetailsVO)
                    clearForm(
                        titleInput,
                        authorInput,
                        publishTimeInput,
                        contentInput,
                        commentsInput,
                        imageLocalPathInput,
                        imageUrlInput
                    )
                }
                .show()
        }
    }

    private fun handleSubmit(newsProfileVO: NewsProfileVO, details: NewsDetailsVO) {
        // Placeholder for future API integration.
        val message = getString(
            R.string.submission_success,
            details.title,
            details.comments.size
        )
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun clearForm(vararg fields: EditText) {
        fields.forEach { field -> field.text?.clear() }
    }
}

