package com.example.converter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fillSpinner();
    }

    //fill both spinners with currencies
    private void fillSpinner() {
        Spinner spinnerFrom = (Spinner) findViewById(R.id.spinner_currencyFrom);
        Spinner spinnerTo = (Spinner) findViewById(R.id.spinner_currencyTo);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);
    }


}
