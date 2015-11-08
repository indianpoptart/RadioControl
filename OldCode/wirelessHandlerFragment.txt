/*//Initialize the process

        //Root commands for airplane mode
        String[] airplaneCmd = {"su", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true","svc wifi enable"};
        //Root commands if there is an active bluetooth connection
        String[] bluetoothCmd = {"su", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true","svc wifi enable","service call bluetooth_manager 6"};
        //Root commands if there is a wifi connection. NOT USED
        String[] wifiCmd = {"su", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true"};
        Context context = this;
        //Initialize network settings
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        // Check if the device is connected to the internet
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        //booleans for radios

        //Check for bluetooth
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Check for airplane mode
        boolean isEnabled = Settings.System.getInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;

        if (isConnected && !isEnabled) {
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI; //Boolean to check for an active WiFi connection
            //Check for wifi
            if(isWiFi) {
                //If the bluetooth connection is on
                if(bluetoothAdapter.isEnabled() || bluetoothAdapter.isDiscovering()) {
                    rootAccess(bluetoothCmd);
                }
                //If bluetooth is off, run the standard root request
                else if(!bluetoothAdapter.isEnabled()){
                    rootAccess(airplaneCmd);
                }
            }
        //Check if airplane mode is on, and wifi is off
        //This means user is in a no WiFi and no cell range
        } else if(isConnected == false && isEnabled == true){
            Log.d("Radio","I didn't have to do anything, yay");
        }

        else {
            Log.d("Else","something is different");
        }*/