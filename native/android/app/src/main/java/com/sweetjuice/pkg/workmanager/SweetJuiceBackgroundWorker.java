package com.sweetjuice.pkg.workmanager;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import juiceapp.Juiceapp;

/**
 * SweetJuiceBackgroundWorker is the bridge between Android WorkManager and Go.
 * It is invoked by the OS even when the UI is not active.
 */
public class SweetJuiceBackgroundWorker extends Worker {
    private static final String TAG = "SweetJuiceBackgroundWorker";

    public SweetJuiceBackgroundWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Extract the unique service/task key passed from Go
        String taskKey = getInputData().getString("task_key");
        if (taskKey == null) taskKey = "default_background_task";

        Log.d(TAG, "OS invoked background job for task: " + taskKey);

        // Ensure Go backend is initialized before attempting execution
        Juiceapp.startApplication();

        // Safely dispatch execution into the Go backend layer via the bridge payload
        String payload = "[{\"task_key\":\"" + taskKey + "\"}]";

        // This invokes Go directly to execute backend processing
        String goResult = Juiceapp.handleNativeAction("workmanager:execute", payload);
        Log.d(TAG, "Go execution response: " + goResult);

        // Allow Go to signal a retry if processing failed
        if (goResult != null && goResult.contains("retry")) {
            return Result.retry();
        }

        return Result.success();
    }
}
