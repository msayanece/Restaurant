package com.example.sayan.restaurant;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantList extends Fragment {

    //global var
    ListView listView;
    String[] titles, descriptions;

    //image array(R.drawable.xxx returns int id)
    int[] images = {
            R.drawable.afterrainbow,R.drawable.clearnight,R.drawable.cloudgoesaway,
            R.drawable.cloudy,R.drawable.foggy,R.drawable.heavyshower,R.drawable.hotday,
            R.drawable.night,R.drawable.nightrain,R.drawable.partlycloudy,
            R.drawable.rainwiththunder,R.drawable.windy,R.drawable.sunny,R.drawable.superhot,
            R.drawable.rainy
    };

    public RestaurantList() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_restaurant_list, container, false);

            //set String array from resources
            Resources resources = getResources();
            titles = resources.getStringArray(R.array.titles);
            descriptions = resources.getStringArray(R.array.descriptions);
            listView = (ListView) v.findViewById(R.id.listView_id);

        //set listview rows using custom adapter
            CustomAdapter adapter = new CustomAdapter(getContext(),titles,descriptions,images);
            listView.setAdapter(adapter);
        return v;
        }

        //this holds the view references
        private class MyViewHolder{

            ImageView image;
            TextView title;
            TextView description;

            //constructor
            MyViewHolder(View view){

                //get all the views' references. ID
                title = (TextView) view.findViewById(R.id.title_id);
                description = (TextView) view.findViewById(R.id.description_id);
                image = (ImageView) view.findViewById(R.id.imageView);
            }
        }

        //create custom adapter class
        private class CustomAdapter extends ArrayAdapter {

            //global variables
            String[] titles, descriptions;
            int[] images;
            Context context;

            //constructor
            CustomAdapter(Context context, String[] titles, String[] descriptions, int[] images){

                //call super constructor(context, parent activity of destination, destination of data, source of data)
                super(context,R.layout.single_row_activity,R.id.title_id,titles);

                //initialize global var with parameters
                this.context = context;
                this.titles = titles;
                this.descriptions = descriptions;
                this.images = images;
            }


            //override getView(row number[0 to (max-1)], oldview , parent [listview])
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                MyViewHolder holder = null;
                View row = convertView;

                //for PERFORMANCE OPTIMISATION
                //for 1st time convertView is null, but for next time the old view has some reference
                //So only for 1st time we will inflate the single_row_activity
                if (row ==null) {
                    //get layout inflater object. getSystemService() returns it
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                            context.LAYOUT_INFLATER_SERVICE);

                    //inflate single row activity
                    //inflate(the activity to be inflated, the attach destination activity, if want to attach or not)
                    row = inflater.inflate(R.layout.single_row_activity, parent, false);

                    //initialize holder object
                    holder = new MyViewHolder(row);

                    //store the holder object into row view object
                    row.setTag(holder);
                }

                //for recycling, we are using the previously initialized image and text views ref.
                else{

                    //retrieve the holder object from row view object
                    holder = (MyViewHolder) row.getTag();
                }


                //fetching one data at a time and setting to views from holder object
                holder.title.setText(titles[position]);
                holder.description.setText(descriptions[position]);
                holder.image.setImageResource(images[position]);

                //return the created view for one row
                return row;
            }
    }

}
