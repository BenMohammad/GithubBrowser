package com.githubbrowser.repository;

import android.annotation.SuppressLint;
import android.text.format.Time;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.githubbrowser.AppExecutors;
import com.githubbrowser.api.ApiResponse;
import com.githubbrowser.api.GithubService;
import com.githubbrowser.db.GithubDb;
import com.githubbrowser.db.RepoDao;
import com.githubbrowser.util.RateLimiter;
import com.githubbrowser.vo.Contributor;
import com.githubbrowser.vo.Repo;
import com.githubbrowser.vo.Resource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class RepoRepository {

    private final GithubDb db;
    private final RepoDao repoDao;
    private final GithubService githubService;
    private final AppExecutors appExecutors;

    private RateLimiter<String> repoListRateLimit = new RateLimiter<>(10, TimeUnit.MINUTES);

    public RepoRepository(GithubDb db, RepoDao repoDao, GithubService githubService, AppExecutors appExecutors) {
        this.db = db;
        this.repoDao = repoDao;
        this.githubService = githubService;
        this.appExecutors = appExecutors;
    }

    public LiveData<Resource<List<Repo>>> loadRepos(String owner) {
        return new NetworkBoundResource<List<Repo>, List<Repo>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<Repo> item) {
                repoDao.insertRepos(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Repo> data) {
                return data == null || data.isEmpty() || repoListRateLimit.shouldFetch(owner);
            }

            @NonNull
            @Override
            protected LiveData<List<Repo>> loadFromDb() {
                return repoDao.loadRepositories(owner);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<Repo>>> createCall() {
                return githubService.getRepos(owner);
            }
        }.asLiveData();
    }

    public LiveData<Resource<Repo>> loadRepo(String owner, String name) {
        return new NetworkBoundResource<Repo, Repo>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull Repo item) {
                repoDao.insert(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable Repo data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<Repo> loadFromDb() {
                return repoDao.load(owner, name);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<Repo>> createCall() {
                return githubService.getRepo(owner, name);
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<Contributor>>> loadContributors(String owner, String name) {
        return new NetworkBoundResource<List<Contributor>, List<Contributor>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<Contributor> contributors) {
                for(Contributor contributor : contributors) {
                    contributor.setRepoName(name);
                    contributor.setRepoOwner(owner);
                }

                db.beginTransaction();
                try {
                    repoDao.createRepoIfNotExists(new Repo(Repo.UNKNOWN_ID,
                            name, owner + "/" + name, "",
                            new Repo.Owner(owner, null), 0));
                    repoDao.insertContributors(contributors);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                Timber.d("saved contributors to db");
            }


            @Override
            protected boolean shouldFetch(@Nullable List<Contributor> data) {
                Timber.d("contributors list from db %s" , data);
                return data == null || data.isEmpty();
            }

            @NonNull
            @Override
            protected LiveData<List<Contributor>> loadFromDb() {
                return repoDao.loadContributors(owner, name);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<Contributor>>> createCall() {
                return githubService.getContributors(owner, name);
            }
        }.asLiveData();
    }
}