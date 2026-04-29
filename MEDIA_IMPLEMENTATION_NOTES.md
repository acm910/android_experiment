# 项目媒体能力实现整理（视频 + 相册/拍照）

本文按“文件 + 第几段代码”的方式整理你当前项目中与视频、相册选择、拍照上传头像相关的核心实现。

---

## 1. 总览索引（先看这个）

| 模块 | 文件 | 段号 | 行号 | 作用 |
|---|---|---|---|---|
| 视频投稿预览 | `app/src/main/java/com/example/experiment/SubmissionActivity.kt` | 第1段 | 45-64 | 绑定 `etVideoUrl` + 预览按钮 + `VideoView` |
| 视频投稿校验 | `app/src/main/java/com/example/experiment/SubmissionActivity.kt` | 第2段 | 66-115 | 提交时校验 URL，写入 `videoUrl` |
| 视频预览播放 | `app/src/main/java/com/example/experiment/SubmissionActivity.kt` | 第3段 | 158-187 | 预览播放、错误处理、URL 合法性判断 |
| 视频详情展示 | `app/src/main/java/com/example/experiment/NewsActivity.kt` | 第1段 | 77-83 | 详情绑定时调用 `bindVideo(videoUrl, ...)` |
| 视频详情控制 | `app/src/main/java/com/example/experiment/NewsActivity.kt` | 第2段 | 143-187 | 空值隐藏、http/https 校验、播放准备、异常降级 |
| 视频生命周期 | `app/src/main/java/com/example/experiment/NewsActivity.kt` | 第3段 | 189-216 | `onPause/onResume/onStop` 续播与暂停 |
| 列表视频标识 | `app/src/main/java/com/example/experiment/adapter/NewsAdapter.kt` | 第1段 | 28-39 | `videoUrl` 非空显示“视频”标签 |
| 数据库存储视频 | `app/src/main/java/com/example/experiment/data/store/NewsStore.kt` | 第1段 | 32-71 | 查询详情/列表时读取 `video_url` |
| 数据库存储视频 | `app/src/main/java/com/example/experiment/data/store/NewsStore.kt` | 第2段 | 354-365 | `News -> ContentValues` 写入 `video_url` |
| 表结构视频列 | `app/src/main/java/com/example/experiment/data/db/NewsSchema.kt` | 第1段 | 27-65 | `COL_VIDEO_URL` + 建表 SQL |
| 旧库补列兼容 | `app/src/main/java/com/example/experiment/data/db/AppDatabaseHelper.kt` | 第1段 | 75-90 | 打开数据库时检查并补 `video_url` 列 |
| 相册选择入口 | `app/src/main/java/com/example/experiment/UserFragment.kt` | 第1段 | 44-50 | `PickVisualMedia` 选图回调 |
| 相机权限处理 | `app/src/main/java/com/example/experiment/UserFragment.kt` | 第2段 | 52-63 | 请求 `CAMERA` 权限并处理结果 |
| 拍照回调处理 | `app/src/main/java/com/example/experiment/UserFragment.kt` | 第3段 | 65-77 | `TakePicturePreview` 拍照结果转头像路径 |
| 头像操作面板 | `app/src/main/java/com/example/experiment/UserFragment.kt` | 第4段 | 179-197 | BottomSheet：相册/拍照/取消 |
| 相机调用入口 | `app/src/main/java/com/example/experiment/UserFragment.kt` | 第5段 | 205-217 | 检查权限后启动相机 |
| 拍照落盘 | `app/src/main/java/com/example/experiment/UserFragment.kt` | 第6段 | 219-232 | `Bitmap` 保存到 `filesDir/avatars` |
| 头像持久化 | `app/src/main/java/com/example/experiment/data/session/SessionManager.kt` | 第1段 | 23-41 | 按用户保存并读取头像路径 |
| 权限声明 | `app/src/main/AndroidManifest.xml` | 第1段 | 5-9 | `CAMERA` + `INTERNET` |

---

## 2. 视频相关实现逻辑

### 2.1 投稿页：填写 URL + 预览 + 提交入库

- 文件：`app/src/main/java/com/example/experiment/SubmissionActivity.kt`

**第1段（45-64）**：控件绑定与预览按钮
- 绑定 `etVideoUrl`、`btnPreviewVideo`、`videoPreviewContainer`、`vvSubmissionPreview`。
- 点击“预览视频”后执行 `previewVideo(videoUrl, ...)`。

**第2段（66-115）**：提交校验与写入
- `videoUrl` 为空允许提交。
- 非空时执行 `isValidNetworkVideoUrl(videoUrl)`。
- 通过后把 `videoUrl` 写入 `NewsDetailsVO`，再进入 `handleSubmit(...)`。

**第3段（158-187）**：预览与 URL 校验
- `videoUrl` 为空：隐藏预览容器并停止播放。
- URL 非法：Toast 提示。
- URL 合法：`VideoView.setVideoURI(Uri.parse(videoUrl))` 并自动播放。
- 播放失败：隐藏容器并提示 `video_preview_failed`。

关键代码片段：

```kotlin
private fun previewVideo(videoUrl: String?, container: FrameLayout, videoView: VideoView) {
    if (videoUrl.isNullOrBlank()) {
        container.visibility = View.GONE
        videoView.stopPlayback()
        return
    }
    if (!isValidNetworkVideoUrl(videoUrl)) {
        Toast.makeText(this, R.string.submission_invalid_video_url, Toast.LENGTH_SHORT).show()
        return
    }

    container.visibility = View.VISIBLE
    videoView.setVideoURI(Uri.parse(videoUrl))
    videoView.setOnPreparedListener { mediaPlayer ->
        mediaPlayer.isLooping = true
        videoView.start()
    }
}
```

---

### 2.2 详情页：有视频显示，无视频隐藏

- 文件：`app/src/main/java/com/example/experiment/NewsActivity.kt`

**第1段（77-83）**：详情绑定入口
- `bindDetails()` 中调用 `bindVideo(item.videoUrl, videoContainer, newsVideoView)`。

**第2段（143-187）**：核心显示规则
- URL 空：`container.GONE`。
- URL 必须是 `http/https` 且 `host` 非空。
- URL 合法时显示容器并播放。
- 出错时自动隐藏并 Toast，不影响正文与评论展示。

**第3段（189-216）**：生命周期处理
- `onPause()`：记录进度并暂停。
- `onResume()`：按标记恢复播放。
- `onStop()`：再次记录位置并暂停。

关键代码片段：

```kotlin
private fun bindVideo(videoUrl: String?, container: FrameLayout, videoView: VideoView) {
    val normalizedUrl = videoUrl?.trim().orEmpty()
    if (normalizedUrl.isBlank()) {
        currentVideoUrl = null
        container.visibility = FrameLayout.GONE
        videoView.stopPlayback()
        return
    }

    val uri = Uri.parse(normalizedUrl)
    val scheme = uri.scheme?.lowercase()
    if ((scheme != "http" && scheme != "https") || uri.host.isNullOrBlank()) {
        currentVideoUrl = null
        container.visibility = FrameLayout.GONE
        videoView.stopPlayback()
        return
    }

    container.visibility = FrameLayout.VISIBLE
    videoView.setVideoURI(uri)
}
```

---

### 2.3 列表页：轻量视频标签

- 文件：`app/src/main/java/com/example/experiment/adapter/NewsAdapter.kt`

**第1段（28-39）**
- `onBindViewHolder` 中：`item.videoUrl` 非空显示 `tvVideoTag`，否则隐藏。

```kotlin
holder.videoTagView.visibility = if (item.videoUrl.isNullOrBlank()) View.GONE else View.VISIBLE
```

- 对应布局：`app/src/main/res/layout/item_list.xml`
  - **第1段（35-49）**：定义 `tvVideoTag`，默认 `gone`。

---

### 2.4 数据层：videoUrl 从 VO 到 SQLite 的链路

- VO/Entity 字段
  - `app/src/main/java/com/example/experiment/pojo/VO/NewsDetailsVO.kt` 第1段（8-18）
  - `app/src/main/java/com/example/experiment/pojo/entity/News.kt` 第1段（8-19）

- DB Schema
  - `app/src/main/java/com/example/experiment/data/db/NewsSchema.kt` 第1段（27-65）
  - `COL_VIDEO_URL = "video_url"`

- Store 读写
  - `app/src/main/java/com/example/experiment/data/store/NewsStore.kt`
    - 第1段（32-71）：列表读取 `video_url`
    - 第2段（221-255）：详情按 id 读取 `video_url`
    - 第3段（354-365）：写入 `ContentValues` 时 `put(COL_VIDEO_URL, videoUrl)`

- 老库兼容补列
  - `app/src/main/java/com/example/experiment/data/db/AppDatabaseHelper.kt` 第1段（75-90）
  - `onOpen()` 时检查 `news` 表，不存在 `video_url` 则 `ALTER TABLE` 补列。

---

### 2.5 视频布局位置

- 投稿页预览区域：`app/src/main/res/layout/activity_submission.xml`
  - 第1段（101-133）：`etVideoUrl`、预览按钮、`videoPreviewContainer + VideoView`

- 详情页播放区域：`app/src/main/res/layout/activity_news.xml`
  - 第1段（73-86）：`newsVideoContainer + vvNewsVideo`，默认 `gone`

---

## 3. 相册与拍照相关实现逻辑

### 3.1 入口：用户页点击头像区域

- 文件：`app/src/main/java/com/example/experiment/UserFragment.kt`

**第1段（89-95）**
- 未登录：跳登录页。
- 已登录：打开头像操作 BottomSheet（相册/拍照）。

---

### 3.2 相册调用（Photo Picker）

- 文件：`app/src/main/java/com/example/experiment/UserFragment.kt`

**第2段（44-50）**：注册相册选择回调
- 使用 `ActivityResultContracts.PickVisualMedia()`。
- 选图成功后：`sessionManager.updateAvatarPath(uri.toString())` + `bindAvatar(...)`。

**第3段（199-203）**：启动相册
- 使用 `PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)`。

关键代码片段：

```kotlin
private val photoPickerLauncher =
    registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        sessionManager.updateAvatarPath(uri.toString())
        bindAvatar(uri.toString())
    }
```

---

### 3.3 拍照调用（相机）

- 文件：`app/src/main/java/com/example/experiment/UserFragment.kt`

**第4段（52-63）**：权限请求回调
- 使用 `RequestPermission()` 请求 `Manifest.permission.CAMERA`。
- 授权成功后调用 `cameraPreviewLauncher.launch(null)`。

**第5段（65-77）**：拍照结果回调
- 使用 `TakePicturePreview()` 获取 `Bitmap`。
- 调用 `saveAvatarBitmap(bitmap)` 存为本地文件。
- 再写入 `SessionManager` 并回显头像。

**第6段（205-217）**：权限检查与启动相机
- `ContextCompat.checkSelfPermission(...)` 判断权限。
- 有权限直接拍照，无权限先申请。

**第7段（219-232）**：头像落盘
- 保存到：`requireContext().filesDir/avatars/avatar_<UUID>.jpg`。

关键代码片段：

```kotlin
private val cameraPreviewLauncher =
    registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap == null) return@registerForActivityResult
        val filePath = saveAvatarBitmap(bitmap)
        sessionManager.updateAvatarPath(filePath)
        bindAvatar(filePath)
    }
```

---

### 3.4 头像操作面板（BottomSheet）

- 文件：`app/src/main/java/com/example/experiment/UserFragment.kt`
  - 第8段（179-197）：弹出菜单并绑定“相册/拍照/取消”按钮。

- 布局文件：`app/src/main/res/layout/bottom_sheet_avatar_options.xml`
  - 第1段（23-53）：`btnPickFromGallery`、`btnTakePhoto`、`btnCancelAvatarAction`。

---

### 3.5 头像持久化（按用户）

- 文件：`app/src/main/java/com/example/experiment/data/session/SessionManager.kt`

**第1段（23-41）**：读取/更新头像路径
- 登录用户有唯一键时，头像按用户键保存：`key_user_avatar_<userKey>`。
- 保证“同一用户名再次登录仍显示上次头像”。

---

## 4. 权限与清单配置

- 文件：`app/src/main/AndroidManifest.xml`

**第1段（5-9）**
- `uses-feature android.hardware.camera`（`required=false`）
- `uses-permission android.permission.CAMERA`
- `uses-permission android.permission.INTERNET`

---

## 5. 你项目当前媒体链路（一句话版）

- 视频：`SubmissionActivity` 填写/预览 URL -> `NewsStore` 写入 `news.video_url` -> `NewsActivity.bindVideo()` 按 URL 显示播放（无视频隐藏）。
- 头像：`UserFragment` BottomSheet 选择“相册/拍照” -> 回调拿 `Uri/Bitmap` -> `SessionManager.updateAvatarPath()` 持久化 -> `bindAvatar()` 回显。
# 项目媒体能力实现整理（视频 + 相册/拍照）

本文按“文件 + 第几段代码”的方式整理你当前项目中与**视频**、**相册选择**、**拍照上传头像**相关的核心实现。

---

## 1. 总览索引（先看这个）

| 模块 | 文件 | 段号 | 行号 | 作用 |
|---|---|---|---|---|
| 视频投稿预览 | `app/src/main/java/com/example/experiment/SubmissionActivity.kt` | 第1段 | 45-64 | 绑定 `etVideoUrl` + 预览按钮 + `VideoView` |
| 视频投稿校验 | `app/src/main/java/com/example/experiment/SubmissionActivity.kt` | 第2段 | 66-115 | 提交时校验 URL，写入 `videoUrl` |
| 视频预览播放 | `app/src/main/java/com/example/experiment/SubmissionActivity.kt` | 第3段 | 158-187 | 预览播放、错误处理、URL 合法性判断 |
| 视频详情展示 | `app/src/main/java/com/example/experiment/NewsActivity.kt` | 第1段 | 77-83 | 详情绑定时调用 `bindVideo(videoUrl, ...)` |
| 视频详情控制 | `app/src/main/java/com/example/experiment/NewsActivity.kt` | 第2段 | 143-187 | 空值隐藏、http/https 校验、播放准备、异常降级 |
| 视频生命周期 | `app/src/main/java/com/example/experiment/NewsActivity.kt` | 第3段 | 189-216 | `onPause/onResume/onStop` 续播与暂停 |
| 列表视频标识 | `app/src/main/java/com/example/experiment/adapter/NewsAdapter.kt` | 第1段 | 28-39 | `videoUrl` 非空显示“视频”标签 |
| 数据库存储视频 | `app/src/main/java/com/example/experiment/data/store/NewsStore.kt` | 第1段 | 32-71 | 查询详情/列表时读取 `video_url` |
| 数据库存储视频 | `app/src/main/java/com/example/experiment/data/store/NewsStore.kt` | 第2段 | 354-365 | `News -> ContentValues` 写入 `video_url` |
| 表结构视频列 | `app/src/main/java/com/example/experiment/data/db/NewsSchema.kt` | 第1段 | 27-65 | `COL_VIDEO_URL` + 建表 SQL |
| 旧库补列兼容 | `app/src/main/java/com/example/experiment/data/db/AppDatabaseHelper.kt` | 第1段 | 75-90 | 打开数据库时检查并补 `video_url` 列 |
| 相册选择入口 | `app/src/main/java/com/example/experiment/UserFragment.kt` | 第1段 | 44-50 | `PickVisualMedia` 选图回调 |
| 相机权限处理 | `app/src/main/java/com/example/experiment/UserFragment.kt` | 第2段 | 52-63 | 请求 `CAMERA` 权限并处理结果 |
| 拍照回调处理 | `app/src/main/java/com/example/experiment/UserFragment.kt` | 第3段 | 65-77 | `TakePicturePreview` 拍照结果转头像路径 |
| 头像操作面板 | `app/src/main/java/com/example/experiment/UserFragment.kt` | 第4段 | 179-197 | BottomSheet：相册/拍照/取消 |
| 相机调用入口 | `app/src/main/java/com/example/experiment/UserFragment.kt` | 第5段 | 205-217 | 检查权限后启动相机 |
| 拍照落盘 | `app/src/main/java/com/example/experiment/UserFragment.kt` | 第6段 | 219-232 | `Bitmap` 保存到 `filesDir/avatars` |
| 头像持久化 | `app/src/main/java/com/example/experiment/data/session/SessionManager.kt` | 第1段 | 23-41 | 按用户保存并读取头像路径 |
| 权限声明 | `app/src/main/AndroidManifest.xml` | 第1段 | 5-9 | `CAMERA` + `INTERNET` |

---

## 2. 视频相关实现逻辑

### 2.1 投稿页：填写 URL + 预览 + 提交入库

- 文件：`app/src/main/java/com/example/experiment/SubmissionActivity.kt`

**第1段（45-64）**：控件绑定与预览按钮
- 绑定 `etVideoUrl`、`btnPreviewVideo`、`videoPreviewContainer`、`vvSubmissionPreview`。
- 点击“预览视频”后执行 `previewVideo(videoUrl, ...)`。

**第2段（66-115）**：提交校验与写入
- `videoUrl` 为空允许提交。
- 非空时执行 `isValidNetworkVideoUrl(videoUrl)`。
- 通过后把 `videoUrl` 写入 `NewsDetailsVO`，再进入 `handleSubmit(...)`。

**第3段（158-187）**：预览与 URL 校验
- `videoUrl` 为空：隐藏预览容器并停止播放。
- URL 非法：Toast 提示。
- URL 合法：`VideoView.setVideoURI(Uri.parse(videoUrl))` 并自动播放。
- 播放失败：隐藏容器并提示 `video_preview_failed`。

关键代码片段：

```kotlin
private fun previewVideo(videoUrl: String?, container: FrameLayout, videoView: VideoView) {
    if (videoUrl.isNullOrBlank()) {
        container.visibility = View.GONE
        videoView.stopPlayback()
        return
    }
    if (!isValidNetworkVideoUrl(videoUrl)) {
        Toast.makeText(this, R.string.submission_invalid_video_url, Toast.LENGTH_SHORT).show()
        return
    }

    container.visibility = View.VISIBLE
    videoView.setVideoURI(Uri.parse(videoUrl))
    videoView.setOnPreparedListener { mediaPlayer ->
        mediaPlayer.isLooping = true
        videoView.start()
    }
}
```

---

### 2.2 详情页：有视频显示，无视频隐藏

- 文件：`app/src/main/java/com/example/experiment/NewsActivity.kt`

**第1段（77-83）**：详情绑定入口
- `bindDetails()` 中调用 `bindVideo(item.videoUrl, videoContainer, newsVideoView)`。

**第2段（143-187）**：核心显示规则
- URL 空：`container.GONE`。
- URL 必须是 `http/https` 且 `host` 非空。
- URL 合法时显示容器并播放。
- 出错时自动隐藏并 Toast，不影响正文与评论展示。

**第3段（189-216）**：生命周期处理
- `onPause()`：记录进度并暂停。
- `onResume()`：按标记恢复播放。
- `onStop()`：再次记录位置并暂停。

关键代码片段：

```kotlin
private fun bindVideo(videoUrl: String?, container: FrameLayout, videoView: VideoView) {
    val normalizedUrl = videoUrl?.trim().orEmpty()
    if (normalizedUrl.isBlank()) {
        currentVideoUrl = null
        container.visibility = FrameLayout.GONE
        videoView.stopPlayback()
        return
    }

    val uri = Uri.parse(normalizedUrl)
    val scheme = uri.scheme?.lowercase()
    if ((scheme != "http" && scheme != "https") || uri.host.isNullOrBlank()) {
        currentVideoUrl = null
        container.visibility = FrameLayout.GONE
        videoView.stopPlayback()
        return
    }

    container.visibility = FrameLayout.VISIBLE
    videoView.setVideoURI(uri)
}
```

---

### 2.3 列表页：轻量视频标签

- 文件：`app/src/main/java/com/example/experiment/adapter/NewsAdapter.kt`

**第1段（28-39）**
- `onBindViewHolder` 中：`item.videoUrl` 非空显示 `tvVideoTag`，否则隐藏。

```kotlin
holder.videoTagView.visibility = if (item.videoUrl.isNullOrBlank()) View.GONE else View.VISIBLE
```

- 对应布局：`app/src/main/res/layout/item_list.xml`
  - **第1段（35-49）**：定义 `tvVideoTag`，默认 `gone`。

---

### 2.4 数据层：videoUrl 从 VO 到 SQLite 的链路

- VO/Entity 字段
  - `app/src/main/java/com/example/experiment/pojo/VO/NewsDetailsVO.kt` 第1段（8-18）
  - `app/src/main/java/com/example/experiment/pojo/entity/News.kt` 第1段（8-19）

- DB Schema
  - `app/src/main/java/com/example/experiment/data/db/NewsSchema.kt` 第1段（27-65）
  - `COL_VIDEO_URL = "video_url"`

- Store 读写
  - `app/src/main/java/com/example/experiment/data/store/NewsStore.kt`
    - 第1段（32-71）：列表读取 `video_url`
    - 第2段（221-255）：详情按 id 读取 `video_url`
    - 第3段（354-365）：写入 `ContentValues` 时 `put(COL_VIDEO_URL, videoUrl)`

- 老库兼容补列
  - `app/src/main/java/com/example/experiment/data/db/AppDatabaseHelper.kt` 第1段（75-90）
  - `onOpen()` 时检查 `news` 表，不存在 `video_url` 则 `ALTER TABLE` 补列。

---

### 2.5 视频布局位置

- 投稿页预览区域：`app/src/main/res/layout/activity_submission.xml`
  - 第1段（101-133）：`etVideoUrl`、预览按钮、`videoPreviewContainer + VideoView`

- 详情页播放区域：`app/src/main/res/layout/activity_news.xml`
  - 第1段（73-86）：`newsVideoContainer + vvNewsVideo`，默认 `gone`

---

## 3. 相册与拍照相关实现逻辑

### 3.1 入口：用户页点击头像区域

- 文件：`app/src/main/java/com/example/experiment/UserFragment.kt`

**第1段（89-95）**
- 未登录：跳登录页。
- 已登录：打开头像操作 BottomSheet（相册/拍照）。

---

### 3.2 相册调用（Photo Picker）

- 文件：`app/src/main/java/com/example/experiment/UserFragment.kt`

**第2段（44-50）**：注册相册选择回调
- 使用 `ActivityResultContracts.PickVisualMedia()`。
- 选图成功后：`sessionManager.updateAvatarPath(uri.toString())` + `bindAvatar(...)`。

**第3段（199-203）**：启动相册

- 使用 `PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)`。

关键代码片段：

```kotlin
private val photoPickerLauncher =
    registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        sessionManager.updateAvatarPath(uri.toString())
        bindAvatar(uri.toString())
    }
```

---

### 3.3 拍照调用（相机）

- 文件：`app/src/main/java/com/example/experiment/UserFragment.kt`

**第4段（52-63）**：权限请求回调

- 使用 `RequestPermission()` 请求 `Manifest.permission.CAMERA`。
- 授权成功后调用 `cameraPreviewLauncher.launch(null)`。

**第5段（65-77）**：拍照结果回调
- 使用 `TakePicturePreview()` 获取 `Bitmap`。
- 调用 `saveAvatarBitmap(bitmap)` 存为本地文件。
- 再写入 `SessionManager` 并回显头像。

**第6段（205-217）**：权限检查与启动相机
- `ContextCompat.checkSelfPermission(...)` 判断权限。
- 有权限直接拍照，无权限先申请。

**第7段（219-232）**：头像落盘
- 保存到：`requireContext().filesDir/avatars/avatar_<UUID>.jpg`。

关键代码片段：

```kotlin
private val cameraPreviewLauncher =
    registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap == null) return@registerForActivityResult
        val filePath = saveAvatarBitmap(bitmap)
        sessionManager.updateAvatarPath(filePath)
        bindAvatar(filePath)
    }
```

---

### 3.4 头像操作面板（BottomSheet）

- 文件：`app/src/main/java/com/example/experiment/UserFragment.kt`
  - 第8段（179-197）：弹出菜单并绑定“相册/拍照/取消”按钮。

- 布局文件：`app/src/main/res/layout/bottom_sheet_avatar_options.xml`
  - 第1段（23-53）：`btnPickFromGallery`、`btnTakePhoto`、`btnCancelAvatarAction`。

---

### 3.5 头像持久化（按用户）

- 文件：`app/src/main/java/com/example/experiment/data/session/SessionManager.kt`

**第1段（23-41）**：读取/更新头像路径
- 登录用户有唯一键时，头像按用户键保存：`key_user_avatar_<userKey>`。
- 保证“同一用户名再次登录仍显示上次头像”。

---

## 4. 权限与清单配置

- 文件：`app/src/main/AndroidManifest.xml`

**第1段（5-9）**
- `uses-feature android.hardware.camera`（`required=false`）
- `uses-permission android.permission.CAMERA`
- `uses-permission android.permission.INTERNET`

---

## 5. 你项目当前媒体链路（一句话版）

- 视频：`SubmissionActivity` 填写/预览 URL -> `NewsStore` 写入 `news.video_url` -> `NewsActivity.bindVideo()` 按 URL 显示播放（无视频隐藏）。
- 头像：`UserFragment` BottomSheet 选择“相册/拍照” -> 回调拿 `Uri/Bitmap` -> `SessionManager.updateAvatarPath()` 持久化 -> `bindAvatar()` 回显。


