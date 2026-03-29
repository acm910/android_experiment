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
import com.example.experiment.data.NewsDbHelper
import com.example.experiment.pojo.VO.NewsDetailsVO
import com.example.experiment.pojo.entity.News
import java.util.UUID

class SubmissionActivity : AppCompatActivity() {
    private lateinit var dbHelper: NewsDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submission)
        dbHelper = NewsDbHelper(this)

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
                    val previewNewsDetailsVO = NewsDetailsVO(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        author = author,
                        publishTime = publishTime,
                        content = content,
                        comments = comments,
                        imageLocalPath = imageLocalPath,
                        imageUrl = imageUrl
                    )

                    handleSubmit(previewNewsDetailsVO)
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

    private fun handleSubmit(details: NewsDetailsVO) {
        // 投稿先本地入库，主页收到 RESULT_OK 后会按数据库重新拉取。
        val inserted = dbHelper.insertOrReplace(
            News(
                id = details.id,
                title = details.title,
                author = details.author,
                publishTime = details.publishTime,
                profile = details.content.take(80),
                content = details.content,
                comments = details.comments,
                imageLocalPath = details.imageLocalPath,
                imageUrl = details.imageUrl
            )
        )

        if (inserted == -1L) {
            Toast.makeText(this, "投稿失败，请稍后重试", Toast.LENGTH_SHORT).show()
            return
        }

        val message = getString(
            R.string.submission_success,
            details.title,
            details.comments.size
        )
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        setResult(RESULT_OK)
        finish()
    }

    private fun clearForm(vararg fields: EditText) {
        fields.forEach { field -> field.text?.clear() }
    }
}

