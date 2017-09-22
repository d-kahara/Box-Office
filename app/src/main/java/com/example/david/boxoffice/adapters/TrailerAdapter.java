package com.example.david.boxoffice.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.david.boxoffice.R;
import com.example.david.boxoffice.model.Trailer;

import java.util.List;

/**
 * Created by david on 9/22/17.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.MyViewHolder> {

    private Context mContext;
    private List<Trailer> trailerList;


    //constructor for trailer Adapter
    public TrailerAdapter(Context mContext, List<Trailer> trailerList){
        this.mContext = mContext;
        this.trailerList = trailerList;

    }

    //Create Views
    @Override
    public TrailerAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i){
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.trailer_card, viewGroup, false);
        return new MyViewHolder(view);

    }


    //Bind the view to the resource
    @Override
    public void onBindViewHolder(final TrailerAdapter.MyViewHolder viewHolder, int i){
        viewHolder.title.setText(trailerList.get(i).getName());

    }

    @Override
    public int getItemCount(){

        return trailerList.size();

    }

    //Inner class for Recycler View
    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public ImageView thumbnail;

        public MyViewHolder(View view){
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);

            view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    //Get specific video in the recycler View
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        Trailer clickedDataItem = trailerList.get(pos);

                        //Get the movie Key from the Trailer model
                        String videoId = trailerList.get(pos).getKey();

                        //Open You tube to view the movie
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v="+videoId));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("VIDEO_ID", videoId);
                        mContext.startActivity(intent);

                        Toast.makeText(v.getContext(), "You clicked " + clickedDataItem.getName(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}
