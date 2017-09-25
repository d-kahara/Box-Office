package com.example.david.boxoffice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.david.boxoffice.adapters.MoviesAdapter;
import com.example.david.boxoffice.api.Client;
import com.example.david.boxoffice.api.Service;
import com.example.david.boxoffice.model.Movie;
import com.example.david.boxoffice.model.MoviesResponse;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = MainActivity.class.getSimpleName() ;
    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private List<Movie> movieList;
    ProgressDialog pd;
    private SwipeRefreshLayout swipeContainer;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private String mRecentMovies;

    private static final int SIGN_IN_REQUEST_CODE = 10;
    //private FirebaseListAdapter<Movie> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
        } else {
            // User is already signed in. Therefore, display
            // a welcome Toast
            Toast.makeText(this,
                    "Welcome " + FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getDisplayName(),
                    Toast.LENGTH_LONG)
                    .show();
            initViews();


        }

        //This allows for swiping down to refresh the movies list
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.main_content);
        swipeContainer.setColorSchemeResources(android.R.color.holo_orange_dark);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mRecentMovies != null) {
                    loadJSON(mRecentMovies);
                } else {
                    Toast.makeText(MainActivity.this, "Please Enter the Name of a movie in the search Bar", Toast.LENGTH_LONG).show();
                }

            }
        });


   }

    @Override
    public void onResume(){
        super.onResume();
        //Creating shared Preferences for Recently searched movies
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mRecentMovies = mSharedPreferences.getString(Constants.PREFERENCES_MOVIE_KEY, null);
        initViews();
        if (mRecentMovies != null) {
            loadJSON(mRecentMovies);
        } else {
            Toast.makeText(MainActivity.this, "Please Enter the Name of a movie in the search Bar", Toast.LENGTH_LONG).show();
            if (swipeContainer.isRefreshing()) {
                swipeContainer.setRefreshing(false);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(this,
                        "Successfully signed in. Welcome!",Toast.LENGTH_LONG).show();

                initViews();
                //Creating shared Preferences for Recently searched movies
                mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                mRecentMovies = mSharedPreferences.getString(Constants.PREFERENCES_MOVIE_KEY, null);

                if (mRecentMovies != null) {
                    loadJSON(mRecentMovies);
                } else {
                    Toast.makeText(MainActivity.this, "Please Enter the Name of a movie in the search Bar", Toast.LENGTH_LONG).show();
                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                }

            } else {
                Toast.makeText(this,
                        "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();

                // Close the app
                finish();
            }
        }

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

    public void initViews() {
        //Setting a progress Dialog
        pd = new ProgressDialog(this);
        pd.setMessage("Fetching....");
        pd.setCancelable(false);



        //Binds the recycler view to the resource
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



    }


    //This method does the heavy lifting.Making the network calls to the service and processing the response
    //It loads the movie entry made
    private void loadJSON(String movie) {
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
            Call<MoviesResponse> call = apiService.getMovie(movie, BuildConfig.MOVIE_DB_API_KEY);
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
                    Toast.makeText(MainActivity.this, "Error fetching Data", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }




    //Creates the overflow menu to change preferences
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        getMenuInflater().inflate(R.menu.menu_search, menu);


        MaterialSearchView searchView = (MaterialSearchView) findViewById(R.id.search_view);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();

        MenuItem item = menu.findItem(R.id.action_search);


        searchView.setMenuItem(item);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                addToSharedPreferences(query);
                loadJSON(query);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_popular:
                Intent intent = new Intent(MainActivity.this, PopularActivity.class);
                startActivity(intent);
                return true;


            case  R.id.menu_sign_out:
                    AuthUI.getInstance().signOut(this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(MainActivity.this,
                                            "You have been signed out.",
                                            Toast.LENGTH_LONG)
                                            .show();

                                    // Close activity
                                    finish();
                                }
                            });


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    private void addToSharedPreferences(String movie) {
        mEditor.putString(Constants.PREFERENCES_MOVIE_KEY, movie).apply();
    }


}