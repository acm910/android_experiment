package com.example.experiment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import coil.load
import com.example.experiment.data.session.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.io.FileOutputStream
import java.util.UUID


//todo 登录功能用一下Sharedpreferences 简单实现，后续可以改成 Room 或者其他更合适的方案。登录状态和用户信息（如用户名和头像路径）保存在 SessionManager 中，提供统一的接口供各页面查询和更新。
/**
 * 用户页 Fragment：提供登录入口，并根据登录态展示用户名称和头像。
 */
class UserFragment : Fragment(R.layout.fragment_user) {
    private lateinit var sessionManager: SessionManager
    private lateinit var avatarView: ImageView
    private lateinit var userNameView: TextView
    private lateinit var userSubtitleView: TextView

    private val loginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                renderLoginState()
            }
        }

    private val photoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            sessionManager.updateAvatarPath(uri.toString())
            bindAvatar(uri.toString())
            Toast.makeText(requireContext(), R.string.user_avatar_updated, Toast.LENGTH_SHORT).show()
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                cameraPreviewLauncher.launch(null)
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.user_camera_permission_denied,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val cameraPreviewLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap == null) return@registerForActivityResult
            val filePath = saveAvatarBitmap(bitmap)
            if (filePath.isNullOrBlank()) {
                Toast.makeText(requireContext(), R.string.user_avatar_update_failed, Toast.LENGTH_SHORT)
                    .show()
                return@registerForActivityResult
            }
            sessionManager.updateAvatarPath(filePath)
            bindAvatar(filePath)
            Toast.makeText(requireContext(), R.string.user_avatar_updated, Toast.LENGTH_SHORT).show()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        avatarView = view.findViewById(R.id.ivUserAvatar)
        userNameView = view.findViewById(R.id.tvUserName)
        userSubtitleView = view.findViewById(R.id.tvUserSubtitle)

        val headerBlock = view.findViewById<View>(R.id.userHeaderBlock)
        val logoutBtn = view.findViewById<View>(R.id.btnLogout)

        headerBlock.setOnClickListener {
            if (!sessionManager.isLoggedIn()) {
                loginLauncher.launch(Intent(requireContext(), LoginActivity::class.java))
                return@setOnClickListener
            }
            showAvatarOptionSheet()
        }

        logoutBtn.setOnClickListener {
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(requireContext(), R.string.user_not_logged_in, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            sessionManager.clearLogin()
            renderLoginState()
            Toast.makeText(requireContext(), R.string.user_logout_success, Toast.LENGTH_SHORT).show()
        }

        renderLoginState()
    }

    override fun onResume() {
        super.onResume()
        renderLoginState()
    }

    private fun renderLoginState() {
        if (!sessionManager.isLoggedIn()) {
            userNameView.text = getString(R.string.user_name_login_hint)
            userSubtitleView.text = getString(R.string.user_subtitle_login_hint)
            avatarView.setImageDrawable(null)
            avatarView.setBackgroundResource(R.drawable.bg_avatar_empty)
            return
        }

        val username = sessionManager.getUserName().orEmpty()
        val avatarPath = sessionManager.getAvatarPath()

        userNameView.text = username.ifBlank { getString(R.string.user_name_login_hint) }
        userSubtitleView.text = getString(R.string.user_subtitle_logged_in)
        avatarView.setBackgroundResource(0)
        bindAvatar(avatarPath)
    }

    private fun bindAvatar(avatarPath: String?) {
        val source = avatarPath?.trim()
        if (source.isNullOrBlank()) {
            avatarView.setImageResource(R.mipmap.ic_launcher_round)
            return
        }

        if (source.startsWith("http://") ||
            source.startsWith("https://") ||
            source.startsWith("file://") ||
            source.startsWith("content://") ||
            source.startsWith("/") ||
            source.startsWith("android.resource://")
        ) {
            avatarView.load(source) {
                crossfade(true)
                placeholder(R.mipmap.ic_launcher_round)
                error(R.mipmap.ic_launcher_round)
            }
            return
        }

        val resourceName = source
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .substringBeforeLast('.')

        val drawableId = requireContext().resources.getIdentifier(
            resourceName,
            "drawable",
            requireContext().packageName
        )
        val mipmapId = requireContext().resources.getIdentifier(
            resourceName,
            "mipmap",
            requireContext().packageName
        )

        when {
            drawableId != 0 -> avatarView.setImageResource(drawableId)
            mipmapId != 0 -> avatarView.setImageResource(mipmapId)
            else -> avatarView.setImageResource(R.mipmap.ic_launcher_round)
        }
    }

    private fun showAvatarOptionSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_avatar_options, null, false)
        dialog.setContentView(sheetView)

        sheetView.findViewById<Button>(R.id.btnPickFromGallery).setOnClickListener {
            dialog.dismiss()
            launchPhotoPicker()
        }
        sheetView.findViewById<Button>(R.id.btnTakePhoto).setOnClickListener {
            dialog.dismiss()
            ensureCameraPermissionThenOpenCamera()
        }
        sheetView.findViewById<Button>(R.id.btnCancelAvatarAction).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun launchPhotoPicker() {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun ensureCameraPermissionThenOpenCamera() {
        val permission = Manifest.permission.CAMERA
        val granted = ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (granted) {
            cameraPreviewLauncher.launch(null)
            return
        }
        requestCameraPermissionLauncher.launch(permission)
    }

    private fun saveAvatarBitmap(bitmap: Bitmap): String? {
        return try {
            val dir = File(requireContext().filesDir, "avatars")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "avatar_${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                output.flush()
            }
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

}

