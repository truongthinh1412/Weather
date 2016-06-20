package puf.example.td1;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddLocationActivity extends AppCompatActivity {

    static int maxFavorites = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        final LinearLayout lm = (LinearLayout) findViewById(R.id.linearMain);

        // create the layout params that will be used to define how your
        // button will be displayed


        LinearLayout.LayoutParams paramsB= new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,1.0f);

        LinearLayout.LayoutParams paramsT= new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,9.0f);

        LinearLayout.LayoutParams params= new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
        for(int j=0;j<maxFavorites;j++)
        {
            //if(MainActivity.favoritesCities.getString("Status_" + j + "id", "") != "") {
                LinearLayout lh = new LinearLayout(this);
                lh.setOrientation(LinearLayout.HORIZONTAL);
                lh.setWeightSum(10);
                lh.setLayoutParams(params);
                // Create TextView
                TextView city = new TextView(this);
                final SharedPreferences favoritesCities = getSharedPreferences("MyCities",Context.MODE_PRIVATE);
                final SharedPreferences.Editor mEdit1 = favoritesCities.edit();
                city.setText(favoritesCities.getString("Status_" + j + "id", "") + " " + favoritesCities.getString("Status_" + j + "name", ""));
                city.setGravity(Gravity.CENTER);
                city.setLayoutParams(paramsT);
                lh.addView(city);

                // Create Button
                final Button btn = new Button(this);

                // Give button an ID
                btn.setId(j);
                btn.setText("Remove");
                btn.setGravity(Gravity.CENTER);
                // set the layoutParams on the button
                btn.setLayoutParams(paramsB);

                final int index = j;
                // Set click listener for button
                btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        int id = v.getId();
                        MainActivity.favCities[id] = new City();
                        mEdit1.remove("Status_" + id + "id");
                        mEdit1.remove("Status_" + id + "name");
                    }
                });

                lh.addView(btn);

                lm.addView(lh,j);
            //}
        }
    }
}
