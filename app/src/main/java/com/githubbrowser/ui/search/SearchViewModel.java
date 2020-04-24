package com.githubbrowser.ui.search;

import android.support.v4.os.IResultReceiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.githubbrowser.repository.RepoRepository;
import com.githubbrowser.util.AbsentLiveData;
import com.githubbrowser.util.Objects;
import com.githubbrowser.vo.Repo;
import com.githubbrowser.vo.Resource;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public class SearchViewModel extends ViewModel {

    private final MutableLiveData<String> query = new MutableLiveData<>();
    private final LiveData<Resource<List<Repo>>> results;
    private final NextPageHandler nextPageHandler;

    @Inject
    SearchViewModel(RepoRepository repoRepository) {
        nextPageHandler = new NextPageHandler(repoRepository);
        results = Transformations.switchMap(query, search -> {
            if(search == null || search.trim().length() == 0) {
                return AbsentLiveData.create();
            } else {
                return repoRepository.search(search);
            }
        });

    }

    @VisibleForTesting
    public LiveData<Resource<List<Repo>>> getResults() {
        return results;
    }

    public void setQuery(@NonNull String originalInput) {
        String input = originalInput.toLowerCase(Locale.getDefault()).trim();
        if(Objects.equals(input, query.getValue())) {
            return;
        }
        nextPageHandler.reset();
        query.setValue(input);
    }

    @VisibleForTesting
    public LiveData<LoadMoreState> getLoadMoreState() {
        return nextPageHandler.getLoadMoreState();
    }

    @VisibleForTesting
    public void loadNextPage() {
        String value = query.getValue();
        if(value == null || value.trim().length() == 0) {
            return;
        }
        nextPageHandler.queryNextPage(value);
    }

    void refresh() {
        if(query.getValue() != null) {
            query.setValue(query.getValue());
        }

    }


    static class LoadMoreState {
        private final boolean running;
        private final String errorMessage;
        private boolean handleError = false;

        public LoadMoreState(boolean running, String errorMessage) {
            this.running = running;
            this.errorMessage = errorMessage;
        }

        boolean isRunning() {
            return running;
        }

        String getErrorMessage() {
            return errorMessage;
        }

        String getErrorMessageIfNotHandled() {
            if(handleError) {
                return null;
            }
            handleError = true;
            return errorMessage;

        }    }
    @VisibleForTesting
    static class NextPageHandler implements Observer<Resource<Boolean>> {

        @Nullable
        private LiveData<Resource<Boolean>> nextPageLiveData;
        private final MutableLiveData<LoadMoreState> loadMoreState = new MutableLiveData<>();
        private String query;
        private final RepoRepository repoRepository;
        @VisibleForTesting
        boolean hasMore;

        @VisibleForTesting
        NextPageHandler(RepoRepository repoRepository) {
            this.repoRepository = repoRepository;
            reset();
        }

        void queryNextPage(String query) {
            if(Objects.equals(this.query, query)) {
                return;
            }
            unregister();
            this.query = query;
            nextPageLiveData = repoRepository.searchNextPage(query);
            loadMoreState.setValue(new LoadMoreState(true, null));
            nextPageLiveData.observeForever(this);
        }

        @Override
        public void onChanged(Resource<Boolean> booleanResource) {
            if(booleanResource == null) {
                reset();
            } else {
                switch(booleanResource.status) {
                    case SUCCESS:
                        hasMore = Boolean.TRUE.equals(booleanResource.data);
                        unregister();
                        loadMoreState.setValue(new LoadMoreState(false, null));
                        break;
                    case ERROR:
                        hasMore = true;
                        unregister();
                        loadMoreState.setValue(new LoadMoreState(false,  booleanResource.message));
                        break;

                }
            }
        }

        private void unregister() {
            if(nextPageLiveData != null) {
               nextPageLiveData.removeObserver(this);
               nextPageLiveData = null;
               if(hasMore) {
                   query = null;
               }
            }
        }

        private void reset() {
            unregister();
            hasMore = true;
            loadMoreState.setValue(new LoadMoreState(false, null));

        }

        MutableLiveData<LoadMoreState> getLoadMoreState() {
            return loadMoreState;
        }
    }
}
