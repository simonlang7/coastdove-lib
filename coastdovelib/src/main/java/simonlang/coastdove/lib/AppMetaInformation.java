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


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Contains meta information regarding a detectable app, such as entry activities
 */
public class AppMetaInformation implements Serializable, Parcelable {
    private static final long serialVersionUID = 6777291841329560324L;
    
    /** Package name of the app */
    private String appPackageName;
    /** Activities that are entry points to the app from a launcher */
    private Collection<String> mainActivities;

    /**
     * Creates AppMetaInformation with the given data
     */
    public AppMetaInformation(String appPackageName, Collection<String> mainActivities) {
        this.appPackageName = appPackageName;
        this.mainActivities = mainActivities;
    }

    protected AppMetaInformation(Parcel in) {
        appPackageName = in.readString();
        int size = in.readInt();
        mainActivities = new TreeSet<>(new CollatorWrapper());
        for (int i = 0; i < size; ++i)
            mainActivities.add(in.readString());
    }

    public static final Creator<AppMetaInformation> CREATOR = new Creator<AppMetaInformation>() {
        @Override
        public AppMetaInformation createFromParcel(Parcel in) {
            return new AppMetaInformation(in);
        }

        @Override
        public AppMetaInformation[] newArray(int size) {
            return new AppMetaInformation[size];
        }
    };

    /**
     * Tells whether the given activity is a possible entry point from a launcher
     * @param activity    Activity to check
     * @return True if the activity is a main activity
     */
    public boolean isMainActivity(String activity) {
        for (String mainActivity : this.mainActivities) {
            if (mainActivity.contains(activity))
                return true;
        }
        return false;
    }

    /** Package name of the app */
    public String getAppPackageName() {
        return appPackageName;
    }

    /** Activities that are entry points to the app from a launcher */
    public Collection<String> getMainActivities() {
        return mainActivities;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appPackageName);
        dest.writeInt(mainActivities.size());
        for (String activity : mainActivities)
            dest.writeString(activity);
    }
}
