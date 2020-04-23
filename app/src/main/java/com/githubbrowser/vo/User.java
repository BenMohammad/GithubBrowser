package com.githubbrowser.vo;


import androidx.annotation.NonNull;
import androidx.room.Entity;

import com.google.gson.annotations.SerializedName;

@Entity(primaryKeys = "login")
public class User {

    @SerializedName("login")
    @NonNull
    public final String login;

    @SerializedName("avatar_url")
    public final String avatarUrl;

    @SerializedName("name")
    public final String name;

    @SerializedName("company")
    public final String company;

    @SerializedName("repos_url")
    public final String reposUrl;

    @SerializedName("blog")
    private final String blog;

    public User(@NonNull String login, String avatarUrl, String name, String company, String reposUrl, String blog) {
        this.login = login;
        this.avatarUrl = avatarUrl;
        this.name = name;
        this.company = company;
        this.reposUrl = reposUrl;
        this.blog = blog;
    }
}
