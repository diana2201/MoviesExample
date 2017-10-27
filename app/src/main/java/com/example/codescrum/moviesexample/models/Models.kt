package com.example.codescrum.moviesexample.models

/**
 * Created by codescrum on 25/10/2017.
 */
data class Movie(val id : Int, val title : String, val overview : String, val vote_average : Float, val poster_path : String, val release_date : String, val genres_id : List<Int>)

data class PopularMoviesResponse(val page: Int, val results: List<Movie>)

data class Genre(val id: Int, val name: String)

data class GenreLisIdResponse(val response: List<Genre>)