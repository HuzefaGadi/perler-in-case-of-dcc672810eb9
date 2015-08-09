package com.lazydroid.incaseof;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;


public class SettingsActivity extends ActionBarActivity {

    Button addressBook, save;
    EditText emailAddresses, interval,password;
    Spinner exposure;
    RadioGroup radioGroup;
    SharedPreferences preferences;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        radioGroup = (RadioGroup) findViewById(R.id.camera_settings);
        exposure = (Spinner) findViewById(R.id.exposureText);
        interval = (EditText) findViewById(R.id.shotIntervalText);
        save = (Button) findViewById(R.id.save);
        addressBook = (Button) findViewById(R.id.address_book);
        emailAddresses = (EditText) findViewById(R.id.emailAddress);
        password = (EditText)findViewById(R.id.password);
        preferences = getSharedPreferences(InCaseOfApp.PREFERENCES, MODE_PRIVATE);
        edit = preferences.edit();
        emailAddresses.setText(preferences.getString(InCaseOfApp.SHOOTING_EMAIL_ADDRESS, ""));
        interval.setText(preferences.getInt(InCaseOfApp.SHOOTING_INTERVAL, 2) + "");
        exposure.setSelection(preferences.getInt(InCaseOfApp.SHOOTING_EXPOSURE, 0));
        password.setText(preferences.getString(InCaseOfApp.PASSWORD,"000000"));
        int flash = preferences.getInt(InCaseOfApp.SHOOTING_FLASH, 0);
        switch (flash) {
            case 1:

                radioGroup.check(R.id.on);
                break;
            case 2:
                radioGroup.check(R.id.off);
                break;
            case 0:
                radioGroup.check(R.id.auto);
                break;
            default:
                radioGroup.check(R.id.auto);
                break;
        }
        addressBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(ContactsContract.CommonDataKinds.Email.CONTENT_TYPE);
                startActivityForResult(intent, 1);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int flash = 0;
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.on:
                        flash = 1;
                        break;
                    case R.id.off:
                        flash = 2;
                        break;
                    case R.id.auto:
                        flash = 0;
                        break;
                    default:
                        flash = 0;
                        break;
                }
                edit.putInt(InCaseOfApp.SHOOTING_INTERVAL, Integer.parseInt(interval.getText().toString()));
                edit.putInt(InCaseOfApp.SHOOTING_FLASH, flash);
                edit.putInt(InCaseOfApp.SHOOTING_EXPOSURE,exposure.getSelectedItemPosition());
                edit.putString(InCaseOfApp.SHOOTING_EMAIL_ADDRESS, emailAddresses.getText().toString().trim());
                edit.putString(InCaseOfApp.PASSWORD,password.getText().toString());
                edit.commit();

                Toast.makeText(getApplicationContext(), "Saved successfully", Toast.LENGTH_LONG).show();

            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = this.managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();
                String email = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                emailAddresses.setText(emailAddresses.getText() + email + ",");
            }


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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
