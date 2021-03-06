/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016. Dmytro Karataiev
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.adkdevelopment.moviesdb.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adkdevelopment.moviesdb.App;
import com.adkdevelopment.moviesdb.BuildConfig;
import com.adkdevelopment.moviesdb.R;
import com.adkdevelopment.moviesdb.data.model.Backdrops;
import com.adkdevelopment.moviesdb.data.model.Consts;
import com.adkdevelopment.moviesdb.data.model.MovieCast;
import com.adkdevelopment.moviesdb.data.model.MovieCredits;
import com.adkdevelopment.moviesdb.data.model.MovieObject;
import com.adkdevelopment.moviesdb.data.model.MovieResults;
import com.adkdevelopment.moviesdb.data.model.Review;
import com.adkdevelopment.moviesdb.data.model.Trailer;
import com.adkdevelopment.moviesdb.data.remote.ApiService;
import com.adkdevelopment.moviesdb.ui.adapters.ActorsAdapter;
import com.adkdevelopment.moviesdb.ui.base.BaseFragment;
import com.adkdevelopment.moviesdb.ui.interfaces.ItemClickListener;
import com.adkdevelopment.moviesdb.utils.DatabaseTasks;
import com.adkdevelopment.moviesdb.utils.Utility;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Detailed Movie Fragment with poster, rating, description.
 * Created by karataev on 12/15/15.
 */
public class DetailFragment extends BaseFragment
        implements YouTubePlayer.OnInitializedListener, ItemClickListener<MovieCast, View> {

    private final String TAG = DetailFragment.class.getSimpleName();

    // String which is used in share intent
    private String mMovie;
    private MovieObject mMovieObject;
    private ShareActionProvider mShareActionProvider;

    // YouTube variables
    private YouTubePlayer mYouTubePlayer;
    private YouTubePlayerSupportFragment youTubePlayerSupportFragment;

    // save current video and position
    private int currentVideoMillis;
    private int currentVideo;
    private final String VIDEO_TAG = "youtube";
    private final String VIDEO_NUM = "video_num";

    // list of videos
    private List<String> mTrailersList;
    private List<String> mReviewsList;

    @BindView(R.id.movie_poster)
    ImageView mImagePoster;
    @BindView(R.id.movie_item_spinner)
    ProgressBar mProgressSpinner;
    @BindView(R.id.movie_poster_favorite)
    ImageView mImageFavorite;
    @BindView(R.id.detail_releasedate_textview)
    TextView mTextRelease;
    @BindView(R.id.detail_rating_textview)
    TextView mTextRating;
    @BindView(R.id.detail_description_textview)
    TextView mTextDescription;
    @BindView(R.id.detail_votecount_textview)
    TextView mTextVotes;
    @BindView(R.id.detail_background)
    NestedScrollView mLinearBackground;
    @BindView(R.id.detail_reviews_textview)
    TextView mTextReviews;
    @Nullable
    @BindView(R.id.backdrop)
    ImageView mImageBackdrop;
    @BindView(R.id.detail_runtime)
    TextView mTextRuntime;
    @BindView(R.id.detail_genres)
    TextView mTextGenres;
    @BindView(R.id.detail_production)
    TextView mTextProduction;
    @BindView(R.id.detail_imdb)
    TextView mTextImdb;
    @BindView(R.id.detail_budget)
    TextView mTextBudget;
    @BindView(R.id.detail_tagline)
    TextView mTextTagline;

    private Unbinder mUnbinder;

    // Actors RecyclerList view
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerActors;
    private ActorsAdapter mActorsAdapter;

    // RxJava
    private ApiService mApiService;
    private CompositeSubscription mSubscriptions;

    // Callback inside of Picasso Call
    private final Callback callback = new Callback() {
        @Override
        public void onSuccess() {
            mProgressSpinner.setVisibility(View.GONE);
            mImageFavorite.setVisibility(View.VISIBLE);

            // On mImageFavorite icon click
            mImageFavorite.setOnClickListener(v -> {

                ContentValues favValue = Utility.makeContentValues(mMovieObject);

                Toast.makeText(getContext(), mMovieObject.getTitle(), Toast.LENGTH_LONG).show();

                if (!Utility.isFavorite(getContext(), mMovieObject)) {
                    mImageFavorite.setImageResource(R.drawable.ic_bookmark_fav);

                    // Insert on background thread
                    DatabaseTasks databaseTasks = new DatabaseTasks(getContext());
                    databaseTasks.execute(DatabaseTasks.INSERT, favValue);
                } else {
                    mImageFavorite.setImageResource(R.drawable.ic_bookmark);

                    // Delete on background thread
                    DatabaseTasks databaseTasks = new DatabaseTasks(getContext());
                    databaseTasks.execute(DatabaseTasks.DELETE, favValue);
                }
            });
        }

        @Override
        public void onError() {
            mImagePoster.setBackgroundResource(R.color.white);
            mProgressSpinner.setVisibility(View.GONE);
            mImageFavorite.setVisibility(View.GONE);
        }
    };

    public DetailFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DetailFragment newInstance() {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mApiService = App.getApiManager().getMoviesService();
        mSubscriptions = new CompositeSubscription();

        mReviewsList = new ArrayList<>();
        mTrailersList = new ArrayList<>();

        if (savedInstanceState != null && savedInstanceState.containsKey(VIDEO_TAG)) {
            // Get video progress
            currentVideoMillis = savedInstanceState.getInt(VIDEO_TAG);
            currentVideo = savedInstanceState.getInt(VIDEO_NUM);
        }

        Intent intent;

        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovieObject = arguments.getParcelable(MovieObject.MOVIE_OBJECT);
        } else if (getActivity().getIntent().hasExtra(Consts.MOVIE_ID)) {
            String movieId = getActivity().getIntent().getStringExtra(Consts.MOVIE_ID);
            mSubscriptions.add(App.getApiManager().getMoviesService()
                    .getMovie(movieId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<MovieObject>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(MovieObject movieObject) {
                            // TODO: 5/28/16 refactor
                            mMovieObject = movieObject;
                            mMovieObject.makeNice(getContext());
                            loadData();
                            initRecycler();
                        }
                    }));
        } else {
            // Gets data from intent (using parcelable) and populates views
            intent = this.getActivity().getIntent();
            mMovieObject = intent.getParcelableExtra(MovieObject.MOVIE_OBJECT);
            if (mMovieObject == null) {
                mSubscriptions.add(App.getApiManager()
                        .getMoviesService()
                        .getMoviesSort(Utility.getSort(getContext()))
                        .subscribe(new Subscriber<MovieResults>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(MovieResults movieResults) {
                                List<MovieObject> movies = movieResults.getResults();
                                if (movies != null && movies.size() > 0) {
                                    mMovieObject = movies.get(0);
                                }
                            }
                        }));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mUnbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: 5/28/16 refactor
        if (mMovieObject != null) {
            loadData();
            initRecycler();
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail_fragment, menu);

        // Retrieve the share menu item
        MenuItem item = menu.findItem(R.id.share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(movieIntent());
        } else {
            Log.e(TAG, "fail to set a share intent");
        }
    }

    /**
     * Method to populate Intent with data
     *
     * @return Intent with data to external apps
     */
    private Intent movieIntent() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, mMovie + "\n#Pop Movie App");

        return sendIntent;
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {

        try {
            mYouTubePlayer = player;
            // Detect if display is in landscape mode and set YouTube layout height accordingly
            TypedValue tv = new TypedValue();

            if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true) &&
                    (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)) {
                int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());

                if (getView() != null) {
                    FrameLayout youtubeFrame = (FrameLayout) getView().findViewById(R.id.youtube_fragment);
                    ViewGroup.LayoutParams layoutParams = youtubeFrame.getLayoutParams();

                    layoutParams.height = Utility.screenSize(getContext())[1] - (3 * actionBarHeight);
                    layoutParams.width = Utility.screenSize(getContext())[0];
                    youtubeFrame.setLayoutParams(layoutParams);
                }
            }

            player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                @Override
                public void onLoading() {

                }

                @Override
                public void onLoaded(String s) {
                    currentVideo = mTrailersList.indexOf(s);
                }

                @Override
                public void onAdStarted() {

                }

                @Override
                public void onVideoStarted() {

                }

                @Override
                public void onVideoEnded() {

                }

                @Override
                public void onError(YouTubePlayer.ErrorReason errorReason) {

                }
            });
            if (!wasRestored) {
                if (mTrailersList != null && mTrailersList.size() > 0) {
                    mYouTubePlayer.cueVideos(mTrailersList, currentVideo, currentVideoMillis);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "1 e:" + e);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        // Save YouTube progress
        if (mYouTubePlayer != null) {
            try {
                saveInstanceState.putInt(VIDEO_TAG, mYouTubePlayer.getCurrentTimeMillis());
                saveInstanceState.putInt(VIDEO_NUM, currentVideo);
            } catch (Exception e) {
                Log.e(TAG, "2 YouTube state error:" + e);
            }
        }

    }

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(getActivity(), RECOVERY_DIALOG_REQUEST).show();
        } else {
            Toast.makeText(getActivity(), "YouTube Initialisation error", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            try {
                getYouTubePlayerProvider().initialize(BuildConfig.YOUTUBE_API_KEY, this);
            } catch (Exception e) {
                Log.d("DetailFragment", "e:" + e);
            }
        }
    }

    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerSupportFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        if (mYouTubePlayer != null) {
            mYouTubePlayer.release();
        }
        if (mSubscriptions != null && !mSubscriptions.isUnsubscribed()) {
            mSubscriptions.unsubscribe();
        }

    }

    /**
     * Loads data
     */
    private void loadData() {
        // ActionBar title and image adding
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(mMovieObject.getTitle());
        }

        if (mImageBackdrop == null) {
            mImageBackdrop = ButterKnife.findById(getActivity(), R.id.backdrop);
        }

        Picasso.with(getContext()).load(mMovieObject.getPosterPath()).into(mImagePoster, callback);

        mTextDescription.setText(mMovieObject.getOverview());
        mTextRating.setText(String.format(getString(R.string.movie_rating), mMovieObject.getVoteAverage()));
        mTextRelease.setText(mMovieObject.getReleaseDate());
        mTextVotes.setText(String.format(getString(R.string.votes_text), mMovieObject.getVoteCount()));

        if (Utility.isFavorite(getContext(), mMovieObject)) {
            Picasso.with(getContext()).load(R.drawable.ic_bookmark_fav).into(mImageFavorite);
        } else {
            Picasso.with(getContext()).load(R.drawable.ic_bookmark).into(mImageFavorite);
        }

        // Initializes mMovie with info about a movie
        mMovie = mMovieObject.getTitle() +
                "\n" + mMovieObject.getReleaseDate() +
                "\n" + mMovieObject.getVoteAverage() +
                "\n" + mMovieObject.getOverview();
    }

    private void initRecycler() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),
                RecyclerView.HORIZONTAL, false);
        mActorsAdapter = new ActorsAdapter(getContext(), null);

        mRecyclerActors.setLayoutManager(layoutManager);
        mRecyclerActors.setAdapter(mActorsAdapter);
        mRecyclerActors.setNestedScrollingEnabled(true);

        // downloads all the details about a movie
        mSubscriptions.add(mApiService.getMovie(String.valueOf(mMovieObject.getId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MovieObject>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(MovieObject movieObject) {
                        if (movieObject.getBudget() > 0) {
                            mTextBudget.setVisibility(View.VISIBLE);
                            mTextBudget.setText(getString(R.string.movie_budget,
                                    Utility.currencyFormat(movieObject.getBudget())));
                        }

                        if (movieObject.getRuntime() > 0) {
                            mTextRuntime.setVisibility(View.VISIBLE);
                            mTextRuntime.setText(Utility.getRuntimeString(movieObject.getRuntime()));
                        }

                        if (!movieObject.getTagline().isEmpty()) {
                            mTextTagline.setVisibility(View.VISIBLE);
                            mTextTagline.setText(movieObject.getTagline());
                        }

                        if (!movieObject.getImdbId().isEmpty()) {
                            mTextImdb.setVisibility(View.VISIBLE);
                            mTextImdb.setMovementMethod(LinkMovementMethod.getInstance());

                            Spanned result;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                result = Html.fromHtml(Utility.getImdbLink(movieObject.getImdbId()), Html.FROM_HTML_MODE_LEGACY);
                            } else {
                                //noinspection deprecation
                                result = Html.fromHtml(Utility.getImdbLink(movieObject.getImdbId()));
                            }

                            mTextImdb.setText(result);
                            mTextImdb.setLinkTextColor(Color.BLUE);
                        }

                        if (movieObject.getGenres() != null && movieObject.getGenres().size() > 0) {
                            mTextGenres.setVisibility(View.VISIBLE);
                            mTextGenres.setText(Utility.getGenresString(movieObject.getGenres()));
                        }

                        if (movieObject.getProductionCompanies() != null
                                && movieObject.getProductionCompanies().size() > 0) {
                            mTextProduction.setVisibility(View.VISIBLE);
                            mTextProduction.setText(Utility.getProductionString(movieObject.getProductionCompanies()));
                        }

                    }
                }));

        // downloads links to images for a movie, takes one at random
        // and makes it as a CollapsingToolbar Background,
        // then extracts palette and colors Detail Activity
        mSubscriptions.add(
                mApiService.getMovieImages(String.valueOf(mMovieObject.getId()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Backdrops>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Backdrops backdrops) {
                                loadBackdrop(backdrops);
                            }
                        })
        );

        // Adds actors to the RecyclerView
        mSubscriptions.add(
                mApiService.getMovieCredits(String.valueOf(mMovieObject.getId()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<MovieCredits>() {
                            @Override
                            public void onCompleted() {
                                Log.d(TAG, "onCompleted: ");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: ", e);
                            }

                            @Override
                            public void onNext(MovieCredits movieCredits) {
                                mActorsAdapter = new ActorsAdapter(getContext(), movieCredits);
                                mRecyclerActors.setAdapter(mActorsAdapter);
                                mActorsAdapter.setData(DetailFragment.this);
                            }
                        })
        );

        // Adds trailers and inflates YouTube Fragment
        mSubscriptions.add(
                mApiService.getMovieVideos(String.valueOf(mMovieObject.getId()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(movieTrailers -> Observable.from(movieTrailers.getTrailers()))
                        .subscribe(new Observer<Trailer>() {
                            @Override
                            public void onCompleted() {
                                try {
                                    if (mTrailersList.size() > 0) {
                                        // YouTube view initialization

                                        youTubePlayerSupportFragment = new YouTubePlayerSupportFragment();
                                        youTubePlayerSupportFragment.initialize(BuildConfig.YOUTUBE_API_KEY, DetailFragment.this);

                                        FragmentManager fragmentManager = getFragmentManager();
                                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                                        transaction.add(R.id.youtube_fragment, youTubePlayerSupportFragment).commit();
                                    }
                                    if (mYouTubePlayer != null) {
                                        mYouTubePlayer.cueVideos(mTrailersList);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "3 e:" + e);
                                }

                                // If there are trailers - add their links to the share Intent
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("\nAlso check out the Trailers:\n");
                                for (String each : mTrailersList) {
                                    stringBuilder.append("https://www.youtube.com/watch?v=").append(each).append("\n");
                                }
                                mMovie += stringBuilder.toString();
                                if (mShareActionProvider != null) {
                                    mShareActionProvider.setShareIntent(movieIntent());
                                }

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: ", e);
                            }

                            @Override
                            public void onNext(Trailer trailer) {
                                mTrailersList.add(trailer.getKey());
                            }
                        })
        );

        // Adds reviews to the TextView
        mSubscriptions.add(
                mApiService.getMovieReviews(String.valueOf(mMovieObject.getId()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(reviews -> Observable.from(reviews.getReviews()))
                        .subscribe(new Observer<Review>() {
                            @Override
                            public void onCompleted() {
                                if (mReviewsList != null) {
                                    mTextReviews.setText(TextUtils.join("\n", mReviewsList));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Review review) {
                                mReviewsList.add(review.getAuthor() + "\n" + review.getContent());
                            }
                        })
        );
    }

    @Override
    public void onItemClicked(MovieCast movieCast, View view) {
        Intent intent = new Intent(getContext(), ActorActivity.class);
        intent.putExtra(Consts.ACTOR_EXTRA, movieCast);
        startActivity(intent);
    }

    /**
     * Loads backdrop and uses palette from it to paint the screen
     *
     * @param backdrops to randomly pick
     */
    private void loadBackdrop(Backdrops backdrops) {
        if (mImageBackdrop != null) {
            Picasso.with(getContext())
                    .load(Utility.getFullUrl(getContext(), backdrops.getBackdrops()
                            .get(new Random().nextInt(backdrops.getBackdrops().size()))
                            .getFilePath()))
                    .into(mImageBackdrop, new Callback() {
                        @Override
                        public void onSuccess() {
                            // makes detail view colored according to the image palette
                            if (mImageBackdrop != null) {
                                BitmapDrawable drawable = (BitmapDrawable) mImageBackdrop.getDrawable();

                                if (drawable != null) {
                                    Palette palette = Palette.from(drawable.getBitmap()).generate();

                                    int lightVibrantColor = palette.getLightVibrantColor(0);
                                    if (lightVibrantColor == 0) {
                                        lightVibrantColor = palette.getLightMutedColor(0);
                                    }

                                    int vibrantColor = palette.getVibrantColor(0);
                                    if (vibrantColor == 0) {
                                        vibrantColor = palette.getMutedColor(0);
                                    }

                                    int darkVibrantColor = palette.getDarkVibrantColor(0);
                                    if (darkVibrantColor == 0) {
                                        darkVibrantColor = palette.getDarkMutedColor(0);
                                    }

                                    mLinearBackground.setBackgroundColor(lightVibrantColor);
                                    CollapsingToolbarLayout toolbarLayout = ButterKnife.findById(getActivity(), R.id.collapsing_toolbar);
                                    toolbarLayout.setContentScrimColor(vibrantColor);

                                    if (Build.VERSION.SDK_INT >= 21) {
                                        Window window = getActivity().getWindow();
                                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                                        window.setStatusBarColor(darkVibrantColor);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onError() {
                            Log.e(TAG, "onError: no image");
                        }
                    });
        }
    }
}
