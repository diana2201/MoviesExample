package com.example.codescrum.moviesexample.models

/**
 * Created by codescrum on 25/10/2017.
 */
data class Movie(val id : Int, val title : String, val overview : String, val vote_average : Float, val poster_path : String, val release_date : String)

data class PopularMoviesResponse(val page: Int, val results: List<Movie>)