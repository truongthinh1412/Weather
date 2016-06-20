package puf.example.td1;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    TextView txtRequestUrl, txtResponseJson, WindDeg, WindSpeed, CoordLon, CoordLat,Pressure,Humidity,TempMin, TempMax;
    ActionBar actionBar;
    SearchView txtSearchValue;
    TextView txtMsg;
    SharedPreferences sharedPreferences;
    static SharedPreferences favoritesCities;
    static City[] favCities = new City[AddLocationActivity.maxFavorites];
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        for(int i = 0; i < AddLocationActivity.maxFavorites; i++){
            favCities[i] = new City();
        }

        actionBar = getActionBar();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Your location is", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                GPSTracker gps = new GPSTracker(MainActivity.this);
                if(gps.canGetLocation()) {
                    Toast.makeText(getApplicationContext(), "Your location is" + gps.getLatitude() + " / " + gps.getLongitude(), Toast.LENGTH_SHORT).show();
                 }else{
                    Toast.makeText(getApplicationContext(), "Your GPS is not enabled", Toast.LENGTH_SHORT).show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Resources res = getResources();
        Bitmap src = BitmapFactory.decodeResource(res, R.drawable.gambit);
        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(res, src);
        dr.setCornerRadius(Math.max(src.getWidth(), src.getHeight()) / 2.0f);
        ImageView ImageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView);
        ImageView.setImageDrawable(dr);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


        //txtRequestUrl = (TextView) findViewById(R.id.txtRequestUrl);
        //txtResponseJson = (TextView) findViewById(R.id.txtResponseJson);
        WindDeg = (TextView) findViewById(R.id.winddeg);
        WindSpeed = (TextView) findViewById(R.id.windspeed);
        CoordLat = (TextView) findViewById(R.id.coordlat);
        CoordLon = (TextView) findViewById(R.id.coordlon);
        //Temperature = (TextView) findViewById(R.id.temp);
        Pressure = (TextView) findViewById(R.id.press);
        Humidity = (TextView) findViewById(R.id.humid);
        TempMin = (TextView) findViewById(R.id.tempmin);
        TempMax = (TextView) findViewById(R.id.tempmax);


        String SERVER_URL = "";
        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        Log.i("WEATHER", sharedPreferences.toString());
        for (String key : sharedPreferences.getAll().keySet()) {
            Log.i("WEATHER", key + " : " + sharedPreferences.getString(key, ""));
        }
        if(sharedPreferences.contains("cityId")){
            SERVER_URL = "http://api.openweathermap.org/data/2.5/weather?id=" + sharedPreferences.getString("cityId","") + "&units=metric&appid=8b62177ed538309f1fe0756026559a29";
        }
        //txtRequestUrl.setText(new Date() + "\n" + SERVER_URL);
        // Use AsyncTask to execute potential slow task without freezing GUI
        new LongOperation().execute(SERVER_URL);
    }


    private class LongOperation extends AsyncTask<String, Void, Void> {
        private String jsonResponse;
        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        protected void onPreExecute() {
            dialog.setMessage("Please wait..");
            dialog.show();
        }

        protected Void doInBackground(String... urls) {
            try {
                // solution uses Java.Net class (Apache.HttpClient is now deprecated)
                // STEP1. Create a HttpURLConnection object releasing REQUEST to given site
                URL url = new URL(urls[0]); //argument supplied in the call to AsyncTask
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("User-Agent", "");
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                // STEP2. wait for incoming RESPONSE stream, place data in a buffer
                InputStream isResponse = urlConnection.getInputStream();
                BufferedReader responseBuffer = new BufferedReader(new
                        InputStreamReader(isResponse));

                // STEP3. Arriving JSON fragments are concatenate into a StringBuilder
                String myLine = "";
                StringBuilder strBuilder = new StringBuilder();
                while ((myLine = responseBuffer.readLine()) != null) {
                    strBuilder.append(myLine);
                }
                //show response (JSON encoded data)
                jsonResponse = strBuilder.toString();
                Log.e("RESPONSE", jsonResponse);
            } catch (Exception e) {
                Log.e("RESPONSE Error", e.getMessage());
            }
            return null; // needed to gracefully terminate Void method
        }

        protected void onPostExecute(Void unused) {
            try {
                dialog.dismiss();

                // update GUI with JSON Response
                // txtResponseJson.setText(jsonResponse);

                // Step4. Convert JSON list into a Java collection of Person objects
                // prepare to decode JSON response and create Java list

                Gson gson = new Gson();
                try {
                    final Meteo m = gson.fromJson(jsonResponse,Meteo.class);
                    if (m != null) {
                        WindDeg.setText("Wind Degree is at : " + m.getWind().getDeg());
                        WindSpeed.setText("Wind Speed is at : " + m.getWind().getSpeed());
                        CoordLon.setText("Longitude : " + m.getCoord().getLon());
                        CoordLat.setText("Latitude : " + m.getCoord().getLat());
                        //Temperature.setText("Temperature : " + m.getMain().getTemp());
                        Pressure.setText("Pressure : " + m.getMain().getPressure());
                        Humidity.setText("Humidity : " + m.getMain().getHumidity());
                        TempMin.setText("Lowest Possible  Temperature : " + m.getMain().getTemp_min());
                        TempMax.setText("Highest Possible Temperature : " + m.getMain().getTemp_max());

                        //save to SharedPrefs
                        final SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("cityId", m.getId()+"");
                        editor.commit();

                        Button addLocationToFav = (Button)findViewById(R.id.favButton);
                        addLocationToFav.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boolean fullfav = true;
                                for (int j = 0; j < AddLocationActivity.maxFavorites; j++){
                                    if(favCities[j].id ==("")){
                                        fullfav = false;
                                    }
                                }
                                if(fullfav){
                                    Toast.makeText(getBaseContext(),"Your favorites is full ! Please remove some locations before adding", Toast.LENGTH_LONG).show();
                                }else{
                                    for (int i = 0; i < AddLocationActivity.maxFavorites; i++) {
                                        if(favCities[i].id == m.getId()+""){
                                            Toast.makeText(getBaseContext(),"This location is already in your Favorites", Toast.LENGTH_LONG).show();
                                            break;
                                        }
                                        if (favCities[i].id == "") {
                                            favCities[i].id = m.getId() + "";
                                            favCities[i].name = m.getName();
                                            break;
                                        }
                                    }
                                }

                                favoritesCities = getSharedPreferences("MyCities", Context.MODE_PRIVATE);
                                SharedPreferences.Editor mEdit1 = favoritesCities.edit();
                                mEdit1.putInt("Status_size", AddLocationActivity.maxFavorites);

                                //mEdit1.clear();
                                for(int i=0;i<AddLocationActivity.maxFavorites;i++)
                                {
                                    mEdit1.remove("Status_" + i + "id");
                                    mEdit1.remove("Status_" + i + "name");
                                    mEdit1.putString("Status_" + i + "id", favCities[i].getId());
                                    mEdit1.putString("Status_" + i + "name", favCities[i].getName());
                                }
                                mEdit1.commit();
                            }
                        });

                        //Button to show location
                        FloatingActionButton showLocation = (FloatingActionButton)findViewById(R.id.fab1);
                        showLocation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //String geoCode = "geo:" + m.getCoord().getLat() + "," + m.getCoord().getLon() + "?q=" + m.getName();
                                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                                intent.putExtra("longitude",m.getCoord().getLon());
                                intent.putExtra("latitude",m.getCoord().getLat());
                                startActivity(intent);
                            }
                        });
                    }else{
                        Log.e("WEATHER","Not found info");
                    }
                } catch (Exception e) {
                    Log.e("PARSING", e.getMessage());
                }

            } catch (JsonSyntaxException e) {
                Log.e("POST-Execute", e.getMessage());
            }
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //get access to collapsible searchView
        txtSearchValue = (SearchView) menu.findItem(R.id.action_search).getActionView();
        //set searchView listener
        txtSearchValue.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Toast.makeText(getApplicationContext(),"1-SUBMIT..." + query,Toast.LENGTH_SHORT).show();
                //recreate the original ActionBar
                query = query.replace(" ","");
                String SERVER_URL = "http://api.openweathermap.org/data/2.5/weather?q=" + query + ",vn&units=metric&appid=8b62177ed538309f1fe0756026559a29";
                new LongOperation().execute(SERVER_URL);
                invalidateOptionsMenu();
                //clear search view text
                txtSearchValue.setQuery("",false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // accept input one character at a time
                //txtMsg.append("\n2-CHANGE..." + newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // NOTED THAT SEARCH menuItems IS NOT PROCESSED IN THIS METHOD( IT HAS ITS OWN LISTENER SET BY onCreateOptionsMenu )
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            txtMsg.setText("Sharing...");
            //perform share operation...
            return true;
        } else if (id == R.id.action_download) {
            txtMsg.setText("Downloading...");
            //perform download operation...
            return true;
        } else if (id == R.id.action_about) {
            txtMsg.setText("About...");
            //perform about operation...
            return true;
        } else if (id == R.id.action_settings) {
            txtMsg.setText("Settings...");
            //perform settings operation...
            return true;
        }
        return false;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Toast.makeText(getApplicationContext(),"I clicked on Home", Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_favorites) {
            Toast.makeText(getApplicationContext(),"I clicked on Favorites", Toast.LENGTH_LONG).show();
            Intent intentAdd = new Intent(MainActivity.this,AddLocationActivity.class);
            startActivity(intentAdd);
        } else if (id == R.id.nav_search) {
            Toast.makeText(getApplicationContext(),"I clicked on Search", Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_notifications) {
            Toast.makeText(getApplicationContext(),"I clicked on Notifications", Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_settings){
            Toast.makeText(getApplicationContext(),"I clicked on Settings", Toast.LENGTH_LONG).show();
        }else if (id == R.id.nav_about){
            Toast.makeText(getApplicationContext(),"I clicked on About", Toast.LENGTH_LONG).show();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://puf.example.td1/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://puf.example.td1/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
