package com.githubbrowser.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.githubbrowser.R;
import com.githubbrowser.binding.FragmentDataBindingComponent;
import com.githubbrowser.databinding.SearchFragmentBinding;
import com.githubbrowser.di.Injectable;
import com.githubbrowser.ui.common.NavigationController;
import com.githubbrowser.ui.common.RepoListAdapter;
import com.githubbrowser.util.AutoClearedValue;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

public class SearchFragment extends Fragment implements Injectable {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    NavigationController navigationController;

    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<SearchFragmentBinding> binding;
    AutoClearedValue<RepoListAdapter> adapter;
    private SearchViewModel searchViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SearchFragmentBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.search_fragment, container, false, dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        searchViewModel = new ViewModelProvider(this, viewModelFactory).get(SearchViewModel.class);
        initRecyclerView();
        RepoListAdapter rvAdapter = new RepoListAdapter(dataBindingComponent,true,  repo ->  navigationController.navigateToRepo(repo.owner.login, repo.name));
        binding.get().repoList.setAdapter(rvAdapter);
        adapter = new AutoClearedValue<>(this, rvAdapter);
        initSearchInputListener();
        binding.get().setCallback(() -> searchViewModel.refresh());
    }

    private void initSearchInputListener() {
        binding.get().input.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch(v);
                return true;
            }
            return false;
        });
    }

    private void doSearch(View v) {
        String query = binding.get().input.getText().toString();
        dismissKeyBoard(v.getWindowToken());
        binding.get().setQuery(query);
        searchViewModel.setQuery(query);
    }

    private void initRecyclerView() {
        binding.get().repoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastPosition = layoutManager.findLastVisibleItemPosition();
                if(lastPosition == adapter.get().getItemCount() -1) {
                    searchViewModel.loadNextPage();
                }
            }
        });
        searchViewModel.getResults().observe(this, result -> {
            binding.get().setSearchResource(result);
            binding.get().setResultCount((result == null || result.data == null) ? 0: result.data.size());
            adapter.get().replace(result == null ? null : result.data);
            binding.get().executePendingBindings();
        });

        searchViewModel.getLoadMoreState().observe(this, loadingMore -> {
            if(loadingMore == null) {
                binding.get().setLoadingMore(false);
            } else {
                binding.get().setLoadingMore(loadingMore.isRunning());
                String error = loadingMore.getErrorMessageIfNotHandled();
                if(error != null) {
                    Snackbar.make(binding.get().loadMoreBar, error, Snackbar.LENGTH_SHORT).show();
                }
            }
            binding.get().executePendingBindings();
        });
    }

    private void dismissKeyBoard(IBinder windowToken){
        FragmentActivity activity = getActivity();
        if(activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(windowToken, 0);

        }
    }

}
