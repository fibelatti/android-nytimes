package com.fibelatti.nytimes.helpers.impl;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.fibelatti.nytimes.helpers.AnalyticsHelper;

public class AnalyticsHelperImpl
        implements AnalyticsHelper {
    private final String ANALYTICS_KEY_REFRESH_MOST_VIEWED = "Refresh most viewed articles";

    private final String ANALYTICS_KEY_LOADED_NEW_PAGE = "Requested a new page";
    private final String ANALYTICS_PARAM_PAGE_NUMBER = "Page number";

    private final String ANALYTICS_KEY_PERFORMED_SEARCH = "Searched for articles";
    private final String ANALYTICS_PARAM_QUERY = "Query";

    private static AnalyticsHelperImpl instance;

    private AnalyticsHelperImpl() {
    }

    public static AnalyticsHelperImpl getInstance() {
        if (instance == null) {
            instance = new AnalyticsHelperImpl();
        }
        return instance;
    }

    @Override
    public void fireRefreshMostViewedEvent() {
        Answers.getInstance().logCustom(new CustomEvent(ANALYTICS_KEY_REFRESH_MOST_VIEWED));
    }

    @Override
    public void fireLoadNewPageEvent(int pageRequested) {
        Answers.getInstance().logCustom(new CustomEvent(ANALYTICS_KEY_LOADED_NEW_PAGE)
                .putCustomAttribute(ANALYTICS_PARAM_PAGE_NUMBER, pageRequested));
    }

    @Override
    public void fireQueryArticlesEvent(String query) {
        Answers.getInstance().logCustom(new CustomEvent(ANALYTICS_KEY_PERFORMED_SEARCH)
                .putCustomAttribute(ANALYTICS_PARAM_QUERY, query));
    }
}
