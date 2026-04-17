# 微博第三方客户端

基于 Jetpack Compose 开发的现代化微博第三方 Android 客户端。

## 功能特性

### 已实现功能
- ✅ OAuth2 微博登录
- ✅ 首页时间线浏览
- ✅ 微博卡片展示（支持图片、转发、视频）
- ✅ 下拉刷新、上拉加载更多
- ✅ 发布微博（文本 + 图片）
- ✅ 转发微博
- ✅ 评论功能（查看、发表、回复）
- ✅ 点赞/取消点赞
- ✅ 发现页面（热搜、话题）
- ✅ 搜索功能
- ✅ 消息中心
- ✅ 个人资料页
- ✅ 用户详情页
- ✅ 微博详情页
- ✅ 草稿箱
- ✅ 图片查看器（支持手势缩放）
- ✅ 深色模式支持
- ✅ 设置页面

### 技术架构
- **UI 框架**: Jetpack Compose
- **架构模式**: MVVM + Repository
- **依赖注入**: Hilt
- **网络请求**: Retrofit + OkHttp
- **图片加载**: Coil
- **本地存储**: Room + DataStore
- **分页加载**: Paging 3

## 项目结构

```
app/src/main/java/com/example/weibo/
├── data/
│   ├── api/              # Retrofit API 接口
│   ├── model/            # 数据模型 (User, WeiboPost, Comment等)
│   ├── local/            # Room 数据库 + DataStore
│   ├── repository/       # 数据仓库
│   └── paging/           # Paging 分页源
├── di/                   # Hilt 依赖注入
├── navigation/           # Compose Navigation
├── ui/
│   ├── screens/          # 页面 (Home, Login, Post等)
│   ├── components/       # 可复用组件 (ImageViewer, VideoPlayer等)
│   ├── theme/            # Material3 主题
│   └── viewmodel/        # ViewModel
└── utils/                # 工具类
```

## 快速开始

### 1. 配置微博开放平台

1. 访问 [微博开放平台](https://open.weibo.com/)
2. 注册开发者账号
3. 创建移动应用，获取 App Key 和 App Secret
4. 配置回调地址（默认: `https://api.weibo.com/oauth2/default.html`）

### 2. 修改配置

在 `app/build.gradle.kts` 中替换以下配置：

```kotlin
buildConfigField("String", "WEIBO_APP_KEY", "\"YOUR_APP_KEY_HERE\"")
```

在 `AuthRepository.kt` 中替换：

```kotlin
const val CLIENT_SECRET = "YOUR_APP_SECRET_HERE"
```

### 3. 构建运行

```bash
# 使用 Gradle 构建
./gradlew assembleDebug

# 或使用 Android Studio 直接运行
```

## 主要 API 接口

### 认证
- `oauth2/authorize` - 用户授权
- `oauth2/access_token` - 获取 Token

### 微博
- `statuses/home_timeline` - 首页时间线
- `statuses/update` - 发布微博
- `statuses/upload` - 发布带图片微博
- `statuses/repost` - 转发微博
- `statuses/destroy` - 删除微博

### 互动
- `comments/show` - 获取评论
- `comments/create` - 发表评论
- `comments/reply` - 回复评论
- `attitudes/create` - 点赞
- `attitudes/destroy` - 取消点赞

### 用户
- `users/show` - 获取用户信息
- `friendships/friends` - 关注列表
- `friendships/followers` - 粉丝列表
- `friendships/create` - 关注用户
- `friendships/destroy` - 取消关注

## 界面预览

| 页面 | 功能 |
|------|------|
| **首页** | 时间线、下拉刷新、图片网格、转发内容 |
| **发现** | 热搜榜、话题推荐、功能入口 |
| **发布** | 文本输入、图片选择、表情、话题 |
| **消息** | @我的、评论、赞、私信入口 |
| **我的** | 用户信息、统计数据、设置入口 |
| **微博详情** | 完整内容、评论列表、互动按钮 |
| **用户详情** | 资料展示、微博列表、关注按钮 |
| **图片查看** | 左右滑动、手势缩放、下载分享 |
| **评论页** | 评论列表、发表评论、回复 |
| **搜索页** | 搜索历史、热搜榜、结果展示 |
| **转发页** | 转发内容、原微博预览、同时评论 |
| **草稿箱** | 草稿列表、编辑、删除 |

## 依赖库

```kotlin
// Compose BOM
implementation(platform("androidx.compose:compose-bom:2024.02.00"))

// Hilt
implementation("com.google.dagger:hilt-android:2.50")

// Retrofit
implementation("com.squareup.retrofit2:retrofit:2.9.0")

// Coil
implementation("io.coil-kt:coil-compose:2.5.0")

// Room
implementation("androidx.room:room-runtime:2.6.1")

// Paging
implementation("androidx.paging:paging-compose:3.2.1")

// Media3 (视频播放)
implementation("androidx.media3:media3-exoplayer:1.2.1")
```

## 组件说明

### 图片查看器 (ImageViewer)
- 支持左右滑动切换
- 双击缩放
- 手势缩放（捏合）
- 下载和分享功能

### 视频播放器 (VideoPlayer)
- 基于 ExoPlayer
- 播放/暂停控制
- 进度条拖动
- 全屏播放支持

### 富文本渲染 (RichText)
- 话题高亮 (#话题#)
- @用户高亮
- URL 链接识别
- 可点击跳转

### 表情选择器 (EmojiPicker)
- 分类展示
- 常用表情
- Emoji 支持
- 快速插入

## 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

MIT License

## 免责声明

本项目仅供学习交流使用，请遵守微博开放平台相关协议。使用本项目产生的任何法律责任由使用者自行承担。

## 更新日志

### v1.0.0 (2024-04)
- 初始版本发布
- 实现基础功能：登录、浏览、发布、评论、点赞
- 支持图片查看、转发、搜索、草稿箱
