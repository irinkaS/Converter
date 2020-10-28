package com.example.converter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

// TODO Work on Analyze - Inspect Code
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String currencyFrom;
    String currencyTo;
    BigDecimal sumFrom;
    BigDecimal sumTo;
    Spinner spinnerCurrencyFrom;
    Spinner spinnerCurrencyTo;
    EditText editText;
    EditText editText2;
    BigDecimal rate;
    public static final int NO_RATE = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerCurrencyFrom = findViewById(R.id.spinner_currencyFrom);
        spinnerCurrencyFrom.setOnItemSelectedListener(this);
        spinnerCurrencyTo = findViewById(R.id.spinner_currencyTo);
        spinnerCurrencyTo.setOnItemSelectedListener(this);
        editText = findViewById(R.id.editText_quantityOfCurrencyFrom);
        editText2 = findViewById(R.id.editText_quantityOfCurrencyTo);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCurrentRate();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    private void convertSum(){
        sumFrom = new BigDecimal(editText.getText().toString());
        sumTo = sumFrom.multiply(rate);
        editText2.setText(sumTo.toString());
    }

    private void updateCurrentRate(){
        if (isNewReq()){
            asyncExecute();
        }else{
            readRateFromCache();
            convertSum();
            //check NO_RATE
        }
    }


    private void asyncExecute() {
        AsyncTask.execute(() -> {
            try {
                URL endpoint = new URL("https://free.currencyconverterapi.com/api/v6/convert?q=" + currencyFrom + "_" + currencyTo + "&compact=ultra");
                HttpsURLConnection myConnection = (HttpsURLConnection) endpoint.openConnection();
                if (myConnection.getResponseCode() == 200) {
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader =
                            new InputStreamReader(responseBody, "UTF-8");
                    JsonReader jsonReader = new JsonReader(responseBodyReader);
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        String key = jsonReader.nextName();
                        rate = new BigDecimal(jsonReader.nextString());
                        convertSum();

                        writeCache();
                    }
                    jsonReader.close();
                    myConnection.disconnect();
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                readRateFromCache();
            } catch (Exception e) {
                editText2.setText("");
            }
        });
    }

    private void readRateFromCache(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        // TODO get data from list of RUB, USD etc. by index, form string like USD_RUB
        Float cacheRate = sharedPref.getFloat(getString(R.string.RUB_USD), NO_RATE);
        rate = new BigDecimal(cacheRate);
    }

    private void writeCache() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        //TODO make while cycle for every currency pair
        if (currencyFrom.equals("RUB") && currencyTo.equals("EUR")) {
            editor.putFloat(getString(R.string.RUB_EUR), Float.parseFloat(rate.toString()));
        } else if (currencyFrom.equals("RUB") && currencyTo.equals(("USD"))) {
            editor.putFloat(getString(R.string.RUB_USD), Float.parseFloat(rate.toString()));
        } else if (currencyFrom.equals("EUR") && currencyTo.equals(("RUB"))) {
            editor.putFloat(getString(R.string.EUR_RUB), Float.parseFloat(rate.toString()));
        } else if (currencyFrom.equals("USD") && currencyTo.equals(("RUB"))) {
            editor.putFloat(getString(R.string.USD_RUB), Float.parseFloat(rate.toString()));
        } else if (currencyFrom.equals("USD") && currencyTo.equals(("EUR"))) {
            editor.putFloat(getString(R.string.USD_EUR), Float.parseFloat(rate.toString()));
        } else if (currencyFrom.equals("EUR") && currencyTo.equals(("USD"))) {
            editor.putFloat(getString(R.string.EUR_USD), Float.parseFloat(rate.toString()));
        }
        editor.putLong("timeOfPrevReq", System.nanoTime());
        editor.putLong("timeOfNextReq", (long) (System.nanoTime() + 3600 * 10E9));
        editor.apply();
    }

    private boolean isNewReq () {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        //TODO repair time of request
        Long timeOfNextReq = sharedPref.getLong(getString(R.string.TIME_OF_NEXT_REQ), 0);
        return System.nanoTime() > timeOfNextReq;

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        currencyFrom = spinnerCurrencyFrom.getSelectedItem().toString();
        currencyTo = spinnerCurrencyTo.getSelectedItem().toString();
        asyncExecute();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        throw new NullPointerException("Nothing selected");
    }
}
