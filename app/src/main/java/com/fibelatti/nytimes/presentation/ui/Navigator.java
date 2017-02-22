package com.fibelatti.nytimes.presentation.ui;

import android.app.Activity;
import android.content.Intent;

import com.fibelatti.nytimes.presentation.ui.activities.SearchActivity;

public class Navigator {
    Activity activity;

    public Navigator(Activity activity) {
        this.activity = activity;
    }

    public void startSearchActivity() {
        Intent intent = new Intent(activity, SearchActivity.class);
        activity.startActivity(intent);
    }
}
