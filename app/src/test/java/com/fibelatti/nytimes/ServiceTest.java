package com.fibelatti.nytimes;

import android.content.Context;

import com.annimon.stream.Stream;
import com.fibelatti.nytimes.models.MostViewed;
import com.fibelatti.nytimes.models.MostViewedResult;
import com.fibelatti.nytimes.models.Search;
import com.fibelatti.nytimes.models.SearchDoc;
import com.fibelatti.nytimes.presentation.presenters.MainPresenter;
import com.fibelatti.nytimes.presentation.presenters.SearchPresenter;
import com.fibelatti.nytimes.presentation.presenters.impl.MainPresenterImpl;
import com.fibelatti.nytimes.presentation.presenters.impl.SearchPresenterImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.android.schedulers.AndroidSchedulers;
import rx.observers.TestSubscriber;
import rx.plugins.RxJavaHooks;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServiceTest {
    @Mock
    Context fakeContext;
    MainPresenter mainPresenter;
    SearchPresenter searchPresenter;

    @Before
    public void setUp() throws Exception {
        // Override RxJava schedulers
        RxJavaHooks.setOnIOScheduler(scheduler -> Schedulers.immediate());

        RxJavaHooks.setOnComputationScheduler(scheduler -> Schedulers.immediate());

        RxJavaHooks.setOnNewThreadScheduler(scheduler -> Schedulers.immediate());

        // Override RxAndroid schedulers
        final RxAndroidPlugins rxAndroidPlugins = RxAndroidPlugins.getInstance();
        rxAndroidPlugins.registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });

        mainPresenter = MainPresenterImpl.createPresenter(fakeContext);
        searchPresenter = SearchPresenterImpl.createPresenter(fakeContext);
    }

    @After
    public void tearDown() throws Exception {
        RxJavaHooks.reset();
        RxAndroidPlugins.getInstance().reset();

        mainPresenter = null;
        searchPresenter = null;
    }

    @Test
    public void getMostViewedArticlesWithApiKey() throws Exception {
        // Adding sleep to avoid HTTP 429 when running all tests
        Thread.sleep(3000);

        TestSubscriber<MostViewed> testSubscriber = new TestSubscriber<>();

        mainPresenter.getMostViewedArticles(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        assertEquals(testSubscriber.getValueCount(), 1);

        Stream.of(testSubscriber.getOnNextEvents())
                .forEach(this::assertMostViewed);
    }

    private void assertMostViewed(MostViewed mostViewed) {
        assertEquals(mostViewed.getStatus().toUpperCase(), "OK");
        assertTrue(mostViewed.getResults().size() > 0);

        Stream.of(mostViewed.getResults())
                .forEach(this::assertMortViewedResult);
    }

    private void assertMortViewedResult(MostViewedResult mostViewedResult) {
        assertFalse(mostViewedResult.getTitle().isEmpty());
        assertFalse(mostViewedResult.getPublishedDate().isEmpty());
        assertFalse(mostViewedResult.getAbstract().isEmpty());

        Stream.of(mostViewedResult.getMedia())
                .filter(m -> m.getType().equals("image"))
                .findFirst()
                .ifPresent(medium -> Stream.of(medium.getMediaMetadata())
                        .filter(m -> m.getFormat().equals("square320"))
                        .findFirst()
                        .ifPresent(metadata -> assertFalse(metadata.getUrl().isEmpty())));
    }

    @Test
    public void searchArticlesWithApiKey() throws Exception {
        // Adding sleep to avoid HTTP 429 when running all tests
        Thread.sleep(3000);

        TestSubscriber<Search> testSubscriber = new TestSubscriber<>();

        searchPresenter.searchArticles("SpaceX", 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        assertEquals(testSubscriber.getValueCount(), 1);

        Stream.of(testSubscriber.getOnNextEvents())
                .forEach(this::assertSearch);
    }

    private void assertSearch(Search search) {
        assertEquals(search.getStatus().toUpperCase(), "OK");
        assertTrue(search.getResponse().getDocs().size() > 0);

        Stream.of(search.getResponse().getDocs())
                .forEach(this::assertMortViewedResult);
    }

    private void assertMortViewedResult(SearchDoc searchDoc) {
        String title = searchDoc.getHeadline().getPrintHeadline() != null ?
                searchDoc.getHeadline().getPrintHeadline() : searchDoc.getHeadline().getMain();

        assertFalse(title.isEmpty());
        assertFalse(searchDoc.getPubDate().isEmpty());
        assertFalse(searchDoc.getLeadParagraph().isEmpty());

        Stream.of(searchDoc.getMultimedia())
                .filter(m -> m.getType().equals("image"))
                .findFirst()
                .ifPresent(media -> assertFalse(media.getUrl().isEmpty()));
    }
}
