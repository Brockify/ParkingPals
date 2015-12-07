package parkingpal.com.parkingpal;

import android.app.ActionBar;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Brockyy on 11/12/2015.
 */
public class Main_Activity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener  {

    Button paySubmitButton;
    get_parking_lots getParkingLots;
    ListView parkingLotListView;
    ArrayList<String> parkingLots = null;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.fragment_open_spots_);
       LayoutInflater mInflater = LayoutInflater.from(this);
       View mCustomView = mInflater.inflate(R.layout.mainactivity_actionbar, null);
       getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
       getSupportActionBar().setCustomView(mCustomView);
       parkingLotListView = (ListView) findViewById(R.id.openSpotListView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
       //when "Pay Now" button gets clicked
       ImageButton checkInButton = (ImageButton) findViewById(R.id.checkButton);
       checkInButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               LayoutInflater layoutInflater = LayoutInflater.from(Main_Activity.this);
               View promptView = layoutInflater.inflate(R.layout.popup_checkin, null);
               AlertDialog.Builder builder = new AlertDialog.Builder(Main_Activity.this);
               builder.setView(promptView);
               builder.create();
               builder.show();

               //when pay button gets clicked
               final Button payButton = (Button) promptView.findViewById(R.id.payButton);
               payButton.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       LayoutInflater layoutInflater = LayoutInflater.from(Main_Activity.this);
                       View promptView = layoutInflater.inflate(R.layout.popup_spotnumber, null);
                       final AlertDialog.Builder dialog = new AlertDialog.Builder(Main_Activity.this);
                       dialog.setView(promptView);
                       final AlertDialog spotNumDialog = dialog.create();
                       spotNumDialog.show();
                       paySubmitButton = (Button) promptView.findViewById(R.id.paySubmitButton);
                       paySubmitButton.setText("Pay");
                       paySubmitButton.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View view) {
                               spotNumDialog.dismiss();
                           }
                       });
                   }
               });

               //when studentButton gets clicked
               Button studentButton = (Button) promptView.findViewById(R.id.parkingIdButton);
               studentButton.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       LayoutInflater inflaterl = LayoutInflater.from(Main_Activity.this);
                       View promptView = inflaterl.inflate(R.layout.popup_spotnumber, null);
                       AlertDialog.Builder builder = new AlertDialog.Builder(Main_Activity.this);
                       builder.setView(promptView);
                       final AlertDialog spotNumDialog = builder.create();
                       spotNumDialog.show();
                       paySubmitButton = (Button) promptView.findViewById(R.id.paySubmitButton);
                       paySubmitButton.setText("Check in");
                       paySubmitButton.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View view) {
                               spotNumDialog.dismiss();
                           }
                       });
                   }
               });
           }
       });

       parkingLotListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               TextView lotNumberTextView = (TextView) view.findViewById(R.id.parkingLotTextView);
               String lotNum = (String) lotNumberTextView.getText();
               LayoutInflater layoutInflater = LayoutInflater.from(Main_Activity.this);
               View promptView = layoutInflater.inflate(R.layout.popup_map, null);
               AlertDialog.Builder alert = new AlertDialog.Builder(Main_Activity.this);
               alert.setView(promptView);
               alert.setTitle("Parking lot " + lotNum);
               AlertDialog dialog = alert.create();
               dialog.show();
           }
       });

       getParkingLots = new get_parking_lots();
       getParkingLots.execute();
   }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_open_spots_, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View promptView = inflater.inflate(R.layout.activity_settings, null);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setView(promptView);
            alertDialog.setTitle("Settings");
            final AlertDialog dialog = alertDialog.create();
            dialog.show();

            Button saveButton = (Button) promptView.findViewById(R.id.saveButton);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        parkingLots.clear();
        getParkingLots = new get_parking_lots();
        getParkingLots.execute();
    }

    class get_parking_lots extends AsyncTask<Void, Void, String>
    {
        @Override
        protected String doInBackground(Void... voids) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://www.skyrealmstudio.com/cgi-bin/ParkingPal/GetParkingLots.py");
            String responseString = null;
            parkingLots = new ArrayList<>();

            try {
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                responseString = EntityUtils.toString(response.getEntity());
                JSONArray jsonArray = new JSONArray(responseString);
                for(int i = 0; i < jsonArray.length(); i++)
                {
                    String lot = null;
                    lot = jsonArray.getJSONObject(i).getString("Lot");
                    parkingLots.add(lot);
                }

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return responseString;
        }

        protected void onPostExecute(String result)
        {
            ListAdapter adapter = new ArrayAdapter(Main_Activity.this, R.layout.parking_lots_item, R.id.parkingLotTextView, parkingLots);
            parkingLotListView.setAdapter(adapter);
            if(swipeRefreshLayout.isRefreshing())
            {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}
