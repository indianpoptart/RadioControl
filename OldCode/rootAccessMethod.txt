public void rootAccess(String[] commands){
        Process p;
        try {
            p = Runtime.getRuntime().exec("su"); //Request SU
            DataOutputStream os = new DataOutputStream(p.getOutputStream()); //Used for terminal
            for (String tmpCmd : commands) {
                os.writeBytes(tmpCmd + "\n"); //Sends commands to the terminal
            }
            os.writeBytes("exit\n"); //Quits the terminal session
            os.flush(); //Ends datastream
        } catch (IOException e) {
            Log.d("Root", "There was an error with root");
        }
    }