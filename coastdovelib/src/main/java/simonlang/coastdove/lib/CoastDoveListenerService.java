package simonlang.coastdove.lib;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

/**
 * Listener service to be bound by the Coast Dove core app
 */
public abstract class CoastDoveListenerService extends Service {
    public static final int MSG_APP_STARTED = 1;
    public static final int MSG_APP_CLOSED = 2;
    public static final int MSG_ACTIVITY_DETECTED = 4;
    public static final int MSG_LAYOUTS_DETECTED = 8;
    public static final int MSG_INTERACTION_DETECTED = 16;
    public static final int MSG_NOTIFICATION_DETECTED = 32;
    public static final int MSG_SCREEN_STATE_DETECTED = 64;

    /**
     * Handler for incoming messages from Coast Dove core
     */
    private final class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            data.setClassLoader(CoastDoveListenerService.this.getClass().getClassLoader());
            if ((msg.what & MSG_APP_CLOSED) != 0) {
                appClosed();
            }
            if ((msg.what & MSG_APP_STARTED) != 0) {
                String appPackageName = data.getString("appPackageName");
                appStarted(appPackageName);
            }
            if ((msg.what & MSG_ACTIVITY_DETECTED) != 0) {
                String activity = data.getString("activity");
                activityDetected(activity);
            }
            if ((msg.what & MSG_LAYOUTS_DETECTED) != 0) {
                String[] layoutsArray = data.getStringArray("layouts");
                TreeSet<String> layouts = new TreeSet<>(new CollatorWrapper());
                for (String layout : layoutsArray)
                    layouts.add(layout);
                layoutsDetected(layouts);
            }
            if ((msg.what & MSG_INTERACTION_DETECTED) != 0) {
                Parcelable[] interactionArray = data.getParcelableArray("interaction");
                if (interactionArray == null)
                    Log.e("Listener", "Interaction data is null");
                else {
                    Collection<InteractionEventData> interaction = new LinkedList<>();
                    for (Parcelable eventData : interactionArray)
                        interaction.add((InteractionEventData) eventData);
                    interactionDetected(interaction);
                }
            }
            if ((msg.what & MSG_NOTIFICATION_DETECTED) != 0) {
                String notification = data.getString("notification");
                notificationDetected(notification);
            }
            if ((msg.what & MSG_SCREEN_STATE_DETECTED) != 0) {
                boolean screenOff = data.getBoolean("screenOff");
                screenStateDetected(screenOff);
            }
        }
    }

    /** Messenger to communicate with the Coast Dove core app */
    private transient final Messenger mMessenger = new Messenger(new IncomingHandler());

    /** Last package name detected, or "" if none so far */
    private transient volatile String lastAppPackageName;
    /** Last activity detected, or "" if none so far */
    private transient volatile String lastActivity;
    /** Last layouts detected (empty set if none) */
    private transient volatile Set<String> lastLayouts;
    /** Last interaction detected (empty if none) */
    private transient volatile Collection<InteractionEventData> lastInteraction;
    /** Last notification detected, or "" if none so far */
    private transient volatile String lastNotification;
    /** Whether the screen is currently off, according to the last
     *  screen state detected (false by default) */
    private transient volatile boolean screenOff;

    /**
     * Binds the service and initializes all members to empty or default values (empty sets, "", false)
     * to make sure they don't need to be checked for null. Do not call this method, this is done by
     * Android using bindService.
     */
    @Nullable
    @Override
    public final IBinder onBind(Intent intent) {
        this.lastActivity = "";
        this.lastLayouts = new TreeSet<>(new CollatorWrapper());
        this.lastInteraction = new LinkedList<>();
        this.lastNotification = "";
        this.screenOff = false;

        return mMessenger.getBinder();
    }

    /** Internal wrapper for onAppStarted */
    private void appStarted(String appPackageName) {
        this.lastAppPackageName = appPackageName;
        onAppStarted();
    }

    /** Internal wrapper for onAppClosed */
    private void appClosed() {
        onAppClosed();
    }

    /** Internal wrapper for onActivityDetected */
    private void activityDetected(String activity) {
        this.lastActivity = activity;
        onActivityDetected(lastActivity);
    }

    /** Internal wrapper for onLayoutsDetected */
    private void layoutsDetected(Set<String> layouts) {
        this.lastLayouts = new TreeSet<>(new CollatorWrapper());
        this.lastLayouts.addAll(layouts);
        onLayoutsDetected(lastLayouts);
    }

    /** Internal wrapper for onInteractionDetected */
    private void interactionDetected(Collection<InteractionEventData> interaction) {
        this.lastInteraction = new LinkedList<>(interaction);
        onInteractionDetected(lastInteraction);
    }

    /** Internal wrapper for onNotificationDetected */
    private void notificationDetected(String notification) {
        this.lastNotification = notification;
        onNotificationDetected(notification);
    }

    /** Internal wrapper for onScreenStateDetected */
    private void screenStateDetected(boolean screenOff) {
        this.screenOff = screenOff;
        onScreenStateDetected(screenOff);
    }


    /**
     * Called by the library when any associated app is put in the foreground
     * (i.e., when any activity of an associated app is shown when another app
     * was in foreground before)
     */
    protected abstract void onAppStarted();

    /**
     * Called by the library when any associated app is put in the background
     * (i.e., no activity of an associated app is shown anymore when before it
     * was)
     */
    protected abstract void onAppClosed();

    /**
     * Called by the library whenever a new activity has been detected
     * @param activity    The activity detected
     */
    protected abstract void onActivityDetected(String activity);

    /**
     * Called by the library whenever a new set of layouts has been detected
     * @param layouts    Layouts detected
     */
    protected abstract void onLayoutsDetected(Set<String> layouts);

    /**
     * Called by the library whenever a new interaction has been detected
     * @param interaction    Interaction detected
     */
    protected abstract void onInteractionDetected(Collection<InteractionEventData> interaction);

    /**
     * Called by the library whenever a new notification has been detected
     * @param notification    Notification detected
     */
    protected abstract void onNotificationDetected(String notification);

    /**
     * Called by the library whenever the screen state has changed (turned off or on)
     * @param screenOff    Whether the screen has been turned off or on (true if off)
     */
    protected abstract void onScreenStateDetected(boolean screenOff);


    /** Last package name detected, or "" if none so far */
    public final String getLastAppPackageName() {
        return lastAppPackageName;
    }

    /** Last activity detected, or "" if none so far */
    public final String getLastActivity() {
        return lastActivity;
    }

    /** Last layouts detected (empty set if none) */
    public final Set<String> getLastLayouts() {
        return lastLayouts;
    }

    /** Last interaction detected (empty set if none) */
    public final Collection<InteractionEventData> getLastInteraction() {
        return lastInteraction;
    }

    /** Last notification detected, or "" if none so far */
    public final String getLastNotification() {
        return lastNotification;
    }

    /** Whether the screen is currently off, according to the last
     *  screen state detected (false by default) */
    public final boolean isScreenOff() {
        return screenOff;
    }
}
