package com.example.david.boxoffice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.david.boxoffice.adapters.MoviesAdapter;
import com.example.david.boxoffice.api.Client;
import com.example.david.boxoffice.api.Service;
import com.example.david.boxoffice.model.Movie;
import com.example.david.boxoffice.model.MoviesResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by david on 9/23/17.
 */

public class PopularActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private List<Movie> movieList;
    ProgressDialog pd;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //This method initializes the views i.e movie cards
        initViews();


        //This allows for swiping down to refresh the movies list
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.main_content);
            swipeContainer.setColorSchemeResources(android.R.color.holo_orange_dark);
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initViews();
                Toast.makeText(PopularActivity.this, "Movies Refreshed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //This method returns the activity upon which it is called with regards to the context
    public Activity getActivity() {
        Context context = this;
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }


    //This method initializes the views and calls loadJson()
    public void initViews() {
        //Setting a progress Dialog
        pd = new ProgressDialog(this);
        pd.setMessage("Fetching Movies....");
        pd.setCancelable(false);
        pd.show();


        //Binds the recycler view to the view
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        //Initializing the movies array list and the adapter
        movieList = new ArrayList<>();
        adapter = new MoviesAdapter(this, movieList);

        //Setting up the gridlayout with regards to the device's orientation
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        }

        //Setup animations for the recycler view
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        loadJSON();
    }

    //This method does the heavy lifting.Making the network calls to the service and processing the response
    //It loads popular movies
    private void loadJSON() {
        try {
            if (BuildConfig.MOVIE_DB_API_KEY.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please get API key", Toast.LENGTH_SHORT);
                pd.dismiss();
            }

            //instantiate the client
            Client Client = new Client();
            //Implement the Service
            Service apiService = Client.getClient().create(Service.class);

            //Make a network call
            Call<MoviesResponse> call = apiService.getPopularMovies(BuildConfig.MOVIE_DB_API_KEY);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = response.body().getResults();

                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                    pd.dismiss();
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(PopularActivity.this, "Error fetching Data", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

}
