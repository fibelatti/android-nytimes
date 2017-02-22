package com.fibelatti.nytimes.presentation.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.annimon.stream.Stream;
import com.fibelatti.nytimes.R;
import com.fibelatti.nytimes.helpers.AnalyticsHelper;
import com.fibelatti.nytimes.helpers.impl.AnalyticsHelperImpl;
import com.fibelatti.nytimes.models.Search;
import com.fibelatti.nytimes.presentation.presenters.SearchPresenter;
import com.fibelatti.nytimes.presentation.presenters.impl.SearchPresenterImpl;
import com.fibelatti.nytimes.presentation.ui.adapters.SearchAdapter;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchActivity
        extends AppCompatActivity {
    public static final String TAG = SearchActivity.class.getSimpleName();

    private Context context;

    private AnalyticsHelper analyticsHelper;

    private SearchPresenter presenter;
    private SearchAdapter searchAdapter;

    private Subscription searchSubscription;

    private int currentPage;
    private String query;

    private boolean isLoading;
    int pastVisibleItems, visibleItemCount, totalItemCount;

    //region layout bindings
    @BindView(R.id.root_layout)
    CoordinatorLayout rootLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.edit_text_query)
    EditText searchBar;
    @BindView(R.id.layout_placeholder)
    RelativeLayout layoutPlaceholder;
    @BindView(R.id.layout_placeholder_no_results)
    RelativeLayout layoutPlaceholderNoResults;
    @BindView(R.id.layout_content)
    RelativeLayout layoutContent;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.recycler_view_articles)
    RecyclerView recyclerViewArticles;
    @BindView(R.id.layout_search)
    LinearLayout layoutSearch;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        analyticsHelper = AnalyticsHelperImpl.getInstance();

        presenter = SearchPresenterImpl.createPresenter(this);
        searchAdapter = new SearchAdapter(this);

        presenter.onCreate();

        setUpLayout();
        setUpRecyclerView();
        setUpValues();
        setUpSearchBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();

        if (searchSubscription != null && !searchSubscription.isUnsubscribed())
            searchSubscription.unsubscribe();
    }

    private void setUpLayout() {
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
    }

    private void setUpValues() {
        setTitle(getString(R.string.search_title));

        this.currentPage = 1;
    }

    private void setUpSearchBar() {
        RxTextView.textChanges(searchBar)
                .debounce(500, TimeUnit.MILLISECONDS)
                .map(CharSequence::toString)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    query = s;
                    if (query.isEmpty()) {
                        hideAllLayouts();
                    } else {
                        refreshData();
                    }
                });

        requestFocus(searchBar);

        searchBar.setOnEditorActionListener((v, keyCode, event) -> {
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    private void setUpRecyclerView() {
        swipeRefresh.setOnRefreshListener(this::refreshData);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        recyclerViewArticles.setLayoutManager(linearLayoutManager);
        recyclerViewArticles.setItemAnimator(new DefaultItemAnimator());
        recyclerViewArticles.setAdapter(searchAdapter);

        recyclerViewArticles.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = linearLayoutManager.getChildCount();
                totalItemCount = linearLayoutManager.getItemCount();
                pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (!isLoading && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    fetchNextPage();
                }
            }
        });
    }

    private void refreshData() {
        this.currentPage = 1;

        analyticsHelper.fireQueryArticlesEvent(query);

        if (searchSubscription != null && !searchSubscription.isUnsubscribed())
            searchSubscription.unsubscribe();

        searchSubscription = presenter.searchArticles(query, currentPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this::clearListAndShowLoading)
                .doOnCompleted(this::hideLoadAndNotifyChanges)
                .subscribe(this::addResultsToList, this::handleError);
    }

    private void fetchNextPage() {
        this.currentPage++;

        analyticsHelper.fireLoadNewPageEvent(currentPage);

        if (searchSubscription != null && !searchSubscription.isUnsubscribed())
            searchSubscription.unsubscribe();

        searchSubscription = presenter.searchArticles(query, currentPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this::showLoading)
                .doOnCompleted(this::hideLoadAndNotifyChanges)
                .subscribe(this::addResultsToList, this::handleError);
    }

    private void clearListAndShowLoading() {
        searchAdapter.clearArticleList();
        swipeRefresh.setRefreshing(false);

        showLoading();
    }

    private void showLoading() {
        isLoading = true;
        searchAdapter.showLoadingItem();
    }

    private void hideLoadAndNotifyChanges() {
        isLoading = false;
        searchAdapter.hideLoadingItem();
        searchAdapter.notifyDataSetChanged();
    }

    private void addResultsToList(Search search) {
        if (search.getResponse().getDocs().size() == 0) {
            showPlaceholderNoResultsLayout();
        } else {
            Stream.of(search.getResponse().getDocs())
                    .forEach(searchDoc -> searchAdapter
                            .addArticleToList(searchDoc));

            showContentLayout();
        }
    }

    private void handleError(Throwable throwable) {
        showPlaceholderLayout();

        Log.e(getString(R.string.generic_log_error, TAG, "refreshData"), throwable.getMessage());
    }

    private void showContentLayout() {
        layoutContent.setVisibility(View.VISIBLE);
        layoutPlaceholder.setVisibility(View.GONE);
        layoutPlaceholderNoResults.setVisibility(View.GONE);
    }

    private void showPlaceholderLayout() {
        layoutContent.setVisibility(View.GONE);
        layoutPlaceholder.setVisibility(View.VISIBLE);
        layoutPlaceholderNoResults.setVisibility(View.GONE);
    }

    private void showPlaceholderNoResultsLayout() {
        layoutContent.setVisibility(View.GONE);
        layoutPlaceholder.setVisibility(View.GONE);
        layoutPlaceholderNoResults.setVisibility(View.VISIBLE);
    }

    private void hideAllLayouts() {
        layoutContent.setVisibility(View.GONE);
        layoutPlaceholder.setVisibility(View.GONE);
        layoutPlaceholderNoResults.setVisibility(View.GONE);
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @OnClick(R.id.button_try_again)
    public void tryReload() {
        refreshData();
    }
}
