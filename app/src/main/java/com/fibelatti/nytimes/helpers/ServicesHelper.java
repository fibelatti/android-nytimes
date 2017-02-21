package com.fibelatti.nytimes.helpers;

import com.fibelatti.nytimes.network.ArticleService;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServicesHelper {
    private static final Object syncLock = new Object();

    private static Retrofit retrofit;
    private static ArticleService articleService;

    private ServicesHelper() {
    }

    public static ArticleService getArticleService() {
        if (articleService == null) {
            synchronized (syncLock) {
                if (articleService == null) {
                    if (retrofit == null) setUpRetrofit();
                    articleService = retrofit.create(ArticleService.class);
                }
            }
        }
        return articleService;
    }

    private static void setUpRetrofit() {
        retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ArticleService.BASE_URL)
                .build();
    }
}
