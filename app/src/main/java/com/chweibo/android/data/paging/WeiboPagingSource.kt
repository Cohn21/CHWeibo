package com.chweibo.android.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.data.model.WeiboTimelineResponse
import retrofit2.Response

class WeiboPagingSource(
    private val apiCall: suspend (page: Int, loadSize: Int, sinceId: Long?, maxId: Long?) -> Response<WeiboTimelineResponse>,
    private val onPageLoaded: suspend (List<WeiboPost>) -> Unit = {}
) : PagingSource<Int, WeiboPost>() {

    override fun getRefreshKey(state: PagingState<Int, WeiboPost>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, WeiboPost> {
        return try {
            val page = params.key ?: 1
            val response = apiCall(page, params.loadSize, null, null)

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val posts = data.statuses
                onPageLoaded(posts)

                val nextKey = if (posts.isEmpty()) null else page + 1

                LoadResult.Page(
                    data = posts,
                    prevKey = if (page == 1) null else page - 1,
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
