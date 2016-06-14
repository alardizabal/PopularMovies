package com.alardizabal.popularmovies.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.alardizabal.popularmovies.R;
import com.alardizabal.popularmovies.fragments.DetailFragment;

public class DetailActivity extends AppCompatActivity {

    private final String LOG_TAG = getClass().getSimpleName();

    static final String BACK_BUTTON_PRESSED = "backButtonPressed";
    static final String PREFERENCE_FILE_KEY = "com.alardizabal.popularmovies.PREFERENCE_FILE_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle bundle = new Bundle();
        Intent intent = this.getIntent();
        if (intent != null) {
            bundle.putString("movieId", intent.getStringExtra("movieId"));
            bundle.putString("originalTitle", intent.getStringExtra("originalTitle"));
            bundle.putString("posterPath", intent.getStringExtra("posterPath"));
            bundle.putString("overview", intent.getStringExtra("overview"));
            bundle.putDouble("voteAverage", intent.getDoubleExtra("voteAverage", 0));
            bundle.putString("releaseDate", intent.getStringExtra("releaseDate"));
            bundle.putParcelableArrayList("trailers",intent.getParcelableArrayListExtra("trailers"));
            bundle.putParcelableArrayList("reviews", intent.getParcelableArrayListExtra("reviews"));
        }

        if (savedInstanceState == null) {
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
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
}
