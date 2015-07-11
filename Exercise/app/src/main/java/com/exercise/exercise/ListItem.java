package com.exercise.exercise;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class ListItem extends LinearLayout {
    private LinearLayout mExpandable;

    //Venues information
    private long mPhoneNum;
    private String mID;
    private double mLongitude;
    private double mLatitude;



    private Button btnCall;
    private Button btnfind;
    private TextView txtName;
    private TextView txtAddress;
    private TextView txtPhone;
    private TextView txtDistance;
    private RatingBar ratingBar;


    /**
     * ListItems represents one venue returned by foursquare
     * @param context
     */
    public ListItem(Context context) {
        super(context);
        init(context);
    }

    private void init(final Context context) {
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);
        inflate(context, R.layout.list_items, this);

        mExpandable = (LinearLayout)findViewById(R.id.lay_Expandable);
        btnCall = (Button)findViewById(R.id.btn_Call);
        btnfind = (Button)findViewById(R.id.btn_Find);
        txtName = (TextView)findViewById(R.id.txt_Name);
        txtAddress = (TextView)findViewById(R.id.txt_Address);
        txtPhone = (TextView)findViewById(R.id.txt_Phone);
        txtDistance = (TextView)findViewById(R.id.txt_Distance);
        ratingBar = (RatingBar)findViewById(R.id.ratingBar);

        setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(mExpandable.getVisibility() == GONE)
                    mExpandable.setVisibility(VISIBLE);
                else
                    mExpandable.setVisibility(GONE);
            }
        });

        btnCall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String uri = "tel:" + mPhoneNum;
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(uri));
                    context.startActivity(dialIntent);
                } catch (Exception e) {
                    Toast.makeText(context, "Your call has failed...",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        btnfind.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //send location to google maps as a query so it places a marker
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f", mLatitude, mLongitude,mLatitude, mLongitude);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "You have problem with you maps apps",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    /**
    * @param pNum phone number venue as long
    * @param forPNum formatted phone number from foursquare for text view
    * @param id venue id
    * @param name venue name
    * @param add venue address
    * @param rating venue rating
    * @param lng Longitude
    * @param lat Latitude
    * @param dis distance
    */
    public void setInfo(String id, String name, long pNum, String forPNum, String add, double lng, double lat, double dis, float rating){
        mID = id;
        mLongitude = lng;
        mLatitude = lat;

        txtName.setText(name);

        mPhoneNum = pNum;
        if(pNum == 0)
            btnCall.setVisibility(GONE);
        else
            btnCall.setVisibility(VISIBLE);

        txtPhone.setText("Phone :" +forPNum);

        txtAddress.setText("Address: "+ add);

        txtDistance.setText("Distance :"+dis+"m");

        ratingBar.setRating(rating*0.5f);


    }
}
