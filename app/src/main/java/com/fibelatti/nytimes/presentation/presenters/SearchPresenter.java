package com.fibelatti.nytimes.presentation.presenters;

import com.fibelatti.nytimes.models.Search;

import rx.Observable;

public interface SearchPresenter {
    void onCreate();

    void onPause();

    void onResume();

    void onDestroy();

    Observable<Search> searchArticles(String query, int pageNumber);
}
