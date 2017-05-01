@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mRunning) {
            mRunning = true;
            Log.d("RadioControl","CellService Toggled");
            Utilities util = new Utilities();
            Context context = getApplicationContext();
            String[] cellOffCmd = {"service call phone 27","service call phone 14 s16"};

            RootAccess.runCommands(cellOffCmd);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(getApplicationContext(), CellRadioService.class);
                    stopService(i);
                    Log.d("RadioControl","CellService killed");
                }
            }, 0);
        }
        return super.onStartCommand(intent, flags, startId);
    }
