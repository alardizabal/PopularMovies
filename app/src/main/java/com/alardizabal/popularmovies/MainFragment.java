package com.alardizabal.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class MainFragment extends Fragment {

    private ArrayAdapter<Movie> gridAdapter;
    private GridView gridView;
//    private ImageAdapter imageAdapter;
    public List<Movie> movies;
//    private OnFragmentInteractionListener mListener;
//
    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        if (id == R.id.action_refresh) {
//            updateWeather();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        GridView gridView = (GridView)fin
//        gridAdapter = new ArrayAdapter<Movie>(getActivity(), R.layout.fragment_main, movies);
//        gridAdapter = new ArrayAdapter<Movie>(
//                getActivity(), // The current context (this activity)
//                R.layout.fragment_main, // The name of the layout ID.
//                R.id.gridview, // The ID of the textview to populate.
//                new ArrayList<Movie>());


        movies = new ArrayList<Movie>();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        gridView = (GridView) rootView.findViewById(R.id.gridview);


//        gridview.setAdapter(new ImageAdapter(getActivity(), movies));

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        moviesTask.execute();
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

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
                final String FORECAST_BASE_URL =
                        "http://api.themoviedb.org/3/movie/popular?";
                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
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

                Log.v(LOG_TAG, "Forecast string: " + movieJsonStr);
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

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        protected void onPostExecute(List<Movie> result) {
            if (result != null) {
//                gridAdapter.clear();
//                gridAdapter.addAll(result);
                ImageAdapter imageAdapter = new ImageAdapter((getContext()));
                gridView.setAdapter(imageAdapter);
                imageAdapter.notifyDataSetChanged();
                System.out.println(gridView.getAdapter().getCount());

            }
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context context;

        public ImageAdapter(Context c) {
            context = c;
        }

        public int getCount() {
            System.out.println(movies.size());
            return movies.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            Movie movie = new Movie();
            movie = movies.get(position);
            Glide.with(context).load(movie.getPosterPath()).into(imageView);
            return imageView;
        }
    }
}