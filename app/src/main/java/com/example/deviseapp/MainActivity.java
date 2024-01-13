package com.example.deviseapp;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMontantDevise;
    private Spinner spinnerResultDevise;

    private JsonObject rates;

    private static final String API_KEY = "dc36d1d92c5c8cf35b4269e1c2eb81d9";
    private static final String API_URL = "http://api.exchangeratesapi.io/v1/latest?access_key=" + API_KEY;

    private EditText montant;

    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        spinnerMontantDevise = findViewById(R.id.montantDevise);
        spinnerResultDevise = findViewById(R.id.resultatDevise);
        montant = findViewById(R.id.montant);
        result = findViewById(R.id.result);

        // Load exchange rates asynchronously
        new LoadDataAsyncTask().execute();
    }

    // Method to calculate currency conversion
    public void calculer(View view) {
        double montantIN = Double.parseDouble(montant.getText().toString());
        double DeviseOutValue = Double.parseDouble(String.valueOf(rates.get(spinnerResultDevise.getSelectedItem().toString())));
        double DeviseIN = Double.parseDouble(String.valueOf(rates.get(spinnerMontantDevise.getSelectedItem().toString())));

        // Check for valid input values and perform the conversion
        if (montantIN != 0 && DeviseIN != 0 && DeviseOutValue != 0) {
            result.setText(String.valueOf((montantIN * DeviseOutValue) / DeviseIN));
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadDataAsyncTask extends AsyncTask<Void, Void, JsonObject> {

        @Override
        protected JsonObject doInBackground(Void... voids) {
            try {
                // Establish a connection to the exchange rates API
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();

                // Check if the API request was successful (HTTP status code 200)
                if (responseCode == 200) {
                    // Read the API response
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    StringBuilder response = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();

                    // Parse the JSON response
                    String responseData = response.toString();
                    return JsonParser.parseString(responseData).getAsJsonObject();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JsonObject result) {
            if (result != null) {
                // Extract exchange rates from the JSON response
                rates = result.getAsJsonObject("rates");

                // Get the list of currency codes
                List<String> keys = new ArrayList<>(rates.keySet());

                // Create adapters and set them for the spinners
                ArrayAdapter<String> adapterMontant = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, keys);
                ArrayAdapter<String> adapterDevice = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, keys);

                adapterMontant.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                adapterDevice.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinnerMontantDevise.setAdapter(adapterMontant);
                spinnerResultDevise.setAdapter(adapterDevice);
            } else {
                // Log an error if there is no JSON data
                Log.e("Fail", "No JSON Data");
            }
        }
    }
}
