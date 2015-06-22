package com.blinduck.Postalgia;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 7/5/13
 * Time: 10:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class SettingsActivity2 extends SherlockActivity implements TextWatcher {

    private EditText firstName;
    private ImageView firstNameImage;

    private EditText lastName;
    private ImageView lastNameImage;

    private EditText mobile;
    private ImageView mobileImage;

    private EditText email;
    private ImageView emailImage;

    private EditText  block;
    private ImageView blockImage;

    private EditText street;
    private ImageView streetImage;

    private EditText unit;
    private ImageView unitImage;

    private EditText postal;
    private ImageView postalImage;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);    //To change body of overridden methods use File | Settings | File Templates.

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings_save) {
            finish();
        }
        return super.onOptionsItemSelected(item);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);

        prefs = getSharedPreferences(WelcomeActivity.PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();

        firstName = (EditText) findViewById(R.id.prefs_firstName);
        firstNameImage = (ImageView) findViewById(R.id.prefs_firstName_v);

        lastName = (EditText) findViewById(R.id.prefs_lastName);
        lastNameImage = (ImageView)findViewById(R.id.prefs_lastName_v);

        mobile = (EditText)findViewById(R.id.prefs_mobile);
        mobileImage = (ImageView)findViewById(R.id.prefs_mobile_v);

        email = (EditText)findViewById(R.id.prefs_email);
        emailImage = (ImageView) findViewById(R.id.prefs_email_v);

        block = (EditText)findViewById(R.id.prefs_block);
        blockImage = (ImageView)findViewById(R.id.prefs_block_v);

        street = (EditText)findViewById(R.id.prefs_street);
        streetImage = (ImageView)findViewById(R.id.prefs_street_v);

        unit = (EditText)findViewById(R.id.prefs_unit);
        unitImage = (ImageView)findViewById(R.id.prefs_unit_v);

        postal = (EditText)findViewById(R.id.prefs_postal);
        postalImage =(ImageView)findViewById(R.id.prefs_postal_v);

        setAllValues();

        firstName.addTextChangedListener(this);
        lastName.addTextChangedListener(this);
        mobile.addTextChangedListener(this);
        email.addTextChangedListener(this);

        block.addTextChangedListener(this);
        street.addTextChangedListener(this);
        unit.addTextChangedListener(this);
        postal.addTextChangedListener(this);
        validateAll();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == android.R.id.home)  {
            finish();
        }

        return super.onMenuItemSelected(featureId, item);    //To change body of overridden methods use File | Settings | File Templates.
    }

    private void setAllValues() {
        if (prefs.contains("firstName")) firstName.setText(prefs.getString("firstName", ""));
        if(prefs.contains("lastName"))  lastName.setText(prefs.getString("lastName", ""));
        if (prefs.contains("mobile"))   mobile.setText(prefs.getString("mobile", ""));
        email.setText(prefs.getString("email", ""));

        block.setText(prefs.getString("block", ""));
        street.setText(prefs.getString("street", ""));
        unit.setText(prefs.getString("unit", ""));
        postal.setText(prefs.getString("postal", ""));



    }

    public void validateAll ()  {
        editor.putBoolean("ALLVALID", true);
        editor.commit();



        String firstNameText = firstName.getText().toString();
        String lastNameText = lastName.getText().toString();
        String mobileText = mobile.getText().toString();
        String emailText = email.getText().toString();

        String blockText = block.getText().toString();
        String streetText = street.getText().toString();
        String unitText = unit.getText().toString();
        String postalText = postal.getText().toString();

        saveValue("firstName", firstNameText);
        saveValue("lastName", lastNameText);
        saveValue("mobile", mobileText);
        saveValue("email", emailText);

        saveValue("block", blockText);
        saveValue("street", streetText);
        saveValue("unit", unitText);
        saveValue("postal", postalText);

        setValid(firstNameImage, isNotNullCheck(firstNameText));
        setValid(lastNameImage, isNotNullCheck(lastNameText));
        setValid(mobileImage, isValidMobileNumber(mobileText));
        setValid(emailImage, isEmailValid(emailText));

        setValid (blockImage, isNotNullCheck(blockText));
        setValid(streetImage, isNotNullCheck(streetText));
        setValid(unitImage, isNotNullOptionalFieldCheck(unitText));
        setValid(postalImage, isPostalCodeCheck(postalText));



    }



    private void setAllValidFalse () {
        editor.putBoolean("ALLVALID", false);
        editor.commit();

    }

    private void saveValue(String prefName, String prefValue) {
        editor.putString(prefName, prefValue);
        editor.commit();

    }


    /*Checks*/

    private boolean isPostalCodeCheck(String postalText) {
        if ((postalText.length() == 5 || postalText.length() == 6) && postalText.matches("[0-9]+") ) return  true;
        else {
            setAllValidFalse();
            return false;
        }
    }

    private boolean isValidMobileNumber(String mobileText) {
        if (mobileText.length() == 8 && mobileText.matches("[0-9]+") ) return  true;
        else {
            setAllValidFalse();
            return false;
        }
    }

    public boolean isNotNullCheck (String text) {
        if (text != null && text.length() > 0 ) return true;
        else {
            setAllValidFalse();
            return false;
        }
    }

    private boolean isNotNullOptionalFieldCheck(String text) {
        if (text != null && text.length() > 0 ) return true;
        else return false;
    }


    public boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else {
            setAllValidFalse();
            return false;
        }

    }




    @Override
    protected void onStop() {
        String status;
        if (prefs.getBoolean("ALLVALID", false)) status = "All Valid";
        else status = "Not Valid";
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
        //validateAll();
        super.onStop();    //To change body of overridden methods use File | Settings | File Templates.
    }



    private void setValid(ImageView imageView, boolean valid) {
        if (valid) { imageView.setImageResource(R.drawable.tick_pref);}
        else { imageView.setImageResource(R.drawable.cross); }
    }

    @Override
    public void afterTextChanged(Editable s) {
        validateAll();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


}