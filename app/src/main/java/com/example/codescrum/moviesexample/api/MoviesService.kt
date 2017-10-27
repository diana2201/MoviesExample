package com.example.codescrum.moviesexample.api

import com.example.codescrum.moviesexample.models.Movie
import com.example.codescrum.moviesexample.models.PopularMoviesResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by codescrum on 25/10/2017.
 */
interface MoviesService {
    @GET("popular?")
    fun getMovies(@Query("api_key") api_key: String): Observable<PopularMoviesResponse>
}