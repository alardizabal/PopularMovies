package com.alardizabal.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/*
    TODO
    1) Move classes to standalone .java files if appropriate
    2) Move content out of activity and into fragment
    3) Use gson
    4) Move strings to strings file
    5) Move constants to constants file
    6) Make models conforms to Parcelable
    7) Use butterknife
    8) Use Retrofit
    9) Glide error handling
     */

public class MainActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.alardizabal.popularmovies/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    enum SortByType {
        mostPopular,
        topRated,
        favorites
    }

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    static final String STATE_SORT_TYPE = "sortType";
    static final String BACK_BUTTON_PRESSED = "backButtonPressed";
    static final String PREFERENCE_FILE_KEY = "com.alardizabal.popularmovies.PREFERENCE_FILE_KEY";

    private Boolean backButtonPressed;

    private GridView gridView;
    public List<Movie> movies;
    public ArrayList<Trailer> trailers;
    public ArrayList<Review> reviews;

    private Movie selectedMovie;
    private String selectedMovieId;

    SortByType sortByType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            sortByType = (SortByType) savedInstanceState.get(STATE_SORT_TYPE);
        }
        setContentView(R.layout.activity_main);

        movies = new ArrayList<>();
        trailers = new ArrayList<>();
        reviews = new ArrayList<>();

        gridView = (GridView) findViewById(R.id.gridview);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                selectedMovie = movies.get(position);
                selectedMovieId = selectedMovie.getId().toString();

                FetchTrailersTask trailersTask = new FetchTrailersTask();
                trailersTask.execute();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        FetchMoviesTask moviesTask;

        loadPreferences();

        if (!backButtonPressed) {
            if (sortByType == null) {
                moviesTask = new FetchMoviesTask(SortByType.mostPopular);
                moviesTask.execute();
            } else {
                moviesTask = new FetchMoviesTask(sortByType);
                moviesTask.execute();
            }
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.alardizabal.popularmovies/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCE_FILE_KEY, this.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(BACK_BUTTON_PRESSED);
        editor.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_SORT_TYPE, sortByType);
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCE_FILE_KEY, this.MODE_PRIVATE);
        backButtonPressed = sharedPreferences.getBoolean(BACK_BUTTON_PRESSED, false);
        if (backButtonPressed) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(BACK_BUTTON_PRESSED, false);
            editor.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_mostPopular) {
            sortByType = SortByType.mostPopular;
            FetchMoviesTask moviesTask = new FetchMoviesTask(sortByType);
            moviesTask.execute();
            return true;
        }
        if (id == R.id.action_topRated) {
            sortByType = SortByType.topRated;
            FetchMoviesTask moviesTask = new FetchMoviesTask(sortByType);
            moviesTask.execute();
            return true;
        }
        if (id == R.id.action_favorites) {
            sortByType = SortByType.favorites;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private String MOVIE_BASE_URL;

        public FetchMoviesTask(SortByType sortType) {
            if (sortType == SortByType.mostPopular) {
                sortByType = SortByType.mostPopular;
                MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/popular?";
            } else if (sortType == SortByType.topRated) {
                sortByType = SortByType.topRated;
                MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/top_rated?";
            }
        }

        private List<Movie> getMoviesDataFromJson(String moviesJsonStr)
                throws JSONException {

            final String MOVIES_LIST = "results";
            final String MOVIES_POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185/";

            final String MOVIE_ID = "id";
            final String MOVIE_ORIGINAL_TITLE = "original_title";
            final String MOVIE_POSTER_PATH = "poster_path";
            final String MOVIE_OVERVIEW = "overview";
            final String MOVIE_VOTE_AVERAGE = "vote_average";
            final String MOVIE_RELEASE_DATE = "release_date";

            movies.clear();

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            Log.v(LOG_TAG, "JSON: " + moviesJson);

            JSONArray movieArray = moviesJson.getJSONArray(MOVIES_LIST);

            for (int i = 0; i < movieArray.length(); i++) {

                JSONObject movieObject = movieArray.getJSONObject(i);

                Movie movie = new Movie();

                Long movieId = movieObject.getLong(MOVIE_ID);
                String originalTitle = movieObject.getString(MOVIE_ORIGINAL_TITLE);
                String posterPath = movieObject.getString(MOVIE_POSTER_PATH);
                String overview = movieObject.getString(MOVIE_OVERVIEW);
                Double voteAverage = movieObject.getDouble(MOVIE_VOTE_AVERAGE);
                String releaseDate = movieObject.getString(MOVIE_RELEASE_DATE);

                movie.setId(movieId);
                movie.setOriginalTitle(originalTitle);
                movie.setPosterPath(MOVIES_POSTER_BASE_URL + posterPath);
                movie.setOverview(overview);
                movie.setVoteAverage(voteAverage);
                movie.setReleaseDate(releaseDate);

                movies.add(movie);
            }
            return movies;
        }

        @Override
        protected List<Movie> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesJsonStr = null;

            try {
                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                moviesJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Movie JSON String: " + moviesJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(List<Movie> result) {
            if (result != null) {
                ImageAdapter imageAdapter = new ImageAdapter(getBaseContext());
                gridView.setAdapter(imageAdapter);
            }
        }
    }

    public class FetchTrailersTask extends AsyncTask<String, Void, List<Trailer>> {

        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

        private List<Trailer> getTrailersFromJson(String trailersJsonStr)
                throws  JSONException {

            final String TRAILERS_LIST = "results";

            final String TRAILER_ID = "id";
            final String TRAILER_KEY = "key";
            final String TRAILER_NAME = "name";

            trailers.clear();

            JSONObject trailersJson = new JSONObject(trailersJsonStr);
            Log.v(LOG_TAG, "JSON: " + trailersJson);

            JSONArray trailersArray = trailersJson.getJSONArray(TRAILERS_LIST);

            for (int i = 0; i < trailersArray.length(); i++) {

                JSONObject trailerObject = trailersArray.getJSONObject(i);

                Trailer trailer = new Trailer();

                String trailerId = trailerObject.getString(TRAILER_ID);
                String trailerKey = trailerObject.getString(TRAILER_KEY);
                String trailerName = trailerObject.getString(TRAILER_NAME);

                trailer.setId(trailerId);
                trailer.setKey(trailerKey);
                trailer.setName(trailerName);

                trailers.add(trailer);
            }
            return trailers;
        }

        @Override
        protected List<Trailer> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String trailersJsonStr = null;

            try {
                final String APPID_PARAM = "api_key";

                String TRAILERS_BASE_URL = "http://api.themoviedb.org/3/movie/" + selectedMovieId +"/videos?";

                Uri builtUri = Uri.parse(TRAILERS_BASE_URL).buildUpon()
                        .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                trailersJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Trailer JSON String: " + trailersJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getTrailersFromJson(trailersJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Trailer> result) {
            if (result != null) {
                trailers = new ArrayList<>(result);
                FetchReviewsTask reviewsTask = new FetchReviewsTask();
                reviewsTask.execute();
            }
        }
    }

    public class FetchReviewsTask extends AsyncTask<String, Void, List<Review>> {

        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        private List<Review> getReviewsFromJson(String reviewsJsonStr)
                throws  JSONException {

            final String REVIEWS_LIST = "results";

            final String REVIEW_ID = "id";
            final String REVIEW_AUTHOR = "author";
            final String REVIEW_CONTENT = "content";

            reviews.clear();

            JSONObject reviewJson = new JSONObject(reviewsJsonStr);
            Log.v(LOG_TAG, "JSON: " + reviewJson);

            JSONArray reviewArray = reviewJson.getJSONArray(REVIEWS_LIST);

            for (int i = 0; i < reviewArray.length(); i++) {

                JSONObject reviewObject = reviewArray.getJSONObject(i);
                Review review = new Review();

                String reviewId = reviewObject.getString(REVIEW_ID);
                String reviewAuthor = reviewObject.getString(REVIEW_AUTHOR);
                String reviewContent = reviewObject.getString(REVIEW_CONTENT);

                review.setId(reviewId);
                review.setAuthor(reviewAuthor);
                review.setContent(reviewContent);

                reviews.add(review);
            }
            return reviews;
        }

        @Override
        protected List<Review> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String reviewsJsonStr = null;

            try {
                final String APPID_PARAM = "api_key";

                String REVIEW_BASE_URL = "http://api.themoviedb.org/3/movie/" + selectedMovieId +"/reviews?";

                Uri builtUri = Uri.parse(REVIEW_BASE_URL).buildUpon()
                        .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                reviewsJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Review JSON String: " + reviewsJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getReviewsFromJson(reviewsJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(List<Review> result) {
            if (result != null) {
                reviews = new ArrayList<>(result);
                Intent intent = new Intent(getBaseContext(), DetailActivity.class)
                        .putExtra("originalTitle", selectedMovie.getOriginalTitle())
                        .putExtra("posterPath", selectedMovie.getPosterPath())
                        .putExtra("overview", selectedMovie.getOverview())
                        .putExtra("voteAverage", selectedMovie.getVoteAverage())
                        .putExtra("releaseDate", selectedMovie.getReleaseDate())
                        .putParcelableArrayListExtra("trailers", trailers)
                        .putParcelableArrayListExtra("reviews", reviews);
                startActivity(intent);
            }
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context context;

        public ImageAdapter(Context c) {
            context = c;
        }

        public int getCount() {
            return movies.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);

                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                int width = metrics.widthPixels;
                int adjustedWidth = (int) (width / 2);
                int adjustedHeight = (int) (adjustedWidth * 1.5027027);
                imageView.setLayoutParams(new GridView.LayoutParams(adjustedWidth, adjustedHeight));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }

            Movie movie = movies.get(position);
            Glide.with(context).load(movie.getPosterPath()).into(imageView);
            return imageView;
        }
    }
}