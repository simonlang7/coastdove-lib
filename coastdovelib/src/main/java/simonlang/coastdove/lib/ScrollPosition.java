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
