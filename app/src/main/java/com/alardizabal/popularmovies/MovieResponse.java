package com.alardizabal.popularmovies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alardizabal on 4/30/16.
 */
public class MovieResponse {

    List<Movie> movies;

    public MovieResponse() {
        movies = new ArrayList<>();
    }

    public static MovieResponse parseJSON(String response) {
        Gson gson = new GsonBuilder().create();
        MovieResponse movieResponse = gson.fromJson(response, MovieResponse.class);
        return movieResponse;
    }
}
