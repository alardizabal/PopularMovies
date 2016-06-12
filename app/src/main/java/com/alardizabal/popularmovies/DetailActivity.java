package com.alardizabal.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/*
    TODO
    1) Improve landscape layout
    2) Move strings to strings file
    3) Move constants to constants file
 */

public class DetailActivity extends AppCompatActivity {

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
    static final String PREFERENCE_FILE_KEY = "com.alardizabal.popularmovies.PREFERENCE_FILE_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        trailersListView = (ListView) findViewById(R.id.trailersListview);
        reviewsListView = (ListView) findViewById(R.id.reviewsListview);

        Intent intent = this.getIntent();
        if (intent != null) {
            originalTitle = intent.getStringExtra("originalTitle");
            posterPath = intent.getStringExtra("posterPath");
            overview = intent.getStringExtra("overview");
            voteAverage = intent.getDoubleExtra("voteAverage", 0);
            releaseDate = intent.getStringExtra("releaseDate");
            trailers = intent.getParcelableArrayListExtra("trailers");
            reviews = intent.getParcelableArrayListExtra("reviews");
        }

        TextView textViewOriginalTitle = (TextView) findViewById(R.id.textViewOriginalTitle);
        if (textViewOriginalTitle != null) {
            textViewOriginalTitle.setText(originalTitle);
        }
        TextView textViewOverview = (TextView) findViewById(R.id.textViewOverview);
        if (textViewOverview != null) {
            textViewOverview.setText(overview);
        }
        TextView textViewVoteAverage = (TextView) findViewById(R.id.textViewVoteAverage);
        if (textViewVoteAverage != null) {
            textViewVoteAverage.setText("User Rating: " + voteAverage.toString());
        }
        TextView textViewReleaseDate = (TextView) findViewById(R.id.textViewReleaseDate);
        if (textViewReleaseDate != null) {
            textViewReleaseDate.setText("Release Date: " + releaseDate);
        }
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Glide.with(this).load(posterPath).into(imageView);

        TrailerListAdapter trailerListAdapter = new TrailerListAdapter(this, R.layout.list_item_trailer, trailers);
        trailersListView.setAdapter(trailerListAdapter);

        ReviewListAdapter reviewListAdapter = new ReviewListAdapter(this, R.layout.list_item_review, reviews);
        reviewsListView.setAdapter(reviewListAdapter);

        if (reviews.size() == 0) {
            TextView reviewsTitleLabel = (TextView) findViewById(R.id.reviewsTitleLabel);
            reviewsTitleLabel.setVisibility(View.GONE);
            reviewsListView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                savePreferences();
                super.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        savePreferences();
        super.onBackPressed();
    }

    private void savePreferences() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCE_FILE_KEY, this.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(BACK_BUTTON_PRESSED, true);
        editor.commit();
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
