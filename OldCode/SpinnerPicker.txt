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
