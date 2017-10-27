package com.example.codescrum.moviesexample

import android.content.res.Resources
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.codescrum.moviesexample.api.MoviesService
import com.example.codescrum.moviesexample.api.RestApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getMovies()
    }

    fun getMovies(){

        val popularMovies: MoviesService = RestApi().popularApi

        popularMovies.getMovies(getString(R.string.api_key_mdb))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    response -> Log.d("Diana", response.results.toString())
                }
                )
    }
}
