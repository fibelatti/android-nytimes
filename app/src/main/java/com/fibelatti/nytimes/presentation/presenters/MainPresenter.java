package com.fibelatti.nytimes.presentation.presenters;

import com.fibelatti.nytimes.models.MostViewed;

import rx.Observable;

public interface MainPresenter {
    void onCreate();

    void onPause();

    void onResume();

    void onDestroy();

    Observable<MostViewed> getMostViewedArticles(int days);

    void goToSearch();
}
