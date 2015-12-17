package com.nikhilparanjape.radiocontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener{
    private static final String PRIVATE_PREF = "radiocontrol-prefs";
    private static final String VERSION_KEY = "version_number";
    Model[] modelItems;
    public static ArrayList<String> ssidList = new ArrayList<String>();
    String ssidlist[];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        //Save button for the network list
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                final EditText field = (EditText) findViewById(R.id.editText);

                // get value in field
                String value = field.getText().toString();
                if (value.length() != 0) {
                    ssidList.add(value);
                }

                // pair the value in text field with the key
                editor.putString("disabled_networks", ssidList.toString());
                field.setText("");
                Toast.makeText(MainActivity.this,
                        "SSID Saved", Toast.LENGTH_LONG).show();
                editor.commit();
            }

        });

        //Clear button for the network list
        Button btn2 = (Button) findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.remove("disabled_networks");
                Toast.makeText(MainActivity.this,
                        "Disabled SSID list cleared", Toast.LENGTH_LONG).show();
                editor.apply();
            }

        });


        //getWifiNetworks();
        Integer[] seconds_array = new Integer[]{
                0,1,2,3,4,5,6,7,8,9,10,
                11,12,13,14,15,16,17,18,19,20,
                21,22,23,24,25,26,27,28,29,30,
                31,32,33,34,35,36,37,38,39,40,
                41,42,43,44,45,46,47,48,49,50,
                51,52,53,54,55,56,57,58,59,60};
        //Initialize Timer Spinner
        Spinner spinner = (Spinner) findViewById(R.id.secSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter <Integer> adapter = new ArrayAdapter<Integer>( this,android.R.layout.simple_spinner_item, seconds_array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        SharedPreferences sp = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        long secondsValue = sp.getLong("seconds_spinner", 15);
        spinner.setSelection((int) secondsValue);
        spinner.setOnItemSelectedListener(this);



        //Creates navigation drawer header
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.mipmap.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(getDeviceName()).withEmail("v1.4")
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();
        //Creates navigation drawer items
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("Home").withIcon(GoogleMaterial.Icon.gmd_wifi);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withName("Settings").withIcon(GoogleMaterial.Icon.gmd_settings);
        SecondaryDrawerItem item3 = new SecondaryDrawerItem().withName("About").withIcon(GoogleMaterial.Icon.gmd_info);

        //Create navigation drawer
        Drawer result = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        item3
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        return false;
                    }
                })
                .build();


    }

    //Init for the Whats new dialog
    private void init() {
        SharedPreferences sharedPref    = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        int currentVersionNumber        = 0;

        int savedVersionNumber          = sharedPref.getInt(VERSION_KEY, 0);

        try {
            PackageInfo pi          = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionNumber    = pi.versionCode;
        } catch (Exception e) {}

        if (currentVersionNumber > savedVersionNumber) {
            showWhatsNewDialog();

            SharedPreferences.Editor editor   = sharedPref.edit();

            editor.putInt(VERSION_KEY, currentVersionNumber);
            editor.commit();
        }
    }
    //whats new dialog
    private void showWhatsNewDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);//Creates layout inflator for dialog
        View view = inflater.inflate(R.layout.dialog_whatsnew, null);//Initializes the view for whats new dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//creates alertdialog

        builder.setView(view).setTitle("                 Whats New - Alpha")//sets title
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    //Grab device make and model for drawer
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }
    private void getWifiNetworks() {
        //SharedPreferences sp = getSharedPreferences("NetworkList", Context.MODE_PRIVATE);
        final WifiManager wifiManager =
                (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
        if (networks == null) {
        }
        String[] ssids = new String[networks.size()];
        //final SharedPreferences.Editor editor = sp.edit();
        modelItems = new Model[networks.size()];
        for (int i=0 ; i<networks.size(); i++) {
            ssids[i] = networks.get(i).SSID;
            modelItems[i] = new Model(networks.get(i).SSID, 0);
            //editor.putString("wifinetwork"+i, networks.get(i).SSID);
        }
        //editor.commit();
    }
    //Capitalizes names for devices. Used by getDeviceName()
    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Log.d("Spinner","Value set to: " + id);
        SharedPreferences sp = getSharedPreferences(PRIVATE_PREF, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("seconds_spinner", id);
        editor.commit();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback

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
