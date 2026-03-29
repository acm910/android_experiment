package com.example.experiment.data

import com.example.experiment.pojo.VO.NewsDetailsVO

object NewsMockData {

    private const val TOTAL_NEWS_PAGES = 4
    private const val TOTAL_COMMENT_PAGES = 3

    private val baseNewsList = listOf(
        NewsDetailsVO(
            id = "news_001",
            title = "AI 智能体框架进入规模化落地阶段",
            author = "AI Weekly",
            publishTime = "2026-03-29 09:00",
            content = "多家企业开始将智能体用于工单流转和自动审批，平均处理时长下降约 32%。",
            comments = listOf("企业场景确实更容易看到 ROI。", "期待开源框架对接方案。"),
            imageLocalPath = "img1"
        ),
        NewsDetailsVO(
            id = "news_002",
            title = "多模态模型在工业质检中精度提升",
            author = "智能制造观察",
            publishTime = "2026-03-29 08:40",
            content = "结合图像与文本工艺参数后，模型对细小缺陷识别率较单模态方案提升 11%。",
            comments = listOf("边缘端部署是关键。", "希望看到延迟数据。"),
            imageLocalPath = "ic_launcher"
        ),
        NewsDetailsVO(
            id = "news_003",
            title = "AIGC 设计工具支持品牌风格锁定",
            author = "Design AI Lab",
            publishTime = "2026-03-29 08:20",
            content = "新版本支持企业上传品牌手册，自动约束字体、配色和版式，减少返工成本。",
            comments = listOf("设计团队会更关注可控性。", "这对营销物料很有用。"),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetailsVO(
            id = "news_004",
            title = "国产推理芯片优化 Transformer 访存",
            author = "芯片前沿",
            publishTime = "2026-03-29 08:00",
            content = "通过改进 KV Cache 管理策略，在同等功耗下实现更高吞吐，适合私有化部署。",
            comments = listOf("硬件和框架协同很重要。", "期待公开基准测试。"),
            imageLocalPath = "img1"
        ),
        NewsDetailsVO(
            id = "news_005",
            title = "AI 代码助手新增安全修复建议",
            author = "DevTools News",
            publishTime = "2026-03-29 07:45",
            content = "当检测到常见漏洞模式时，助手会自动提供修复补丁并附带风险解释。",
            comments = listOf("如果能自动加测试会更好。", "解释质量是核心。"),
            imageLocalPath = "ic_launcher"
        ),
        NewsDetailsVO(
            id = "news_006",
            title = "教育行业上线个性化 AI 导学系统",
            author = "EdTech 报道",
            publishTime = "2026-03-28 20:30",
            content = "系统基于学习轨迹自动生成复习计划，并在错题集中加入针对性讲解。",
            comments = listOf("对薄弱项定位很实用。", "希望兼顾隐私合规。"),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetailsVO(
            id = "news_007",
            title = "RAG 检索增强方案降低幻觉率",
            author = "LLM 工程实践",
            publishTime = "2026-03-28 19:50",
            content = "通过分层检索与重排序策略，问答准确率提升并显著减少无依据回答。",
            comments = listOf("知识库质量决定上限。", "重排序模型值得关注。"),
            imageLocalPath = "img1"
        ),
        NewsDetailsVO(
            id = "news_008",
            title = "AI 医疗影像辅助读片进入试点",
            author = "HealthTech Daily",
            publishTime = "2026-03-28 18:30",
            content = "在基层医院场景中，AI 先筛查后复核的流程有效缩短了影像报告等待时间。",
            comments = listOf("医疗场景需要更严格验证。", "临床闭环很关键。"),
            imageLocalPath = "ic_launcher"
        ),
        NewsDetailsVO(
            id = "news_009",
            title = "端侧大模型压缩技术再升级",
            author = "Mobile AI",
            publishTime = "2026-03-28 17:10",
            content = "量化与蒸馏联合方案让 7B 模型在手机端实现更低内存占用和更稳推理速度。",
            comments = listOf("移动端体验会明显提升。", "电量影响也要关注。"),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetailsVO(
            id = "news_010",
            title = "AI 客服系统支持情绪识别路由",
            author = "服务智能化周刊",
            publishTime = "2026-03-28 16:40",
            content = "系统可识别高情绪风险会话并优先转人工，提升投诉场景的响应质量。",
            comments = listOf("实际业务很需要。", "误判率需要公开。"),
            imageLocalPath = "img1"
        ),
        NewsDetailsVO(
            id = "news_011",
            title = "开源视觉模型推出轻量版",
            author = "OpenCV 社区",
            publishTime = "2026-03-28 15:30",
            content = "轻量版在边缘设备上保持较高精度，适用于门店客流分析和安防巡检。",
            comments = listOf("边缘部署终于更友好。", "文档易用性很重要。"),
            imageLocalPath = "ic_launcher"
        ),
        NewsDetailsVO(
            id = "news_012",
            title = "AI 文档助手支持企业知识图谱",
            author = "Knowledge Tech",
            publishTime = "2026-03-28 14:55",
            content = "知识图谱增强后，跨系统问答可以自动关联术语和组织架构上下文。",
            comments = listOf("跨部门协作会更高效。", "图谱维护成本要评估。"),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetailsVO(
            id = "news_013",
            title = "AI 驱动的视频摘要功能发布",
            author = "Media AI",
            publishTime = "2026-03-28 14:10",
            content = "模型可自动抽取关键片段并生成章节标题，适合培训录屏与课程回看。",
            comments = listOf("效率提升很直观。", "希望支持多语言字幕。"),
            imageLocalPath = "img1"
        ),
        NewsDetailsVO(
            id = "news_014",
            title = "金融风控模型引入可解释层",
            author = "FinAI Research",
            publishTime = "2026-03-28 13:30",
            content = "新方案在给出风险评分同时展示主要特征贡献，便于审计与监管复核。",
            comments = listOf("可解释性是落地门槛。", "合规团队会欢迎。"),
            imageLocalPath = "ic_launcher"
        ),
        NewsDetailsVO(
            id = "news_015",
            title = "AI 翻译引擎强化术语一致性",
            author = "Language Tech",
            publishTime = "2026-03-28 12:45",
            content = "行业术语库接入后，长文档翻译中的术语漂移明显下降，校对工作量减少。",
            comments = listOf("专业文档场景很需要。", "术语库管理要方便。"),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetailsVO(
            id = "news_016",
            title = "AI 语音克隆新增授权校验机制",
            author = "Voice AI 观察",
            publishTime = "2026-03-28 11:50",
            content = "平台要求上传授权凭据并进行活体核验，降低未授权声音复用风险。",
            comments = listOf("这一步很有必要。", "希望行业形成统一标准。"),
            imageLocalPath = "img1"
        ),
        NewsDetailsVO(
            id = "news_017",
            title = "自动驾驶仿真平台引入生成式场景",
            author = "AutoAI Insights",
            publishTime = "2026-03-28 11:20",
            content = "系统可快速生成复杂长尾交通事件，用于训练感知和决策模块的鲁棒性。",
            comments = listOf("长尾场景覆盖很关键。", "仿真与实车闭环要打通。"),
            imageLocalPath = "ic_launcher"
        ),
        NewsDetailsVO(
            id = "news_018",
            title = "AI 办公助手支持会议行动项追踪",
            author = "Productivity AI",
            publishTime = "2026-03-28 10:30",
            content = "会议纪要可自动提取待办并同步到任务系统，支持超期提醒与责任人标注。",
            comments = listOf("跨工具同步很实用。", "希望支持更细粒度权限。"),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetailsVO(
            id = "news_019",
            title = "AI 图像生成强调版权来源追踪",
            author = "AIGC 合规简报",
            publishTime = "2026-03-28 09:55",
            content = "新平台为生成图像附加溯源元数据，方便后续版权核验和内容管理。",
            comments = listOf("对商用场景是加分项。", "溯源标准值得统一。"),
            imageLocalPath = "img1"
        ),
        NewsDetailsVO(
            id = "news_020",
            title = "智能推荐系统融合因果推断",
            author = "RecSys Journal",
            publishTime = "2026-03-28 09:20",
            content = "融合因果推断后，推荐策略在促活与长期留存之间取得更稳平衡。",
            comments = listOf("终于不只看短期点击。", "长期指标更有意义。"),
            imageLocalPath = "ic_launcher"
        ),
        NewsDetailsVO(
            id = "news_021",
            title = "AI 搜索引擎支持答案溯源卡片",
            author = "Search AI Daily",
            publishTime = "2026-03-27 20:45",
            content = "回答区域新增来源卡片与时间戳，用户可快速核对信息出处和时效性。",
            comments = listOf("这能缓解信任问题。", "来源质量也要把关。"),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetailsVO(
            id = "news_022",
            title = "AI 法务助手可自动识别风险条款",
            author = "LegalTech News",
            publishTime = "2026-03-27 19:30",
            content = "系统在合同审阅中自动标记高风险条款并提供修改建议，提高初审效率。",
            comments = listOf("法务初审效率会提升。", "最终仍需专业复核。"),
            imageLocalPath = "img1"
        ),
        NewsDetailsVO(
            id = "news_023",
            title = "AI 训练平台发布低成本微调方案",
            author = "ModelOps 周报",
            publishTime = "2026-03-27 18:10",
            content = "参数高效微调结合混合精度训练，可在有限预算下快速迭代行业模型。",
            comments = listOf("中小团队更容易上手。", "工具链成熟度很重要。"),
            imageLocalPath = "ic_launcher"
        ),
        NewsDetailsVO(
            id = "news_024",
            title = "AI 音频降噪技术应用到远程会议",
            author = "AudioLab",
            publishTime = "2026-03-27 17:35",
            content = "新模型可区分语音与环境噪声，在嘈杂场景中显著提升语音清晰度。",
            comments = listOf("远程办公刚需。", "低功耗实现值得关注。"),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetailsVO(
            id = "news_025",
            title = "AI 质检机器人升级异常解释能力",
            author = "Factory AI",
            publishTime = "2026-03-27 16:50",
            content = "当识别到异常时系统会同步给出可视化证据，便于一线人员快速复判。",
            comments = listOf("解释能力提升体验。", "现场可用性最关键。"),
            imageLocalPath = "img1"
        ),
        NewsDetailsVO(
            id = "news_026",
            title = "AI 广告投放平台引入预算守护",
            author = "MarTech Today",
            publishTime = "2026-03-27 15:20",
            content = "在流量异常波动时自动触发预算保护策略，减少短时间高消耗风险。",
            comments = listOf("很实用的运营功能。", "需要可自定义阈值。"),
            imageLocalPath = "ic_launcher"
        ),
        NewsDetailsVO(
            id = "news_027",
            title = "AI 数字人直播支持实时知识问答",
            author = "Digital Human Lab",
            publishTime = "2026-03-27 14:45",
            content = "数字人可联动知识库进行实时答疑，适用于产品发布与培训场景。",
            comments = listOf("互动体验会更好。", "回答准确率要持续监控。"),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetailsVO(
            id = "news_028",
            title = "大模型评测基准新增稳健性维度",
            author = "Eval Bench",
            publishTime = "2026-03-27 13:40",
            content = "新基准加入对抗样本与噪声输入测试，评估模型在复杂场景下的稳定表现。",
            comments = listOf("评测更贴近真实业务。", "希望公开更多样例。"),
            imageLocalPath = "img1"
        ),
        NewsDetailsVO(
            id = "news_029",
            title = "AI 供应链预测模型提升补货效率",
            author = "SupplyChain AI",
            publishTime = "2026-03-27 12:30",
            content = "结合节假日、天气与区域活动数据后，预测偏差下降，库存周转率提升。",
            comments = listOf("业务价值非常直接。", "数据质量影响很大。"),
            imageLocalPath = "ic_launcher"
        ),
        NewsDetailsVO(
            id = "news_030",
            title = "AI 运维助手支持根因定位建议",
            author = "AIOps Weekly",
            publishTime = "2026-03-27 11:20",
            content = "系统可基于日志和指标关联分析，给出疑似故障根因与排查路径。",
            comments = listOf("值班体验会改善。", "需要减少误报率。"),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetailsVO(
            id = "news_031",
            title = "AI 招聘筛选系统增加公平性校验",
            author = "HRTech Insights",
            publishTime = "2026-03-27 10:10",
            content = "平台在推荐候选人时引入偏差检测模块，帮助企业优化筛选策略。",
            comments = listOf("公平性是核心议题。", "透明机制需要持续完善。"),
            imageLocalPath = "img1"
        ),
        NewsDetailsVO(
            id = "news_032",
            title = "AI 视频生成支持镜头脚本控制",
            author = "Creative AI",
            publishTime = "2026-03-27 09:35",
            content = "用户可通过镜头语言模板控制构图和运镜，提升短视频生成可控性。",
            comments = listOf("创作自由度更高了。", "模板生态会很重要。"),
            imageLocalPath = "ic_launcher"
        ),
        NewsDetailsVO(
            id = "news_033",
            title = "AI 助手在政务热线场景完成试运行",
            author = "GovTech 观察",
            publishTime = "2026-03-26 18:50",
            content = "通过意图识别和知识库联动，系统可先行答复高频问题并转接复杂诉求。",
            comments = listOf("热线效率会提升。", "需保证答复准确与审慎。"),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetailsVO(
            id = "news_034",
            title = "AI 代码审查工具上线团队规范模式",
            author = "Engineering Pulse",
            publishTime = "2026-03-26 17:10",
            content = "工具支持按仓库规则输出审查意见，帮助团队统一风格并减少低级缺陷。",
            comments = listOf("团队协作会更顺滑。", "规则维护要简单。"),
            imageLocalPath = "img1"
        ),
        NewsDetailsVO(
            id = "news_035",
            title = "AI 内容审核模型强化跨语种能力",
            author = "Safety AI",
            publishTime = "2026-03-26 16:20",
            content = "模型新增多语种联合训练，可更稳定识别跨语言违规表达和变体文本。",
            comments = listOf("全球化业务很需要。", "误杀和漏判要平衡。"),
            imageLocalPath = "ic_launcher"
        ),
        NewsDetailsVO(
            id = "news_036",
            title = "AI 科研助手可自动生成实验记录",
            author = "Research AI Hub",
            publishTime = "2026-03-26 15:40",
            content = "实验过程中可实时整理步骤和参数，自动生成可复现报告草稿。",
            comments = listOf("科研复现会更方便。", "数据版本管理要跟上。"),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetailsVO(
            id = "news_037",
            title = "AI 电商导购支持意图连续追问",
            author = "Retail AI Daily",
            publishTime = "2026-03-26 14:25",
            content = "导购助手可根据用户偏好连续追问并动态筛选商品，提高转化效率。",
            comments = listOf("电商客服会很受益。", "对话记忆要稳定。"),
            imageLocalPath = "img1"
        ),
        NewsDetailsVO(
            id = "news_038",
            title = "AI 审计工具新增异常交易聚类",
            author = "AuditTech",
            publishTime = "2026-03-26 13:10",
            content = "通过聚类识别可疑交易模式，审计人员可更快定位高风险样本。",
            comments = listOf("减少人工筛选压力。", "需要结合行业规则。"),
            imageLocalPath = "ic_launcher"
        ),
        NewsDetailsVO(
            id = "news_039",
            title = "AI 训练数据平台上线质量评分",
            author = "DataOps AI",
            publishTime = "2026-03-26 12:00",
            content = "平台可对标注一致性和样本覆盖度打分，帮助团队优先修复低质量数据。",
            comments = listOf("数据治理越来越重要。", "评分标准建议可配置。"),
            imageLocalPath = "ic_launcher_round"
        ),
        NewsDetailsVO(
            id = "news_040",
            title = "AI 工作流编排平台发布企业版",
            author = "Automation AI",
            publishTime = "2026-03-26 10:50",
            content = "企业版支持多租户权限、审计日志和可视化流程编排，便于跨部门自动化协作。",
            comments = listOf("企业落地能力更完整。", "权限模型要足够细。"),
            imageLocalPath = "img1"
        )
    )

    fun getNewsDetailsList(): List<NewsDetailsVO> {
        return baseNewsList
    }

    fun hasMoreNews(page: Int): Boolean = page < TOTAL_NEWS_PAGES

    fun getNewsPage(page: Int, pageSize: Int = 5): List<NewsDetailsVO> {
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

