package com.netflixsub.model;

import com.netflixsub.catalog.Movie;
import java.sql.*;
import java.util.*;

public class MovieRepository {
    public List<Movie> all() {
        List<Movie> out = new ArrayList<>();
        try (Statement s = Db.get().createStatement();
             ResultSet rs = s.executeQuery("SELECT id,title,language,genre,year,rating,min_plan FROM movies ORDER BY id")) {
            while (rs.next()) {
                out.add(new Movie(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getInt(5), rs.getDouble(6), rs.getString(7)));
            }
            return out;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
