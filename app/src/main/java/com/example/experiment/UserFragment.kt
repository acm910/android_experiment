package com.example.experiment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

/**
 * 用户页 Fragment：当前仅提供展示结构，后续再接入用户相关功能。
 */
class UserFragment : Fragment(R.layout.fragment_user) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 预留：后续绑定信息修改、退出登录等点击事件。
    }
}

