package com.pointlessgames.hexagone.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pointlessgames.hexagone.game.model.DetailedGameResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

data class RankedGameResult(
    val rank: Int,
    val result: DetailedGameResult
)

class LeaderboardPagingSource(
    private val supabase: SupabaseClient,
) : PagingSource<Int, RankedGameResult>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RankedGameResult> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return try {
            val fromOffset = page * pageSize
            val toOffset = fromOffset + pageSize - 1

            val items = supabase.from("game_results").select {
                order("score", Order.DESCENDING)
                range(fromOffset.toLong(), toOffset.toLong())
            }.decodeList<DetailedGameResult>().mapIndexed { index, item ->
                RankedGameResult(rank = fromOffset + index + 1, result = item)
            }

            LoadResult.Page(
                data = items,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (items.isEmpty() || items.size < pageSize) null else page + 1,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RankedGameResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
