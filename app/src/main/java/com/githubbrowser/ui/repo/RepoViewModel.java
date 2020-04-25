package com.githubbrowser.ui.repo;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.githubbrowser.databinding.RepoItemBinding;
import com.githubbrowser.repository.RepoRepository;
import com.githubbrowser.util.AbsentLiveData;
import com.githubbrowser.util.Objects;
import com.githubbrowser.vo.Contributor;
import com.githubbrowser.vo.Repo;
import com.githubbrowser.vo.Resource;

import java.util.List;

import javax.inject.Inject;

public class RepoViewModel extends ViewModel {

    @VisibleForTesting
    final MutableLiveData<RepoId> repoId;
    private final LiveData<Resource<Repo>> repo;
    private final LiveData<Resource<List<Contributor>>> contributors;

    @Inject
    public RepoViewModel(RepoRepository repoRepository) {
        this.repoId = new MutableLiveData<>();
        repo = Transformations.switchMap(repoId, input -> {
            if(input.isEmpty()) {
                return AbsentLiveData.create();
            }
            return repoRepository.loadRepo(input.owner, input.name);
        });
        contributors = Transformations.switchMap(repoId, input -> {
            if(input.isEmpty()) {
                return AbsentLiveData.create();
            }
            return repoRepository.loadContributors(input.owner, input.name);
        });
    }

    public LiveData<Resource<Repo>> getRepo() {
        return repo;
    }

    public LiveData<Resource<List<Contributor>>> getContributors() {
        return contributors;
    }

    public void retry() {
        RepoId current = repoId.getValue();
        if(current != null && !current.isEmpty()) {
            repoId.setValue(current);
        }
    }

    @VisibleForTesting
    public void setId(String owner, String name) {
        RepoId update = new RepoId(owner, name);
        if(Objects.equals(repoId.getValue(), update)){
            return;
        }
        repoId.setValue(update);
    }

    @VisibleForTesting
    static class RepoId {
        public final String owner;
        public final String name;

        public RepoId(String owner, String name) {
            this.owner = owner;
            this.name = name;
        }

        boolean isEmpty() {
            return owner == null || name == null || owner.length() == 0|| name.length() == 0;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if(this == obj) {
                return true;
            }
            if(obj == null|| getClass() != obj.getClass()) {
                return false;
            }

            RepoId repoId = (RepoId)obj;

            if(owner != null ? !owner.equals(repoId.owner) : repoId.owner != null) {
                return false;
            }
            return name != null ? name.equals(repoId.name) : repoId.name == null;
        }

        @Override
        public int hashCode() {
            int result = owner != null ? owner.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode(): 0);
            return result;
        }
    }
}
