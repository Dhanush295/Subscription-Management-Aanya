package com.netflixsub.catalog;

import com.netflixsub.common.Json;

public class Movie {
    public final String id;
    public final String title;
    public final String language;
    public final String genre;
    public final int year;
    public final double rating;
    public final String minPlan;

    public Movie(String id, String title, String language, String genre, int year, double rating, String minPlan) {
        this.id = id;
        this.title = title;
        this.language = language;
        this.genre = genre;
        this.year = year;
        this.rating = rating;
        this.minPlan = minPlan;
    }

    @Override
    public String toString() {
        return "{\"id\":\"" + id + "\","
             + "\"title\":\"" + Json.esc(title) + "\","
             + "\"language\":\"" + language + "\","
             + "\"genre\":\"" + genre + "\","
             + "\"year\":" + year + ","
             + "\"rating\":" + rating + ","
             + "\"minPlan\":\"" + minPlan + "\"}";
    }
}
