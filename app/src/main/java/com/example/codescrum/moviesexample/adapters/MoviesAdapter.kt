package com.example.codescrum.moviesexample.adapters


import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.example.codescrum.moviesexample.R
import com.example.codescrum.moviesexample.extensions.inflate
import com.example.codescrum.moviesexample.extensions.loadImage
import com.example.codescrum.moviesexample.models.Genre
import com.example.codescrum.moviesexample.models.Movie
import kotlinx.android.synthetic.main.adapter_movie.view.*

/**
 * Created by codescrum on 27/10/2017.
 */
class MoviesAdapter(val data: List<Movie>): RecyclerView.Adapter<MoviesAdapter.viewHolder>(){

    class viewHolder (view: View): RecyclerView.ViewHolder(view){
        fun bindData(movie: Movie){
            with(itemView){

                txt_titleAdapter.text = movie.title
                txt_genresAdapter.text = "Comedy"
                txt_rateAdapter.text = movie.vote_average.toString()
                txt_yearAdapter.text = movie.release_date.split("-")[0]
                img_movieAdapter.loadImage("https://image.tmdb.org/t/p/w300/"+movie.poster_path)

            }

        }

        fun findGenre(ids : List<Int>, genres: List<Genre>): String{
            var genresString: String = ""
            for (genre in genres){
                for (id in ids){
                    if (genre.id === id){
                        genresString = genresString + ", ${genre.name}"
                    }
                }
            }
            return genresString
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesAdapter.viewHolder{
        val view = parent.inflate(R.layout.adapter_movie, false)
        return viewHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: MoviesAdapter.viewHolder, position: Int) {
        val movie = data[position]
        holder.bindData(movie)
    }



}