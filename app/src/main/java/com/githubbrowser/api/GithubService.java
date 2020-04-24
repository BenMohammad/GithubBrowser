package com.githubbrowser.api;

import androidx.lifecycle.LiveData;

import com.githubbrowser.vo.Contributor;
import com.githubbrowser.vo.Repo;
import com.githubbrowser.vo.RepoSearchResult;
import com.githubbrowser.vo.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GithubService {

    @GET("users/{login}")
    LiveData<ApiResponse<User>> getUser(@Path("login") String login);

    @GET("users/{login}/repos")
    LiveData<ApiResponse<List<Repo>>> getRepos(@Path("login") String login);

    @GET("repos/{owner}/{name}")
    LiveData<ApiResponse<Repo>> getRepo(@Path("owner") String owner, @Path("name") String name);

    @GET("repos/{owner}/{name}/contributors")
    LiveData<ApiResponse<List<Contributor>>> getContributors(@Path("owner") String owner, @Path("name") String name);

    @GET("search/repositories")
    LiveData<ApiResponse<RepoSearchResult>> searchRepos(@Query("q") String query);

    @GET("search/repositories")
    Call<RepoSearchResponse> searchRepos(@Query("q") String query, @Query("page") int page);
}
