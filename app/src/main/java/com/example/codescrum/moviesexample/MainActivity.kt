package com.example.codescrum.moviesexample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.example.codescrum.moviesexample.adapters.MoviesAdapter
import com.example.codescrum.moviesexample.api.MoviesService
import com.example.codescrum.moviesexample.api.RestApi
import com.example.codescrum.moviesexample.models.Genre
import com.example.codescrum.moviesexample.models.GenreLisIdResponse
import com.example.codescrum.moviesexample.models.PopularMoviesResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val moviesService: MoviesService = RestApi().moviesApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        getGenres()
        getMovies()
    }

//    fun getGenres(){
//        moviesService.getGenres(getString(R.string.api_key_mdb))
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                        {
//                            response ->
//
//                        }
//                )
//    }

    fun getMovies(){

        moviesService.getMovies(getString(R.string.api_key_mdb))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    response -> loadMovies(response)
                }
                )
    }

    fun loadMovies(data: PopularMoviesResponse){
        recycler_movies.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        recycler_movies.adapter = MoviesAdapter(data.results)
    }
}
