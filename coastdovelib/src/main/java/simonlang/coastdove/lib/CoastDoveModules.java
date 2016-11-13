/*  DetectAppScreen
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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.util.ArrayList;

/**
 * Used to register modules to Coast Dove core
 */
public abstract class CoastDoveModules {
    public static final String DATA_MODULE_NAME = "moduleName";
    public static final String DATA_SERVICE_PACKAGE_NAME = "servicePackageName";
    public static final String DATA_SERVICE_CLASS_NAME = "serviceClassName";
    public static final String DATA_ASSOCIATED_APPS = "associatedApps";

    private static final String REG_SERVICE_PACKAGE = "simonlang.coastdove.core";
    private static final String REG_SERVICE_CLASS = "simonlang.coastdove.core.ipc.ModuleRegisteringService";

    /**
     * Registers a Coast Dove module to the Coast Dove core service. Use this to first register your
     * service, and each time you change your package, your service class, or your associated apps
     * @param context           Context
     * @param serviceClass      .class of your service (which extends CoastDoveListenerService)
     * @param moduleName        Name of your module (usually the app's name)
     * @param associatedApps    All apps that your service can possibly listen to. Depending on your
     *                          module, this may be only one specific app, a selection of several apps,
     *                          or all apps. In case of the latter, just pass a collection with the
     *                          String "*" as its only element.
     */
    public static void registerModule(Context context, Class<?> serviceClass, String moduleName,
                                      ArrayList<String> associatedApps) {
        Context appContext = context.getApplicationContext();
        String servicePackageName = appContext.getPackageName();
        String serviceClassName = serviceClass.getName();

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(REG_SERVICE_PACKAGE, REG_SERVICE_CLASS));
        intent.putExtra(DATA_MODULE_NAME, moduleName);
        intent.putExtra(DATA_SERVICE_PACKAGE_NAME, servicePackageName);
        intent.putExtra(DATA_SERVICE_CLASS_NAME, serviceClassName);
        intent.putStringArrayListExtra(DATA_ASSOCIATED_APPS, associatedApps);
        appContext.startService(intent);
    }

    /**
     * Registers a Coast Dove module to the Coast Dove core service. Use this to first register your
     * service, and each time you change your package, your service class, or your associated apps
     * @param context           Context
     * @param serviceClass      .class of your service (which extends CoastDoveListenerService)
     * @param moduleName        Name of your module (usually the app's name)
     * @param associatedApps    All apps that your service can possibly listen to. Depending on your
     *                          module, this may be only one specific app, a selection of several apps,
     *                          or all apps. In case of the latter, just pass a collection with the
     *                          String "*" as its only element.
     */
    public static void registerModule(Context context, Class<?> serviceClass, String moduleName,
                                      String... associatedApps) {
        ArrayList<String> associatedAppsArrayList = new ArrayList<>(associatedApps.length);
        for (String app : associatedApps)
            associatedAppsArrayList.add(app);
        registerModule(context, serviceClass, moduleName, associatedAppsArrayList);
    }


    /**
     * Checks whether the app (to which the source activity belongs) has permissions to draw
     * overlays, and requests them if it doesn't
     * @param source         Activity from which the request is sent
     * @param requestCode    Request code (any number that is unique among concurrent requests)
     */
    @TargetApi(23)
    public void checkOverlayPermissions(Activity source, int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(source)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + source.getPackageName()));
                source.startActivityForResult(intent, requestCode);
            }
        }
    }
}

