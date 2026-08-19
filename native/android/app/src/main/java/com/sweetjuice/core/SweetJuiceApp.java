package com.sweetjuice.core;

import androidx.appcompat.app.AppCompatActivity;

/**
 * SweetJuiceApp is the core interface implemented by the main Application class.
 * It provides a bridge for plugins and framework components to access the active
 * activity and metadata about the application structure.
 */
public interface SweetJuiceApp {
    /**
     * Returns the currently active activity in the foreground.
     * 
     * @return the active {@link AppCompatActivity}, or null if the app is in the background.
     */
    AppCompatActivity getActiveActivity();

    /**
     * Returns the Class object of the primary main activity.
     * Used for dynamic component state management (e.g., hiding/showing launcher icons).
     * 
     * @return the main activity {@link Class}.
     */
    Class<?> getMainActivityClass();
}
