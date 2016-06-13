package com.alardizabal.popularmovies.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alardizabal.popularmovies.R;
import com.alardizabal.popularmovies.models.Review;
import com.alardizabal.popularmovies.models.Trailer;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DetailFragment extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();

    private String selectedMovieId = "";
    private String originalTitle = "";
    private String posterPath = "";
    private String overview = "";
    private Double voteAverage;
    private String releaseDate = "";

    private ArrayList<Trailer> trailers;
    private ArrayList<Review> reviews;

    private ListView trailersListView;
    private ListView reviewsListView;

    static final String BACK_BUTTON_PRESSED = "backButtonPressed";
    static final String FAVORITES_LIST = "favoritesList";
    static final String PREFERENCE_FILE_KEY = "com.alardizabal.popularmovies.PREFERENCE_FILE_KEY";

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        selectedMovieId = bundle.getString("movieId");
        originalTitle = bundle.getString("originalTitle");
        posterPath = bundle.getString("posterPath");
        overview = bundle.getString("overview");
        voteAverage = bundle.getDouble("voteAverage", 0);
        releaseDate = bundle.getString("releaseDate");
        trailers = bundle.getParcelableArrayList("trailers");
        reviews = bundle.getParcelableArrayList("reviews");

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        trailersListView = (ListView) rootView.findViewById(R.id.trailersListview);
        reviewsListView = (ListView) rootView.findViewById(R.id.reviewsListview);

        TextView textViewOriginalTitle = (TextView) rootView.findViewById(R.id.textViewOriginalTitle);
        if (textViewOriginalTitle != null) {
            textViewOriginalTitle.setText(originalTitle);
        }
        TextView textViewOverview = (TextView) rootView.findViewById(R.id.textViewOverview);
        if (textViewOverview != null) {
            textViewOverview.setText(overview);
        }
        TextView textViewVoteAverage = (TextView) rootView.findViewById(R.id.textViewVoteAverage);
        if (textViewVoteAverage != null) {
            textViewVoteAverage.setText("User Rating: " + voteAverage.toString());
        }
        TextView textViewReleaseDate = (TextView) rootView.findViewById(R.id.textViewReleaseDate);
        if (textViewReleaseDate != null) {
            textViewReleaseDate.setText("Release Date: " + releaseDate);
        }

        final SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFERENCE_FILE_KEY, getContext().MODE_PRIVATE);
        final Set<String> savedFavoritesList = sharedPreferences.getStringSet(FAVORITES_LIST, new HashSet<String>());

        final Button button = (Button) rootView.findViewById(R.id.addFavoriteButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();

                String buttonText = button.getText().toString();
                if (buttonText == "Add to Favorites") {
                    savedFavoritesList.add(selectedMovieId);

                    editor.putStringSet(FAVORITES_LIST, savedFavoritesList);
                    editor.commit();

                    button.setText("Remove from Favorites");

                    Toast toast = Toast.makeText(getContext(), "Added to favorites!", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    savedFavoritesList.remove(selectedMovieId);

                    editor.putStringSet(FAVORITES_LIST, savedFavoritesList);
                    editor.commit();

                    button.setText("Add to Favorites");

                    Toast toast = Toast.makeText(getContext(), "Removed from favorites!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        if (savedFavoritesList.contains(selectedMovieId) == true) {
            button.setText("Remove from Favorites");
        } else {
            button.setText("Add to Favorites");
        }

        ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView);
        Glide.with(this).load(posterPath).into(imageView);

        TrailerListAdapter trailerListAdapter = new TrailerListAdapter(getContext(), R.layout.list_item_trailer, trailers);
        trailersListView.setAdapter(trailerListAdapter);

        trailersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trailer trailer = trailers.get(position);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + trailer.getKey())));
            }
        });

        ReviewListAdapter reviewListAdapter = new ReviewListAdapter(getContext(), R.layout.list_item_review, reviews);
        reviewsListView.setAdapter(reviewListAdapter);

        if (reviews.size() == 0) {
            TextView reviewsTitleLabel = (TextView) rootView.findViewById(R.id.reviewsTitleLabel);
            reviewsTitleLabel.setVisibility(View.GONE);
            reviewsListView.setVisibility(View.GONE);
        }

        return rootView;
    }

    public class TrailerListAdapter extends ArrayAdapter<Trailer> {

        public TrailerListAdapter(Context context, int resource, List<Trailer> trailers) {
            super(context, resource, trailers);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.list_item_trailer, parent, false);
            }

            Trailer trailer = getItem(position);

            if (trailer != null) {
                TextView trailerNameTextView = (TextView) v.findViewById(R.id.trailerNameTextView);

                if (trailerNameTextView != null) {
                    trailerNameTextView.setText(trailer.getName());
                }
            }

            return v;
        }
    }

    public class ReviewListAdapter extends ArrayAdapter<Review> {

        public ReviewListAdapter(Context context, int resource, List<Review> reviews) {
            super(context, resource, reviews);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.list_item_review, parent, false);
            }

            Review review = getItem(position);

            if (review != null) {
                TextView reviewAuthorTextView = (TextView) v.findViewById(R.id.reviewAuthorTextView);
                TextView reviewContentTextView = (TextView) v.findViewById(R.id.reviewContentTextView);

                if (reviewAuthorTextView != null) {
                    reviewAuthorTextView.setText(review.getAuthor());
                }

                if (reviewContentTextView != null) {
                    reviewContentTextView.setText(review.getContent());
                }
            }

            return v;
        }
    }
}
