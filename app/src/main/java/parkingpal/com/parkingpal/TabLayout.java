package parkingpal.com.parkingpal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class TabLayout extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    SharedPreferences sharedPreferences;
    get_parking_lots getParkingLots;
    ListView parkingLotListView;
    AlertDialog parkingSpotAlert, settingsDialog, newCardDialog, newParkingIDDialog, timeDialog;
    String creditCard = "1111111111111111", csc = "303", expiration = "05/10";
    double cardAmount = 50, currentRate = .50;

    // Declaring Your View and Variables
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = TabLayout.this.getSharedPreferences("parkingpal.com.parkingpal", Context.MODE_PRIVATE);
        getParkingLots = new get_parking_lots();
        getParkingLots.execute();

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
            LayoutInflater layoutInfalter = LayoutInflater.from(TabLayout.this);
            AlertDialog.Builder builder = new AlertDialog.Builder(TabLayout.this);
            View promptView = layoutInfalter.inflate(R.layout.settings_popup, null);
            builder.setView(promptView);
            settingsDialog = builder.create();
            settingsDialog.show();
            final Button newCardButton = (Button) promptView.findViewById(R.id.newCardButton);
            Button parkingIdButton = (Button) promptView.findViewById(R.id.parkingIdButton);

            if(!sharedPreferences.getString("CardNumber", "").equals("") && !sharedPreferences.getString("CSC", "").equals("") && !sharedPreferences.getString("Expiration", "").equals(""))
            {
                newCardButton.setText("Clear Card");
            }

            newCardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (newCardButton.getText().toString().equals("Clear Card")) {
                        sharedPreferences.edit().putString("CardNumber", "").putString("CSC", "").putString("Expiration", "").apply();
                        newCardButton.setText("New Card");
                    } else {
                        LayoutInflater newCardInflater = LayoutInflater.from(TabLayout.this);
                        AlertDialog.Builder builder = new AlertDialog.Builder(TabLayout.this);
                        final View promptView = newCardInflater.inflate(R.layout.new_card_popup, null);
                        builder.setView(promptView);
                        newCardDialog = builder.create();
                        newCardDialog.show();
                        Button saveButton = (Button) promptView.findViewById(R.id.saveButton);
                        final EditText cardNumber = (EditText) promptView.findViewById(R.id.cardNumberEditText);
                        final EditText CSC = (EditText) promptView.findViewById(R.id.cscEditText);
                        final EditText expirationDate = (EditText) promptView.findViewById(R.id.expirationEditText);
                        cardNumber.setText(sharedPreferences.getString("CardNumber", null));
                        CSC.setText(sharedPreferences.getString("CSC", null));
                        expirationDate.setText(sharedPreferences.getString("Expiration", null));
                        saveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (cardNumber.getText().length() != 16 && cardNumber.getText().length() != 0) {
                                    Toast.makeText(TabLayout.this, "Card Number must be 16 digits", Toast.LENGTH_LONG).show();
                                } else if (CSC.getText().length() != 3 && CSC.getText().length() != 0) {
                                    Toast.makeText(TabLayout.this, "CSC Must be 3 digits long", Toast.LENGTH_LONG).show();
                                } else {
                                    sharedPreferences.edit().putString("CardNumber", cardNumber.getText().toString()).putString("CSC", CSC.getText().toString()).putString("Expiration", expirationDate.getText().toString()).apply();
                                    Toast.makeText(TabLayout.this, "Card saved", Toast.LENGTH_LONG).show();
                                    newCardDialog.dismiss();
                                    newCardButton.setText("Clear Card");
                                }
                            }
                        });


                    }
                }
            });

            parkingIdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutInflater parkingIDInflater = LayoutInflater.from(TabLayout.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(TabLayout.this);
                    View promptView = parkingIDInflater.inflate(R.layout.enter_parking_id_popup, null);
                    builder.setView(promptView);
                    newParkingIDDialog = builder.create();
                    newParkingIDDialog.show();
                    final EditText parkingIdEditText = (EditText) promptView.findViewById(R.id.parkingIdEditText);
                    if (sharedPreferences != null) {
                        if (!sharedPreferences.getString("ParkingID", "").equals("")) {
                            parkingIdEditText.setText(sharedPreferences.getString("ParkingID", ""));
                        }
                    }

                    Button saveButton = (Button) promptView.findViewById(R.id.saveButton);
                    saveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (parkingIdEditText.getText() != null) {
                                sharedPreferences = TabLayout.this.getSharedPreferences("parkingpal.com.parkingpal", Context.MODE_PRIVATE);
                                sharedPreferences.edit().putString("ParkingID", parkingIdEditText.getText().toString()).apply();
                                Toast.makeText(TabLayout.this, "Parking ID Saved", Toast.LENGTH_LONG).show();
                                newParkingIDDialog.dismiss();
                            } else {
                                sharedPreferences.edit().putString("ParkingID", parkingIdEditText.getText().toString()).apply();
                            }
                        }
                    });
                }
            });


            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        parkingLotListView.
    }

    class get_parking_lots extends AsyncTask<Void, Void, ArrayList<String>>
    {
        HttpResponse response;
        String htmlUrl;
        JSONArray jsonArray;
        ArrayList<String> parkingLots;


        protected void onPreExecute()
        {
            parkingLots = new ArrayList<>();
            htmlUrl = "http://www.skyrealmstudio.com/cgi-bin/ParkingPal/GetParkingLots.py";
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            // Creating HTTP client
            HttpClient httpClient = new DefaultHttpClient();
            // Creating HTTP Post
            HttpPost httpPost = new HttpPost(htmlUrl);
            String responseBody = null;

            // Building post parameters
            // key and value pair
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            //nameValuePair.add(new BasicNameValuePair("Username", user));

            // Url Encoding the POST parameters
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                // writing error to Log
                e.printStackTrace();
            }

            // Making HTTP Request
            try {
                HttpResponse response = httpClient.execute(httpPost);
                responseBody = EntityUtils.toString(response.getEntity());
                jsonArray = new JSONArray(responseBody);

                if(jsonArray != null)
                {
                    JSONObject jsonObject = new JSONObject();
                    for(int i = 0; i < jsonArray.length(); i++)
                    {
                        try {
                            parkingLots.add(jsonArray.getJSONObject(i).getString("Lot"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // writing response to log
            } catch (ClientProtocolException e) {
                // writing exception to log
                e.printStackTrace();
            } catch (IOException e) {
                // writing exception to log
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return parkingLots;
        }

        protected void onPostExecute(ArrayList<String> result)
        {
            parkingLotListView = (ListView) findViewById(R.id.parkingLotListView);
            if(result != null)
            {
                ArrayAdapter adapter = new ArrayAdapter(TabLayout.this, R.layout.parking_lot_list_item, R.id.usernameTextView, parkingLots);
                parkingLotListView.setAdapter(adapter);
            } else {
                parkingLotListView.setAdapter(null);
            }

            parkingLotListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {LayoutInflater layoutInflater = LayoutInflater.from(TabLayout.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(TabLayout.this);
                    View promptView = layoutInflater.inflate(R.layout.pay_now_popup, null);
                    builder.setView(promptView);
                    parkingSpotAlert = builder.create();
                    parkingSpotAlert.show();

                    TextView usernameTextView = (TextView) view.findViewById(R.id.usernameTextView);
                    final TextView parkingSpotTextView = (TextView) promptView.findViewById(R.id.parkingSpotTextView);
                    Button payCheckInButton = (Button) promptView.findViewById(R.id.payCheckInButton);
                    Button parkingIDCheckInButton = (Button) promptView.findViewById(R.id.parkingIDCheckInButton);

                    parkingSpotTextView.setText(usernameTextView.getText());

                    payCheckInButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!sharedPreferences.getString("CardNumber", "").equals("") && !sharedPreferences.getString("CSC", "").equals("") && !sharedPreferences.getString("Expiration", "").equals("")) {
                                LayoutInflater layoutInflater1 = LayoutInflater.from(TabLayout.this);
                                AlertDialog.Builder builder = new AlertDialog.Builder(TabLayout.this);
                                final View promptView = layoutInflater1.inflate(R.layout.time_popup, null);
                                builder.setView(promptView);
                                timeDialog = builder.create();
                                timeDialog.show();

                                Button payButton = (Button) promptView.findViewById(R.id.payButton);
                                payButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        EditText hoursEditText = (EditText) promptView.findViewById(R.id.timeEditText);
                                        String hours = hoursEditText.getText().toString();

                                        if (!sharedPreferences.getString("CardNumber", "").equals("") && !sharedPreferences.getString("CSC", "").equals("") && !sharedPreferences.getString("Expiration", "").equals("")) {
                                            if (hours.equals("")) {
                                                hours = "0";
                                                Toast.makeText(TabLayout.this, "Please enter a valid amount of hours", Toast.LENGTH_LONG).show();
                                            } else if (Integer.parseInt(hours) <= 0) {
                                                Toast.makeText(TabLayout.this, "Please enter a valid amount of hours", Toast.LENGTH_LONG).show();
                                            } else {
                                                if ((cardAmount - (Double.parseDouble(hours) * currentRate)) >= 0 && sharedPreferences.getString("CardNumber", "").equals(creditCard) && sharedPreferences.getString("CSC", "").equals(csc) && sharedPreferences.getString("Expiration", "").equals(expiration)) {
                                                    cardAmount = cardAmount - (Double.parseDouble(hours) * currentRate);
                                                    Toast.makeText(TabLayout.this, "Successfully paid! Remaining card balance = " + String.valueOf(cardAmount), Toast.LENGTH_LONG).show();
                                                    parkingLots.remove(parkingLots.indexOf(parkingSpotTextView.getText().toString()));
                                                    ArrayAdapter adapter = new ArrayAdapter(TabLayout.this, R.layout.parking_lot_list_item, R.id.usernameTextView, parkingLots);
                                                    parkingLotListView.setAdapter(adapter);
                                                    parkingSpotAlert.dismiss();
                                                    timeDialog.dismiss();
                                                } else {
                                                    Toast.makeText(TabLayout.this, "Error: Card not valid or not enough funds.", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(TabLayout.this, "Add your credit card in the settings.", Toast.LENGTH_LONG).show();
                                parkingSpotAlert.dismiss();
                            }
                        }
                    });


                    parkingIDCheckInButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!sharedPreferences.getString("ParkingID", "").equals("")) {
                                Toast.makeText(TabLayout.this, "Successfully checked in!", Toast.LENGTH_LONG).show();
                                parkingSpotAlert.dismiss();
                            } else {
                                Toast.makeText(TabLayout.this, "Add your Parking ID in the settings.", Toast.LENGTH_SHORT).show();
                                parkingSpotAlert.dismiss();
                            }
                        }
                    });

                }
            });
        }
    }

}
