package com.example.semiwiki.Board;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.Path;

public interface BoardService {
    @GET("notice-board/list")
    Call<List<BoardListItemDTO>> getBoardList(
            @Header("Authorization") String bearerToken,
            @Query("keyword") String keyword,
            @Query("categories") List<String> categories,
            @Query("orderBy") String orderBy,
            @Query("offset") Integer offset,
            @Query("limit") Integer limit
    );

    @GET("notice-board/{id}")
    Call<BoardDetailDTO> getBoardDetail(
            @Header("Authorization") String bearerToken,
            @Path("id") long id
    );
}
