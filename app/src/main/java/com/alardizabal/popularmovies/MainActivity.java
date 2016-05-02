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
     */

public class MainActivity extends AppCompatActivity {

    enum SortByType {
        mostPopular,
        topRated
    }

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    static final String STATE_SORT_TYPE = "sortType";
    static final String BACK_BUTTON_PRESSED = "backButtonPressed";
    static final String PREFERENCE_FILE_KEY = "com.alardizabal.popularmovies.PREFERENCE_FILE_KEY";

    private Boolean backButtonPressed;

    private GridView gridView;
    public List<Movie> movies;

    SortByType sortByType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            sortByType = (SortByType) savedInstanceState.get(STATE_SORT_TYPE);
        }
        setContentView(R.layout.activity_main);

        movies = new ArrayList<>();
        gridView = (GridView) findViewById(R.id.gridview);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Movie movie = movies.get(position);
                Intent intent = new Intent(getBaseContext(), DetailActivity.class)
                        .putExtra("originalTitle", movie.getOriginalTitle())
                        .putExtra("posterPath", movie.getPosterPath())
                        .putExtra("overview", movie.getOverview())
                        .putExtra("voteAverage", movie.getVoteAverage())
                        .putExtra("releaseDate", movie.getReleaseDate());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    private void loadPreferences(){
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

        return super.onOptionsItemSelected(item);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private String MOVIE_BASE_URL;

        public FetchMoviesTask (SortByType sortType) {
            if (sortType == SortByType.mostPopular) {
                sortByType = SortByType.mostPopular;
                MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/popular?";
            } else if (sortType == SortByType.topRated) {
                sortByType = SortByType.topRated;
                MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/top_rated?";
            }
        }

        private List<Movie> getMovieDataFromJson(String movieJsonStr)
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

            JSONObject movieJson = new JSONObject(movieJsonStr);
            Log.v(LOG_TAG, "JSON: " + movieJson);

            JSONArray movieArray = movieJson.getJSONArray(MOVIES_LIST);

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

            String movieJsonStr = null;

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
                movieJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Movie JSON String: " + movieJsonStr);
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
                return getMovieDataFromJson(movieJsonStr);
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
                int adjustedWidth = (int)(width/2);
                int adjustedHeight = (int)(adjustedWidth * 1.5027027);
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