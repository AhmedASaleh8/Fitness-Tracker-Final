package com.example.fitnesstracker.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtils {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void runInBackground(Runnable task) {
        executorService.execute(task);
    }

    public static void runOnMainThread(Runnable task) {
        mainHandler.post(task);
    }


    public static void runInBackgroundThenUI(Runnable backgroundTask, Runnable uiTask) {
        executorService.execute(() -> {

            backgroundTask.run();

            mainHandler.post(uiTask);
        });
    }


     // إيقاف ExecutorService عند إغلاق التطبيق

    public static void shutdown() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}