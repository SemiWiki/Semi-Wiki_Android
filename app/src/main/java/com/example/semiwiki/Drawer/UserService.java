package com.example.semiwiki.Drawer;

import com.example.semiwiki.Board.BoardListItemDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserService {

    @GET("user/{accountId}")
    Call<MyPageDTO> getMyPage(
            @Header("Authorization") String bearerToken,
            @Path("accountId") String accountId
    );

    @GET("user/{accountId}/list")
    Call<List<BoardListItemDTO>> getUserPosts(
            @Header("Authorization") String bearerToken,
            @Path("accountId") String accountId,
            @Query("orderBy") String orderBy,
            @Query("offset") Integer offset,
            @Query("limit") Integer limit
    );

    @GET("user/{accountId}/list/like")
    Call<List<BoardListItemDTO>> getUserLikedPosts(
            @Header("Authorization") String bearerToken,
            @Path("accountId") String accountId,
            @Query("offset") Integer offset,
            @Query("limit") Integer limit
    );
}
