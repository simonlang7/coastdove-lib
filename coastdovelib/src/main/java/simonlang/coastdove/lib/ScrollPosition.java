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
 * Scroll position in a list, holding the indices of the first and last items shown, and
 * the total item count
 */
public class ScrollPosition implements Parcelable {
    /** The first shown item's index */
    private int fromIndex;
    /** The last shown item's index */
    private int toIndex;
    /** The list's item count */
    private int itemCount;

    /** Constructs a ScrollPosition object */
    public ScrollPosition(int fromIndex, int toIndex, int itemCount) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.itemCount = itemCount;
    }

    /** Constructs a ScrollPosition from a parcel */
    protected ScrollPosition(Parcel in) {
        fromIndex = in.readInt();
        toIndex = in.readInt();
        itemCount = in.readInt();
    }

    public static final Creator<ScrollPosition> CREATOR = new Creator<ScrollPosition>() {
        @Override
        public ScrollPosition createFromParcel(Parcel in) {
            return new ScrollPosition(in);
        }

        @Override
        public ScrollPosition[] newArray(int size) {
            return new ScrollPosition[size];
        }
    };

    /** The first shown item's index */
    public int getFromIndex() {
        return fromIndex;
    }

    /** The last shown item's index */
    public int getToIndex() {
        return toIndex;
    }

    /** The list's item count */
    public int getItemCount() {
        return itemCount;
    }

    @Override
    public String toString() {
        return "Showing items from " + fromIndex + " to " + toIndex
                + " of " + itemCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(fromIndex);
        dest.writeInt(toIndex);
        dest.writeInt(itemCount);
    }
}
