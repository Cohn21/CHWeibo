package com.chweibo.android.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.data.model.WeiboTimelineResponse
import retrofit2.Response

class WeiboPagingSource(
    private val apiCall: suspend (Int, Long?, Long?) -> Response<WeiboTimelineResponse>
) : PagingSource<Long, WeiboPost>() {

    override fun getRefreshKey(state: PagingState<Long, WeiboPost>): Long? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, WeiboPost> {
        return try {
            val maxId = params.key
            val response = apiCall(params.loadSize, null, maxId)

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val posts = data.statuses

                // 计算下一页的 key
                // 微博 API 使用 max_id 进行分页，返回的数据包含 max_id，需要减 1 避免重复
                val nextKey = if (posts.isEmpty() || data.nextCursor == 0L) {
                    null
                } else {
                    data.nextCursor - 1
                }

                LoadResult.Page(
                    data = posts,
                    prevKey = null,  // 微博时间线只支持向后加载
                    nextKey = nextKey
                )
            } else {
                LoadResult.Error(Exception(response.errorBody()?.string() ?: "Unknown error"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
