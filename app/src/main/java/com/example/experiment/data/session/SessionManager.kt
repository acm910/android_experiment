package com.example.experiment.data.session

import android.content.Context

/**
 * 登录会话管理：负责保存和读取当前登录状态、用户名和头像信息。
 */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_LOGGED_IN, false)
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun getAvatarPath(): String? {
        val userKey = prefs.getString(KEY_USER_UNIQUE_KEY, null)
        if (userKey.isNullOrBlank()) {
            return prefs.getString(KEY_AVATAR_PATH, null)
        }
        return prefs.getString(makeAvatarKey(userKey), prefs.getString(KEY_AVATAR_PATH, null))
    }

    /**
     * 仅更新头像路径，不改动登录态和用户名。
     */
    fun updateAvatarPath(avatarPath: String?) {
        val editor = prefs.edit().putString(KEY_AVATAR_PATH, avatarPath)
        val userKey = prefs.getString(KEY_USER_UNIQUE_KEY, null)
        if (!userKey.isNullOrBlank()) {
            editor.putString(makeAvatarKey(userKey), avatarPath)
        }
        editor.apply()
    }

    /**
     * 登录并切换到指定账号：同一账号会复用历史头像，保证头像持久化。
     */
    fun saveLogin(username: String, defaultAvatarPath: String? = "ic_launcher_round") {
        val displayName = username.trim()
        val userKey = normalizeUsername(displayName)
        if (userKey.isBlank()) return

        val userAvatarKey = makeAvatarKey(userKey)
        val legacyAvatar = prefs.getString(KEY_AVATAR_PATH, null)
        val storedAvatar = prefs.getString(userAvatarKey, null)
        val finalAvatar = storedAvatar ?: legacyAvatar ?: defaultAvatarPath

        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_USER_UNIQUE_KEY, userKey)
            .putString(KEY_USERNAME, displayName)
            .putString(KEY_AVATAR_PATH, finalAvatar)
            .putString(userAvatarKey, finalAvatar)
            .apply()
    }

    fun clearLogin() {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .remove(KEY_USERNAME)
            .remove(KEY_AVATAR_PATH)
            .remove(KEY_USER_UNIQUE_KEY)
            .apply()
    }

    private fun normalizeUsername(username: String): String {
        return username.trim().lowercase()
    }

    private fun makeAvatarKey(userKey: String): String {
        return "${KEY_USER_AVATAR_PREFIX}$userKey"
    }

    /**
     * 读取上次登录页输入的用户名。
     */
    fun getLastUsername(): String {
        return prefs.getString(KEY_LAST_USERNAME, "") ?: ""
    }

    /**
     * 保存上次登录页输入的用户名。
     */
    fun setLastUsername(username: String) {
        prefs.edit()
            .putString(KEY_LAST_USERNAME, username)
            .apply()
    }

    /**
     * 是否勾选了记住用户名。
     */
    fun isRememberUsernameEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_USERNAME, true)
    }

    fun setRememberUsernameEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_REMEMBER_USERNAME, enabled)
            .apply()
    }

    /**
     * 是否勾选了记住密码。
     */
    fun isRememberPasswordEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_PASSWORD, false)
    }

    fun setRememberPasswordEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_REMEMBER_PASSWORD, enabled)
            .apply()
    }

    fun getSavedPassword(): String {
        return prefs.getString(KEY_SAVED_PASSWORD, "") ?: ""
    }

    fun setSavedPassword(password: String) {
        prefs.edit()
            .putString(KEY_SAVED_PASSWORD, password)
            .apply()
    }

    fun clearSavedPassword() {
        prefs.edit()
            .remove(KEY_SAVED_PASSWORD)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "session_prefs"
        private const val KEY_LOGGED_IN = "key_logged_in"
        private const val KEY_USERNAME = "key_username"
        private const val KEY_AVATAR_PATH = "key_avatar_path"
        private const val KEY_USER_UNIQUE_KEY = "key_user_unique_key"
        private const val KEY_USER_AVATAR_PREFIX = "key_user_avatar_"
        private const val KEY_LAST_USERNAME = "key_last_username"
        private const val KEY_REMEMBER_USERNAME = "key_remember_username"
        private const val KEY_REMEMBER_PASSWORD = "key_remember_password"
        private const val KEY_SAVED_PASSWORD = "key_saved_password"
    }
}

