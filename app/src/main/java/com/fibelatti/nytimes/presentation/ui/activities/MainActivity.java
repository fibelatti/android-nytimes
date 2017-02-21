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
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.annimon.stream.Stream;
import com.fibelatti.nytimes.R;
import com.fibelatti.nytimes.models.MostViewed;
import com.fibelatti.nytimes.presentation.presenters.MainPresenter;
import com.fibelatti.nytimes.presentation.presenters.impl.MainPresenterImpl;
import com.fibelatti.nytimes.presentation.ui.adapters.MostViewedAdapter;
import com.github.clans.fab.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity
        extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int DAYS_ONE = 1;
    private static final int DAYS_SEVEN = 7;
    private static final int DAYS_THIRTY = 30;

    private Context context;

    private MainPresenter presenter;
    private MostViewedAdapter mostViewedAdapter;

    private Subscription mostViewedSubscription;

    //region layout bindings
    @BindView(R.id.root_layout)
    CoordinatorLayout rootLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.layout_placeholder)
    RelativeLayout layoutPlaceholder;
    @BindView(R.id.layout_content)
    RelativeLayout layoutContent;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.recycler_view_articles)
    RecyclerView recyclerViewArticles;
    @BindView(R.id.fab_search)
    FloatingActionButton fab;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        presenter = MainPresenterImpl.createPresenter(this);
        mostViewedAdapter = new MostViewedAdapter(this);

        presenter.onCreate();

        setUpLayout();
        setUpRecyclerView();
        setUpFab();
        setUpValues();
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

        refreshData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();

        if (!mostViewedSubscription.isUnsubscribed()) mostViewedSubscription.unsubscribe();
    }

    private void setUpLayout() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
    }

    private void setUpValues() {
        setTitle(getString(R.string.main_title));
    }

    private void setUpRecyclerView() {
        swipeRefresh.setOnRefreshListener(this::refreshData);

        recyclerViewArticles.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewArticles.setItemAnimator(new DefaultItemAnimator());
        recyclerViewArticles.setAdapter(mostViewedAdapter);
    }

    private void setUpFab() {
        fab.setOnClickListener(view -> presenter.goToSearch());
        fab.setShowAnimation(AnimationUtils.loadAnimation(this, R.anim.show_from_bottom));
        fab.setHideAnimation(AnimationUtils.loadAnimation(this, R.anim.hide_to_bottom));
    }

    private void refreshData() {
        if (mostViewedSubscription != null && !mostViewedSubscription.isUnsubscribed())
            mostViewedSubscription.unsubscribe();

        mostViewedSubscription = presenter.getMostViewedArticles(DAYS_ONE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> mostViewedAdapter.clearArticleList())
                .doOnCompleted(this::hideLoadAndNotifyChanges)
                .subscribe(this::addResultsToList, this::handleError);
    }

    private void hideLoadAndNotifyChanges() {
        swipeRefresh.setRefreshing(false);
        mostViewedAdapter.notifyDataSetChanged();
    }

    private void addResultsToList(MostViewed mostViewed) {
        Stream.of(mostViewed.getResults())
                .forEach(mostViewedResult -> mostViewedAdapter
                        .addArticleToList(mostViewedResult));

        showContentLayout();
    }

    private void handleError(Throwable throwable) {
        showPlaceholderLayout();

        Log.e(getString(R.string.generic_log_error, TAG, "refreshData"), throwable.getMessage());
    }

    private void showContentLayout() {
        layoutContent.setVisibility(View.VISIBLE);
        layoutPlaceholder.setVisibility(View.GONE);
    }

    private void showPlaceholderLayout() {
        layoutContent.setVisibility(View.GONE);
        layoutPlaceholder.setVisibility(View.VISIBLE);
    }
}
