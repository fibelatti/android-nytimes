package com.fibelatti.nytimes.presentation.presenters.impl;

import android.content.Context;

import com.fibelatti.nytimes.BuildConfig;
import com.fibelatti.nytimes.helpers.ServicesHelper;
import com.fibelatti.nytimes.models.Search;
import com.fibelatti.nytimes.presentation.presenters.SearchPresenter;

import rx.Observable;

public class SearchPresenterImpl
        implements SearchPresenter {

    private Context context;

    private SearchPresenterImpl(Context context) {
        this.context = context;
    }

    public static SearchPresenterImpl createPresenter(Context context) {
        return new SearchPresenterImpl(context);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public Observable<Search> searchArticles(String query, int pageNumber) {
        return ServicesHelper
                .getArticleService()
                .searchArticles(
                        BuildConfig.NYTIMES_API_KEY,
                        query,
                        pageNumber);
    }
}
