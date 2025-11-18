package com.example.semiwiki.Comment;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface CommentService {
    @GET("/comment/{boardId}")
    Call<List<CommentResponse>> getComments(
            @Path("boardId") long boardId,
            @Header("Authorization") String authorization
    );
}
