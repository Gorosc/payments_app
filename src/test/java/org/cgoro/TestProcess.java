package org.cgoro;

import java.io.IOException;

public class TestProcess {
    private Process process;


    private static TestProcess instance = new TestProcess();

    public static TestProcess getInstance() {
        return instance;
    }

    public synchronized void startIfNotRunning() throws Exception {
        // check if it's not running and if not start
        try {
            instance.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void stop() throws Exception {
        process.destroy();
    }

    private synchronized void start() throws Exception {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                  MainApp.main();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        thread.start();
        Thread.sleep(5000);
    }
}
