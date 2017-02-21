package com.fibelatti.nytimes.presentation.presenters.impl;

import android.app.Activity;
import android.content.Context;

import com.fibelatti.nytimes.BuildConfig;
import com.fibelatti.nytimes.helpers.ServicesHelper;
import com.fibelatti.nytimes.models.MostViewed;
import com.fibelatti.nytimes.presentation.presenters.MainPresenter;
import com.fibelatti.nytimes.presentation.ui.Navigator;

import rx.Observable;

public class MainPresenterImpl
        implements MainPresenter {

    private final int DAYS_ONE = 1;
    private final int DAYS_SEVEN = 7;
    private final int DAYS_THIRTY = 30;

    private Context context;
    private Navigator navigator;

    private MainPresenterImpl(Context context) {
        this.context = context;
    }

    public static MainPresenterImpl createPresenter(Context context) {
        return new MainPresenterImpl(context);
    }

    @Override
    public void onCreate() {
        this.navigator = new Navigator((Activity) context);
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
    public Observable<MostViewed> getMostViewedArticles(int days) {
        return ServicesHelper
                .getArticleService()
                .getMostViewedArticles(
                        DAYS_ONE,
                        BuildConfig.NYTIMES_API_KEY);
    }

    @Override
    public void goToSearch() {
        navigator.startSearchActivity();
    }
}
