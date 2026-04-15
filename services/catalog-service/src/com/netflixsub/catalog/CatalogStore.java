package com.netflixsub.catalog;

import com.netflixsub.model.MovieRepository;
import java.util.ArrayList;
import java.util.List;

public class CatalogStore {
    private final List<Movie> movies = new ArrayList<>();

    public CatalogStore() {
        movies.addAll(new MovieRepository().all());
    }

    public List<Movie> all() { return movies; }

    public List<Movie> filter(String language, String genre) {
        List<Movie> out = new ArrayList<>();
        for (Movie m : movies) {
            if (language != null && !language.equalsIgnoreCase(m.language)) continue;
            if (genre != null && !genre.equalsIgnoreCase(m.genre)) continue;
            out.add(m);
        }
        return out;
    }

    public Movie findById(String id) {
        for (Movie m : movies) if (m.id.equals(id)) return m;
        return null;
    }
}
