package com.example.varsha.cancan;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.Interface.ValueChangedListener;
import com.travijuu.numberpicker.library.NumberPicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class MainActivity extends ActionBarActivity {


    int price;
    NumberPicker orderpicker, emptypicker;
    Button b1;
    private final int REQUEST_CODE_PLACEPICKER = 1;
    EditText user_location, user_name, user_phone;
    String userName;
    String userLocation;
    String userPhone;
    String responseServer;
    String report, message;
    ProgressDialog progressDialog;
    ImageView add_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button) findViewById(R.id.orderButton);
        orderpicker = (NumberPicker) findViewById(R.id.pickerOrder);
        emptypicker = (NumberPicker) findViewById(R.id.pickerEmpty);
        user_location = (EditText) findViewById(R.id.user_location);
        user_name = (EditText) findViewById(R.id.name);
        user_phone = (EditText) findViewById(R.id.phone);

        add_location = (ImageView) findViewById(R.id.user_location_picker);
        add_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                // this would only work if you have your Google Places API working
                try {
                    Intent intent = intentBuilder.build(MainActivity.this);
                    startActivityForResult(intent, REQUEST_CODE_PLACEPICKER);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


        b1.setText("Payment  Rs/- 30");
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userName = user_name.getText().toString();
                userPhone = user_phone.getText().toString();
                userLocation = user_location.getText().toString();

                int count = 0;
                if (userName.length() == 0) {
                    count++;
                    user_name.setError("Required Field");
                }
                if ((userPhone.length() == 0) && (userPhone.length() < 10)) {
                    count++;
                    user_phone.setError("Not a Valid Phone Number");
                }

                userLocation = user_location.getText().toString();
                if (userLocation.length() == 0) {
                    count++;
                    user_location.setError("Required Field");
                }
                if (count == 0) {
                    Toast.makeText(MainActivity.this, "Name::" + userName + "\nPhone Number::" + userPhone + "\nLocation::" + userLocation + "\nTotal Price::" + calculate(), Toast.LENGTH_SHORT).show();
                    AsyncT asyncT = new AsyncT();
                    asyncT.execute();
                }

            }
        });

        orderpicker.setValueChangedListener(new ValueChangedListener() {
            @Override
            public void valueChanged(int value, ActionEnum action) {
                emptypicker.setValue(orderpicker.getValue());
                price = calculate();
                //         Toast.makeText(MainActivity.this, "Price: Rs/- " + price, Toast.LENGTH_SHORT).show();

            }
        });

        emptypicker.setValueChangedListener(new ValueChangedListener() {
            @Override
            public void valueChanged(int value, ActionEnum action) {
                price = calculate();
                //       Toast.makeText(MainActivity.this, "Price: Rs/- " + price, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PLACEPICKER && resultCode == RESULT_OK) {
            Place placeSelected = PlacePicker.getPlace(data, this);

            //String name = placeSelected.getName().toString();
            String address = placeSelected.getAddress().toString();
            user_location.setText(address);
        }
    }


    int calculate() {

        int value = 0;
        if (orderpicker.getValue() == emptypicker.getValue()) {

            value = 30 * orderpicker.getValue();
            b1.setText("Payment  Rs/- " + value);
        }

        if (emptypicker.getValue() > orderpicker.getValue()) {

            emptypicker.setValue(emptypicker.getValue() - 1);
            value = 30 * orderpicker.getValue();
            b1.setText("Payment  Rs/- " + value);
        }

        if (orderpicker.getValue() > emptypicker.getValue()) {

            value = (orderpicker.getValue() * 30) + (orderpicker.getValue() - emptypicker.getValue()) * 250;
            b1.setText("Payment  Rs/- " + value);
        }
        return value;
    }

    void showDialogAlert(String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage(msg);
        alertDialog.setIcon(R.drawable.error);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }


    class AsyncT extends AsyncTask<Object, Object, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Processing ......");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Object... voids) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("https://varshadhoni.000webhostapp.com/CanCan/RequestUsers.php");

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("name", userName));
                nameValuePairs.add(new BasicNameValuePair("phone", userPhone));
                nameValuePairs.add(new BasicNameValuePair("location", userLocation));
                nameValuePairs.add(new BasicNameValuePair("payment", String.valueOf(price)));
                nameValuePairs.add(new BasicNameValuePair("ordercans", String.valueOf(orderpicker.getValue())));
                nameValuePairs.add(new BasicNameValuePair("returncans", String.valueOf(emptypicker.getValue())));
                Log.e("order:::", String.valueOf(orderpicker.getValue()));
                Log.e("empty:::", String.valueOf(emptypicker.getValue()));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                InputStream inputStream = response.getEntity().getContent();
                InputStreamToStringExample str = new InputStreamToStringExample();

                responseServer = str.getStringFromInputStream(inputStream);
                Log.e("response", "response -----" + responseServer);

            } catch (Exception e) {
                Log.e("sister", e.toString());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                JSONObject json = new JSONObject(responseServer);
                JSONArray results = json.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject c = results.getJSONObject(i);
                    report = c.getString("report");
                    message = c.getString("message");

                }
            } catch (Exception e) {
                Log.e("varsha-_sister", e.toString());
                e.printStackTrace();
            }

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (report.equals("success")) {
                //Intent to next acctivity

            } else {
                showDialogAlert(message);

            }


        }
    }


}

class InputStreamToStringExample {

    public static void main(String[] args) throws IOException {

        // intilize an InputStream
        InputStream is =
                new ByteArrayInputStream("file content..blah blah".getBytes());

        String result = getStringFromInputStream(is);

        Log.e("result value server:", result);


    }

    // convert InputStream to String
    static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

}

