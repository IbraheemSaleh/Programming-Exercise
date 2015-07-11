package com.exercise.exercise;

import android.app.ActionBar;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {
    // the foursquare client_id and the client_secret
    final String CLIENT_ID = "ACAO2JPKM1MXHQJCK45IIFKRFR2ZVL0QASMCBCG5NPJQWF2G";
    final String CLIENT_SECRET = "YZCKUYJ1WHUV2QICBXUBEILZI1DMPUIDP5SHV043O04FKBHL";
    //No one is going to travel more than 3kms for coffee...Right?
    final String SEARCH_DISTANCE = "3000";

    private GPSTracker mGPSTracker;
    private ProgressBar mSprLoading;

    private static ArrayList<ListItem> mListItems;
    private LinearLayout layList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layList = (LinearLayout) findViewById(R.id.lay_List);
        mSprLoading = (ProgressBar) findViewById((R.id.spr_Loading));
        mGPSTracker = new GPSTracker(this);
        mListItems = new ArrayList<ListItem>();
    }

    @Override
    public void onStop(){
        super.onStop();
        mGPSTracker.stopUpdates();
    }

    @Override
    public void onStart(){
        super.onStart();
        mGPSTracker.requestLocationUpdates();
    }

    public void locationChanged(String latlng){

        //The url for the foursquare api
        String searchString = "https://api.foursquare.com/v2/venues/explore?client_id=" + CLIENT_ID +"&client_secret=" + CLIENT_SECRET +
                "&v=20130815&ll="+latlng+"&query=coffee&radius="+SEARCH_DISTANCE+"&sortByDistance=1";
        //execute asyncTask
        new foursquare().execute(searchString);
    }

    public void updateListView(){
        //Remove the old views and ad the new ones
        layList.removeAllViews();
        for(int i = 0; i < mListItems.size(); i++) {
            layList.addView(mListItems.get(i));
        }
    }

    private class foursquare extends AsyncTask<String, Integer, Long>  {
        //the Strint will be returned by foursquare api
        String temp;

        @Override
        protected Long doInBackground(String... strings) {
            // make Call to the url
            temp = searchFoursquare(strings[0]);
            return (long)temp.length();
        }

        @Override
        protected void onPreExecute() {
            mSprLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Long result) {
            mSprLoading.setVisibility(View.GONE);
            if (temp == null) {
                // we have an error to the call
                // we can also stop the progress bar
            } else {
                // all things went right
                // parseFoursquare venues search result
                parseFoursquare(temp);
            }
        }
    }


    public static String searchFoursquare(String url) {
        // string buffers the url
        StringBuffer buffer_string = new StringBuffer(url);
        String replyString = "";

        // instanciate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        // instanciate an HttpGet
        HttpGet httpget = new HttpGet(buffer_string.toString());

        try {
            // get the responce of the httpclient execution of the url
            HttpResponse response = httpclient.execute(httpget);
            InputStream is = response.getEntity().getContent();

            // buffer input stream the result
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(20);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            // the result as a string is ready for parsing
            replyString = new String(baf.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // trim the whitespaces
        return replyString.trim();
    }

    private void parseFoursquare(final String response) {
        mListItems.clear();

        String id; //venue id
        String name;  //venue name
        long pNum; //phone number venue as long
        String forPNum; //formatted phone number from foursquare for text view
        String add; // venue address
        double rating; //venue rating
        double lng; // Longitude
        double lat; // Latitude
        double dis; //distance

        try {
            // make an jsonObject in order to parse the response
            JSONObject jsonObject = new JSONObject(response);
            // make an jsonObject in order to parse the response
            if (jsonObject.has("response")) {
                int len = jsonObject.getJSONObject("response").getInt("totalResults");
                if (jsonObject.getJSONObject("response").has("groups")) {
                    JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("groups").getJSONObject(0).getJSONArray("items");
                    JSONObject loopObject;
                    for (int i = 0; i < len; i++) {
                        loopObject = jsonArray.getJSONObject(i).getJSONObject("venue");
                        id = loopObject.getString("id");
                        name = loopObject.getString("name");
                        if(loopObject.getJSONObject("contact").has("phone")) {
                            pNum = loopObject.getJSONObject("contact").getLong("phone");
                            forPNum = loopObject.getJSONObject("contact").getString("formattedPhone");
                        }else {
                            pNum = 0;
                            forPNum = "Not Given";
                        }

                        if(loopObject.getJSONObject("location").has("address"))
                            add = loopObject.getJSONObject("location").getString("address");
                        else
                            add = "Not Given";

                        lng = loopObject.getJSONObject("location").getDouble("lng");
                        lat = loopObject.getJSONObject("location").getDouble("lat");
                        dis = loopObject.getJSONObject("location").getDouble("distance");

                        if(loopObject.has("rating"))
                            rating = loopObject.getDouble("rating");
                        else
                            rating = 0;

                        ListItem tempListItem = new ListItem(this);
                        tempListItem.setInfo(id, name, pNum, forPNum, add, lng, lat, dis, (float)rating);
                        mListItems.add(tempListItem);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //once everything is parsed update the view with all our items
        updateListView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
