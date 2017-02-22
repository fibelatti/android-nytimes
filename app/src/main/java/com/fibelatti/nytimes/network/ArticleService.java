package com.fibelatti.nytimes.network;

import com.fibelatti.nytimes.models.MostViewed;
import com.fibelatti.nytimes.models.Search;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface ArticleService {
    String BASE_URL = "https://api.nytimes.com/svc/";
    String BASE_IMG_URL = "https://static01.nyt.com/";

    @GET("mostpopular/v2/mostviewed/all-sections/{amount_of_days}.json")
    Observable<MostViewed> getMostViewedArticles(
            @Path("amount_of_days") Integer days,
            @Query("api-key") String apiKey
    );

    @GET("search/v2/articlesearch.json")
    Observable<Search> searchArticles(
            @Query("api-key") String apiKey,
            @Query("q") String query,
            @Query("page") Integer pageNumber
    );
}
