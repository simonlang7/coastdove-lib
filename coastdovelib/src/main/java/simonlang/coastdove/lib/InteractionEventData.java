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

/**
 * Data gathered when a TYPE_VIEW_CLICKED, TYPE_VIEW_LONG_CLICKED, or TYPE_VIEW_SCROLLED event occurs
 */
public class InteractionEventData implements Parcelable {
    /** Resource ID of the element interacted with */
    private String androidID;
    /** Text of the element interacted with */
    private String text;
    /** Description of the element interacted with */
    private String description;
    /** Class name of the element interacted with */
    private String className;

    /**
     * Constructs an InteractionEventData object
     */
    public InteractionEventData(String androidID, String text, String description, String className) {
        this.androidID = androidID;
        this.text = text;
        this.description = description;
        this.className = className;
    }

    /**
     * Constructs an InteractionEventData object from a parcel
     */
    protected InteractionEventData(Parcel in) {
        androidID = in.readString();
        text = in.readString();
        description = in.readString();
        className = in.readString();
    }

    public static final Creator<InteractionEventData> CREATOR = new Creator<InteractionEventData>() {
        @Override
        public InteractionEventData createFromParcel(Parcel in) {
            return new InteractionEventData(in);
        }

        @Override
        public InteractionEventData[] newArray(int size) {
            return new InteractionEventData[size];
        }
    };

    @Override
    public String toString() {
        String idString = (androidID == null || androidID.equals("")) ? "" : "ID: " + androidID;
        String textSep = idString.equals("") ? "" : ", ";
        String textString = (text == null || text.equals("")) ? "" : textSep + "Text: " + text;
        String descSep = (idString.equals("") && textString.equals("")) ? "" : ", ";
        String descString = (description == null || description.equals("")) ? "" : descSep + "Description: " + description;
        String classSep = (idString.equals("") && textString.equals("") && descString.equals("")) ? "" : ", ";
        String classString = (className == null || className.equals("")) ? "" : classSep + "Class: " + className;

        return "(" + idString + textString + descString + classString + ")";
    }

    public boolean equals(InteractionEventData other) {
        return this.androidID.equals(other.androidID) &&
                this.text.equals(other.text) &&
                this.className.equals(other.className);
    }

    /** Resource ID of the element interacted with */
    public String getAndroidID() {
        return androidID;
    }

    /** Text of the element interacted with */
    public String getText() {
        return text;
    }
    ;
    /** Description of the element interacted with */
    public String getDescription() {
        return description;
    }

    /** Class name of the element interacted with */
    public String getClassName() {
        return className;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(androidID);
        parcel.writeString(text);
        parcel.writeString(description);
        parcel.writeString(className);
    }
}
