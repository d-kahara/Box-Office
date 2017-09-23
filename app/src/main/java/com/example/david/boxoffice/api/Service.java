package com.example.david.boxoffice.api;

import com.example.david.boxoffice.model.MoviesResponse;
import com.example.david.boxoffice.model.TrailerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by david on 9/15/17.
 * this interface provides different paths that provide varying data-sets depending on the GET method
 */

public interface Service {
    @GET("movie/popular")
    Call<MoviesResponse> getPopularMovies(@Query("api_key")String apiKey);

    @GET("movie/top_rated")
    Call<MoviesResponse> getTopRatedMovies(@Query("api_key")String apiKey);

    @GET("movie/{movie_id}/videos")
    Call<TrailerResponse> getMovieTrailer(@Path("movie_id") int id, @Query("api_key") String apiKey);

    @GET("search/movie")
    Call<TrailerResponse> getMovie(@Query("query") String query, @Query("api_key") String apiKey);
}
