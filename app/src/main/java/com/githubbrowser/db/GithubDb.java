package com.githubbrowser.db;

import android.hardware.SensorEvent;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.githubbrowser.vo.Contributor;
import com.githubbrowser.vo.Repo;
import com.githubbrowser.vo.RepoSearchResult;
import com.githubbrowser.vo.User;

@Database(entities = {User.class, Repo.class, Contributor.class, RepoSearchResult.class}, version = 1)
public abstract class GithubDb extends RoomDatabase {

    abstract public UserDao userDao();

    abstract public RepoDao repoDao();


}
