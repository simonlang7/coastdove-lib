/*  Coast Dove
    Copyright (C) 2016  Simon Lang
    Contact: simon.lang7 at gmail dot com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package simonlang.coastdove.lib;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

/**
 * Listener service to be bound by the Coast Dove core app
 */
public abstract class CoastDoveListenerService extends Service {
    // Sent from Coast Dove Core -> Coast Dove Listener
    public static final int MSG_REPLY_TO = 1;
    public static final int MSG_APP_ENABLED = 2;
    public static final int MSG_APP_DISABLED = 4;
    public static final int MSG_META_INFORMATION = 8;
    public static final int MSG_APP_CLOSED = 16;
    public static final int MSG_APP_OPENED = 32;
    public static final int MSG_ACTIVITY_DETECTED = 64;
    public static final int MSG_LAYOUTS_DETECTED = 128;
    public static final int MSG_INTERACTION_DETECTED = 256;
    public static final int MSG_NOTIFICATION_DETECTED = 512;
    public static final int MSG_SCREEN_STATE_DETECTED = 1024;
    public static final int MSG_VIEW_TREE = 2048;
    public static final int MSG_ACTION_RESULT = 4096;
    public static final int MSG_SCROLL_POSITION_DETECTED = 8192;

    // Sent from Coast Dove Listener -> Coast Dove Core
    public static final int REPLY_REQUEST_META_INFORMATION = 1;
    public static final int REPLY_REQUEST_VIEW_TREE = 2;
    public static final int REPLY_REQUEST_VIEW_TREE_NODE = 4;
    public static final int REPLY_REQUEST_ACTION = 8;

    public static final String DATA_APP_PACKAGE_NAME = "appPackageName";
    public static final String DATA_META_INFORMATION = "appMetaInformation";
    public static final String DATA_ACTIVITY = "activity";
    public static final String DATA_LAYOUTS = "layouts";
    public static final String DATA_INTERACTION = "interaction";
    public static final String DATA_EVENT_TYPE = "eventType";
    public static final String DATA_NOTIFICATION = "notification";
    public static final String DATA_SCREEN_OFF = "screenOff";
    public static final String DATA_VIEW_TREE = "viewTree";
    public static final String DATA_VIEW_TREE_NODE = "viewTreeNode";
    public static final String DATA_VIEW_TREE_START_NODE_RESOURCE = "viewTreeStartNodeResource";
    public static final String DATA_RESOURCE_ID = "resourceID";
    public static final String DATA_ACTION = "action";
    public static final String DATA_ACTION_ARGUMENTS = "actionArguments";
    public static final String DATA_SCROLL_POSITION = "scrollPosition";

    /**
     * Handler for incoming messages from Coast Dove core
     */
    private final class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            data.setClassLoader(CoastDoveListenerService.this.getClass().getClassLoader());
            if ((msg.what & MSG_REPLY_TO) != 0) {
                mReplyMessenger = msg.replyTo;
            }
            if ((msg.what & MSG_APP_ENABLED) != 0) {
                String appPackageName = data.getString(DATA_APP_PACKAGE_NAME);
                appEnabled(appPackageName);
            }
            if ((msg.what & MSG_APP_DISABLED) != 0) {
                String appPackageName = data.getString(DATA_APP_PACKAGE_NAME);
                appDisabled(appPackageName);
            }
            if ((msg.what & MSG_META_INFORMATION) != 0) {
                String appPackageName = data.getString(DATA_APP_PACKAGE_NAME);
                Parcelable appMetaInformation = data.getParcelable(DATA_META_INFORMATION);
                if (appMetaInformation instanceof AppMetaInformation)
                    onMetaInformationDelivered(appPackageName, (AppMetaInformation)appMetaInformation);
            }
            if ((msg.what & MSG_APP_CLOSED) != 0) {
                appClosed();
            }
            if ((msg.what & MSG_APP_OPENED) != 0) {
                String appPackageName = data.getString(DATA_APP_PACKAGE_NAME);
                appOpened(appPackageName);
            }
            if ((msg.what & MSG_ACTIVITY_DETECTED) != 0) {
                String activity = data.getString(DATA_ACTIVITY);
                activityDetected(activity);
            }
            if ((msg.what & MSG_LAYOUTS_DETECTED) != 0) {
                String[] layoutsArray = data.getStringArray(DATA_LAYOUTS);
                TreeSet<String> layouts = new TreeSet<>(new CollatorWrapper());
                for (String layout : layoutsArray)
                    layouts.add(layout);
                layoutsDetected(layouts);
            }
            if ((msg.what & MSG_INTERACTION_DETECTED) != 0) {
                Parcelable[] interactionArray = data.getParcelableArray(DATA_INTERACTION);
                String eventTypeString = data.getString(DATA_EVENT_TYPE);
                EventType eventType = EventType.valueOf(eventTypeString);
                if (interactionArray == null)
                    Log.e("Listener", "Interaction data is null");
                else {
                    Collection<InteractionEventData> interaction = new LinkedList<>();
                    for (Parcelable eventData : interactionArray)
                        interaction.add((InteractionEventData) eventData);
                    interactionDetected(interaction, eventType);
                }
            }
            if ((msg.what & MSG_NOTIFICATION_DETECTED) != 0) {
                String notification = data.getString(DATA_NOTIFICATION);
                notificationDetected(notification);
            }
            if ((msg.what & MSG_SCREEN_STATE_DETECTED) != 0) {
                boolean screenOff = data.getBoolean(DATA_SCREEN_OFF);
                screenStateDetected(screenOff);
            }
            if ((msg.what & MSG_VIEW_TREE) != 0) {
                ViewTreeNode viewTree = data.getParcelable(DATA_VIEW_TREE);
                viewTreeReceived(viewTree);
            }
            if ((msg.what & MSG_ACTION_RESULT) != 0) {
                if (Build.VERSION.SDK_INT >= 21) {
                    int actionID = data.getInt(DATA_ACTION);
                    ViewTreeNode node = null;
                    if (data.containsKey(DATA_VIEW_TREE_NODE))
                        node = data.getParcelable(DATA_VIEW_TREE_NODE);
                    AccessibilityNodeInfo.AccessibilityAction action = new AccessibilityNodeInfo.AccessibilityAction(actionID, null);
                    if (msg.arg1 != 0)
                        actionSuccessful(node, action);
                    else
                        actionFailed(node, action);
                }
            }
            if ((msg.what & MSG_SCROLL_POSITION_DETECTED) != 0) {
                ScrollPosition scrollPosition = data.getParcelable(DATA_SCROLL_POSITION);
                scrollPositionDetected(scrollPosition);
            }
        }
    }

    /** Receives messages from the Coast Dove core app */
    private transient final Messenger mMessenger = new Messenger(new IncomingHandler());
    /** Sends messages back to the Coast Dove core app */
    private transient Messenger mReplyMessenger = null;


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
    /** Apps for which this module is enabled */
    private transient volatile Set<String> enabledApps;
    /** Last viewtree received, if ever requested using requestViewTree */
    private transient volatile ViewTreeNode lastViewTree;
    /** Last scroll position detected */
    private transient volatile ScrollPosition lastScrollPosition;

    /**
     * Binds the service and initializes all members (except lastViewTree, which is set to null)
     * to empty or default values (empty sets, "", false) to make sure they don't need to be checked
     * for null. Do not call this method, this is done by Android using bindService.
     */
    @Nullable
    @Override
    public final IBinder onBind(Intent intent) {
        this.lastActivity = "";
        this.lastLayouts = new TreeSet<>(new CollatorWrapper());
        this.lastInteraction = new LinkedList<>();
        this.lastNotification = "";
        this.screenOff = false;
        this.enabledApps = new TreeSet<>(new CollatorWrapper());
        this.lastViewTree = null;
        this.lastScrollPosition = null;

        onServiceBound();
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mReplyMessenger = null;

        // Since the core is already disconnected, it cannot send MSG_APP_DISABLED
        // messages. We disable all apps here to make sure the user-implemented
        // callback method is called.
        Collection<String> enabledAppsCopy = new LinkedList<>(this.enabledApps);
        for (String app : enabledAppsCopy)
            appDisabled(app);

        onServiceUnbound();
        return super.onUnbind(intent);
    }

    /** Internal wrapper for onAppEnabled */
    private void appEnabled(String appPackageName) {
        this.enabledApps.add(appPackageName);
        onAppEnabled(appPackageName);
    }

    /** Internal wrapper for onAppDisabled */
    private void appDisabled(String appPackageName) {
        this.enabledApps.remove(appPackageName);
        onAppDisabled(appPackageName);
    }

    /** Internal wrapper for onAppOpened */
    private void appOpened(String appPackageName) {
        this.lastAppPackageName = appPackageName;
        onAppOpened();
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
    private void interactionDetected(Collection<InteractionEventData> interaction, EventType eventType) {
        this.lastInteraction = new LinkedList<>(interaction);
        onInteractionDetected(lastInteraction, eventType);
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

    /** Internal wrapper for onViewTreeReceived */
    private void viewTreeReceived(ViewTreeNode viewTree) {
        this.lastViewTree = viewTree;
        onViewTreeReceived(viewTree);
    }

    /** Internal wrapper for onActionSuccessful */
    private void actionSuccessful(ViewTreeNode node, AccessibilityNodeInfo.AccessibilityAction action) {
        onActionSuccessful(node, action);
    }

    /** Internal wrapper for onActionFailed */
    private void actionFailed(ViewTreeNode node, AccessibilityNodeInfo.AccessibilityAction action) {
        onActionFailed(node, action);
    }

    /** Internal wrapper for onScrollPositionDetected */
    private void scrollPositionDetected(ScrollPosition scrollPosition) {
        this.lastScrollPosition = scrollPosition;
        onScrollPositionDetected(scrollPosition);
    }

    /**
     * Requests AppMetaInformation from Coast Dove core. Will be delivered using
     * onMetaInformationDelivered
     * @param appPackageName    App to request meta information for
     */
    public final void requestMetaInformation(String appPackageName) {
        Bundle data = new Bundle();
        data.putString(DATA_APP_PACKAGE_NAME, appPackageName);
        int type = REPLY_REQUEST_META_INFORMATION;

        Message msg = Message.obtain(null, type, 0, 0);
        msg.setData(data);
        try {
            mReplyMessenger.send(msg);
        } catch (RemoteException e) {
            Log.e("Listener", "Unable to send reply (requestMetaInformation): " + e.getMessage());
        }
    }

    /**
     * Requests a view tree from Coast Dove core, which is a copy of the original
     * AccessibilityNodeInfo tree.
     * @param startNodeResource    If this is null, the root of the view tree will be requested.
     *                             Otherwise, the core will look for a NodeInfo whose
     *                             viewIdResourceName ends with this parameter's string, and if
     *                             found, delivers the subtree with that element as its root.
     *                             If not found, nothing is delivered.
     * @param includeSubTree       If true, the entire subtree is included; if false,
     *                             only the node itself is delivered.
     */
    public final void requestViewTree(String startNodeResource, boolean includeSubTree) {
        Bundle data = new Bundle();
        int type;
        type = includeSubTree ? REPLY_REQUEST_VIEW_TREE : REPLY_REQUEST_VIEW_TREE_NODE;

        if (startNodeResource != null)
            data.putString(DATA_VIEW_TREE_START_NODE_RESOURCE, startNodeResource);

        Message msg = Message.obtain(null, type, 0, 0);
        msg.setData(data);
        try {
            mReplyMessenger.send(msg);
        } catch (RemoteException e) {
            Log.e("Listener", "Unable to send reply (requestViewTree): " + e.getMessage());
        }
    }

    /**
     * Requests the Coast Dove core to perform an action on a node (an element).
     * This only works on SDK versions >= 21
     * @param resourceID    Android ID, or ViewIDResourceName, of the element to
     *                      perform the action on
     * @param action        Action to perform
     * @param arguments     Arguments for the action (can be null)
     * @return True if the action was requested, false if not (only works on SDK level >= 21)
     */
    public final boolean requestAction(String resourceID,
            AccessibilityNodeInfo.AccessibilityAction action,
            Bundle arguments) {
        if (Build.VERSION.SDK_INT < 21)
            return false;

        Bundle data = new Bundle();
        data.putInt(DATA_ACTION, action.getId());
        data.putString(DATA_RESOURCE_ID, resourceID);
        if (arguments != null)
            data.putBundle(DATA_ACTION_ARGUMENTS, arguments);
        int type = REPLY_REQUEST_ACTION;

        Message msg = Message.obtain(null, type, 0, 0);
        msg.setData(data);
        try {
            mReplyMessenger.send(msg);
            return true;
        } catch (RemoteException e) {
            Log.e("Listener", "Unable to send reply (requestAction): " + e.getMessage());
        }
        return false;
    }

    /**
     * Requests the Coast Dove core to perform an action on a node (an element).
     * This only works on SDK versions >= 21
     * @param resourceID    Android ID, or ViewIDResourceName, of the element to
     *                      perform the action on
     * @param action        Action to perform
     * @return True if the action was requested, false if not (only works on SDK level >= 21)
     */
    public final boolean requestAction(String resourceID,
                                       AccessibilityNodeInfo.AccessibilityAction action) {
        return requestAction(resourceID, action, null);
    }

    /**
     * Requests the Coast Dove core to perform an action on a node (an element).
     * This only works on SDK versions >= 21
     * @param node          Node to perform the action on
     * @param action        Action to perform
     * @param arguments     Arguments for the action (can be null)
     * @return True if the action was requested, false if not (only works on SDK level >= 21)
     */
    public final boolean requestAction(ViewTreeNode node,
                                       AccessibilityNodeInfo.AccessibilityAction action,
                                       Bundle arguments) {
        if (Build.VERSION.SDK_INT < 21)
            return false;

        Bundle data = new Bundle();
        data.putInt(DATA_ACTION, action.getId());
        data.putParcelable(DATA_VIEW_TREE_NODE, node.getFlatNode());
        if (arguments != null)
            data.putBundle(DATA_ACTION_ARGUMENTS, arguments);
        int type = REPLY_REQUEST_ACTION;

        Message msg = Message.obtain(null, type, 0, 0);
        msg.setData(data);
        try {
            mReplyMessenger.send(msg);
            return true;
        } catch (RemoteException e) {
            Log.e("Listener", "Unable to send reply (requestAction): " + e.getMessage());
        }
        return false;
    }

    /**
     * Requests the Coast Dove core to perform an action on a node (an element).
     * This only works on SDK versions >= 21
     * @param node      Node to perform the action on
     * @param action    Action to perform
     * @return True if the action was requested, false if not (only works on SDK level >= 21)
     */
    public final boolean requestAction(ViewTreeNode node,
                                       AccessibilityNodeInfo.AccessibilityAction action) {
        return requestAction(node, action, null);
    }

    /**
     * Called by the library when the service is bound, can be used for initialization
     */
    protected void onServiceBound() { }

    /**
     * Called by the library when the service is unbound, can be used for cleaning up
     */
    protected void onServiceUnbound() { }

    /**
     * Called by the library when the core enables an app for this module
     * @param appPackageName    App enabled by the core
     */
    protected void onAppEnabled(String appPackageName) { }

    /**
     * Called by the library when the core disables an app for this module
     * @param appPackageName    App disabled by the core
     */
    protected void onAppDisabled(String appPackageName) { }

    /**
     * Called by the library when AppMetaInformation is delivered. Request it by calling
     * requestMetaInformation.
     * @param appPackageName     App to which the meta information belongs
     * @param metaInformation    Meta information delivered
     */
    protected void onMetaInformationDelivered(String appPackageName, AppMetaInformation metaInformation) { }

    /**
     * Called by the library when any associated app is put in the foreground
     * (i.e., when any activity of an associated app is shown when another app
     * was in foreground before)
     */
    protected void onAppOpened() { }

    /**
     * Called by the library when any associated app is put in the background
     * (i.e., no activity of an associated app is shown anymore when before it
     * was)
     */
    protected void onAppClosed() { }

    /**
     * Called by the library whenever a new activity has been detected
     * @param activity    The activity detected
     */
    protected void onActivityDetected(String activity) { }

    /**
     * Called by the library whenever a new set of layouts has been detected
     * @param layouts    Layouts detected
     */
    protected void onLayoutsDetected(Set<String> layouts) { }

    /**
     * Called by the library whenever a new interaction has been detected
     * @param interaction    Interaction detected
     * @param eventType      Type of event
     */
    protected void onInteractionDetected(Collection<InteractionEventData> interaction, EventType eventType) { }

    /**
     * Called by the library whenever a new notification has been detected
     * @param notification    Notification detected
     */
    protected void onNotificationDetected(String notification) { }

    /**
     * Called by the library whenever the screen state has changed (turned off or on)
     * @param screenOff    Whether the screen has been turned off or on (true if off)
     */
    protected void onScreenStateDetected(boolean screenOff) { }

    /**
     * Called by the library when a view tree has been received. Use requestViewTree
     * to request one.
     * @param viewTree    ViewTree received
     */
    protected void onViewTreeReceived(ViewTreeNode viewTree) { }

    /**
     * Called by the library when an action previously requested has been executed
     * successfully. Use requestAction to request an action to be executed.
     * @param node      (Flat) node on which the action was executed
     * @param action    Action executed successfully
     */
    protected void onActionSuccessful(ViewTreeNode node, AccessibilityNodeInfo.AccessibilityAction action) { }

    /**
     * Called by the library when an action previously requested could not be
     * executed.
     * @param node      (Flat) node on which the action was supposed to be executed
     * @param action    Action that failed
     */
    protected void onActionFailed(ViewTreeNode node, AccessibilityNodeInfo.AccessibilityAction action) { }

    /**
     * Called by the library when a scroll position has been detected
     * @param scrollPosition    Scroll position detected
     */
    protected void onScrollPositionDetected(ScrollPosition scrollPosition) { }



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

    /** Last viewtree received, if ever requested using requestViewTree */
    public final ViewTreeNode getLastViewTree() {
        return lastViewTree;
    }

    /** Last scroll position detected */
    public ScrollPosition getLastScrollPosition() {
        return lastScrollPosition;
    }
}
