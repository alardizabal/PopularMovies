package com.alardizabal.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

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

    static final String BACK_BUTTON_PRESSED = "backButtonPressed";
    static final String PREFERENCE_FILE_KEY = "com.alardizabal.popularmovies.PREFERENCE_FILE_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = this.getIntent();
        if (intent != null) {
            originalTitle = intent.getStringExtra("originalTitle");
            posterPath = intent.getStringExtra("posterPath");
            overview = intent.getStringExtra("overview");
            voteAverage = intent.getDoubleExtra("voteAverage", 0);
            releaseDate = intent.getStringExtra("releaseDate");
        }

        TextView textViewOriginalTitle = ((TextView) findViewById(R.id.textViewOriginalTitle));
        if (textViewOriginalTitle != null) {
            textViewOriginalTitle.setText(originalTitle);
        }
        TextView textViewOverview = ((TextView) findViewById(R.id.textViewOverview));
        if (textViewOverview != null) {
            textViewOverview.setText(overview);
        }
        TextView textViewVoteAverage = ((TextView) findViewById(R.id.textViewVoteAverage));
        if (textViewVoteAverage != null) {
            textViewVoteAverage.setText("User Rating: " + voteAverage.toString());
        }
        TextView textViewReleaseDate = ((TextView) findViewById(R.id.textViewReleaseDate));
        if (textViewReleaseDate != null) {
            textViewReleaseDate.setText("Release Date: " + releaseDate);
        }
        ImageView imageView = ((ImageView) findViewById(R.id.imageView));
        Glide.with(this).load(posterPath).into(imageView);
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
}
