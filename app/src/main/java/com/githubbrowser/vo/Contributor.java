package com.githubbrowser.vo;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.google.gson.annotations.SerializedName;

@Entity(primaryKeys = {"repoName", "repoOwner", "login"},
        foreignKeys = @ForeignKey(entity = Repo.class,
                parentColumns = {"name", "owner_login"},
                childColumns = {"repoName", "repoOwner"},
                onUpdate = ForeignKey.CASCADE,
                deferred = true))
public class Contributor {

    @SerializedName("login")
    @NonNull
    private final String login;

    @SerializedName("contributions")
    private final int contributions;

    @SerializedName("avatar_url")
    private final String avatarUrl;

    @NonNull
    private String repoName;

    @NonNull
    private String repoOwner;

    public Contributor(@NonNull String login, int contributions, String avatarUrl) {
        this.login = login;
        this.contributions = contributions;
        this.avatarUrl = avatarUrl;
    }

    @NonNull
    public String getLogin() {
        return login;
    }

    public int getContributions() {
        return contributions;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    @NonNull
    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(@NonNull String repoName) {
        this.repoName = repoName;
    }

    @NonNull
    public String getRepoOwner() {
        return repoOwner;
    }

    public void setRepoOwner(@NonNull String repoOwner) {
        this.repoOwner = repoOwner;
    }
}
