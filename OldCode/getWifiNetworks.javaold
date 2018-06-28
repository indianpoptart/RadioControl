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