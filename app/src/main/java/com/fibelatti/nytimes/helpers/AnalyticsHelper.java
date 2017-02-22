package com.fibelatti.nytimes.helpers;

public interface AnalyticsHelper {
    void fireRefreshMostViewedEvent();

    void fireLoadNewPageEvent(int pageRequested);

    void fireQueryArticlesEvent(String query);
}
