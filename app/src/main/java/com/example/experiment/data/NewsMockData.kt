package com.example.experiment.data

import com.example.experiment.pojo.NewsDetails

object NewsMockData {

    private const val TOTAL_NEWS_PAGES = 4
    private const val TOTAL_COMMENT_PAGES = 3

    private val baseNewsList = listOf(
        NewsDetails(
            id = "news_001",
            title = "Android 16 发布",
            author = "Google Android Team",
            publishTime = "2026-03-29 09:30",
            content = "Android 16 带来更完善的隐私权限管理、后台性能优化和更稳定的系统动画表现。开发者可以在后台任务调度、权限申请和动画渲染方面获得更稳定的一致性。",
            comments = listOf(
                "这次升级对流畅度提升很明显。",
                "希望尽快适配更多机型。",
                "文档写得很清楚，开发成本降低了。"
            ),
            imageLocalPath = "ic_launcher"
        ),
        NewsDetails(
            id = "news_002",
            title = "Kotlin 2.x 更新",
            author = "JetBrains",
            publishTime = "2026-03-28 14:20",
            content = "Kotlin 2.x 在编译速度、错误提示和多平台支持方面都有改进。尤其在大型项目增量编译场景下，构建时间进一步缩短。",
            comments = listOf(
                "IDE 联动体验更好了。",
                "期待更多 KMP 实战案例。"
            ),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetails(
            id = "news_003",
            title = "Jetpack Compose 进阶",
            author = "Android Dev Blog",
            publishTime = "2026-03-27 18:05",
            content = "在复杂列表场景下，Compose 通过状态提升与分层渲染能获得更好的可维护性。建议配合稳定 key 和 remember 策略优化性能。",
            comments = listOf(
                "列表性能优化部分很实用。",
                "示例代码清晰，容易上手。",
                "希望增加更多动画实战案例。"
            ),
            imageLocalPath = "img1"
        ),
        NewsDetails(
            id = "news_004",
            title = "RecyclerView 实战",
            author = "Mobile Weekly",
            publishTime = "2026-03-26 11:45",
            content = "通过 Adapter + ViewHolder 的结构化设计，可以更稳定地组织大规模列表数据。配合 DiffUtil 可以进一步提升刷新效率。",
            comments = listOf(
                "分割线处理方案很干净。",
                "点击跳转详情的流程很标准。"
            ),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetails(
            id = "news_005",
            title = "Android Studio 新功能",
            author = "Android Studio Team",
            publishTime = "2026-03-25 10:00",
            content = "新版 Android Studio 引入了更直观的性能分析与布局检查工具，帮助开发者快速定位 UI 卡顿和渲染瓶颈。",
            comments = emptyList(),
            imageLocalPath = "ic_launcher"
        )
    )

    fun getNewsDetailsList(): List<NewsDetails> {
        return baseNewsList
    }

    fun hasMoreNews(page: Int): Boolean = page < TOTAL_NEWS_PAGES

    fun getNewsPage(page: Int, pageSize: Int = 5): List<NewsDetails> {
        if (!hasMoreNews(page) || pageSize <= 0) return emptyList()
        return List(pageSize) { index ->
            val seed = baseNewsList[(page * pageSize + index) % baseNewsList.size]
            seed.copy(
                id = "${seed.id}_p${page}_i${index}",
                title = if (page == 0) seed.title else "${seed.title} · 第${page + 1}页"
            )
        }
    }

    fun hasMoreComments(page: Int): Boolean = page < TOTAL_COMMENT_PAGES

    fun getMoreComments(newsId: String, page: Int, pageSize: Int = 3): List<String> {
        if (!hasMoreComments(page) || pageSize <= 0) return emptyList()
        return List(pageSize) { index ->
            val commentIndex = page * pageSize + index + 1
            "[$newsId] 追加评论 $commentIndex：这是一条用于上拉加载的测试评论。"
        }
    }
}

