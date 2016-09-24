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

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Container for data from an AccessibilityNodeInfo object
 */
public class ViewTreeNode implements Parcelable {
    public interface Filter {
        boolean filter(ViewTreeNode node);
    }

    /**
     * RangeInfo class, maps to the values of AccessibilityNodeInfo.RangeInfo
     */
    public static class RangeInfo implements Parcelable {
        /** integer range */
        public static final int RANGE_TYPE_INT = 0;
        /** float range */
        public static final int RANGE_TYPE_FLOAT = 1;
        /** percentage range, values from 0 to 1 */
        public static final int RANGE_TYPE_PERCENT = 2;

        private int mType;
        private float mMin;
        private float mMax;
        private float mCurrent;

        public RangeInfo(RangeInfo copyFrom) {
            mType = copyFrom.mType;
            mMin = copyFrom.mMin;
            mMax = copyFrom.mMax;
            mCurrent = copyFrom.mCurrent;
        }

        public RangeInfo(AccessibilityNodeInfo.RangeInfo copyFrom) {
            mType = copyFrom.getType();
            mMin = copyFrom.getMin();
            mMax = copyFrom.getMax();
            mCurrent = copyFrom.getCurrent();
        }

        protected RangeInfo(Parcel in) {
            mType = in.readInt();
            mMin = in.readFloat();
            mMax = in.readFloat();
            mCurrent = in.readFloat();
        }

        public static final Creator<RangeInfo> CREATOR = new Creator<RangeInfo>() {
            @Override
            public RangeInfo createFromParcel(Parcel in) {
                return new RangeInfo(in);
            }

            @Override
            public RangeInfo[] newArray(int size) {
                return new RangeInfo[size];
            }
        };

        public int getType() {
            return mType;
        }

        public float getMin() {
            return mMin;
        }

        public float getMax() {
            return mMax;
        }

        public float getCurrent() {
            return mCurrent;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mType);
            dest.writeFloat(mMin);
            dest.writeFloat(mMax);
            dest.writeFloat(mCurrent);
        }
    }

    /**
     * Creates an empty ViewTreeNode. This is supposed to be created from
     * Coast Dove core only. Modules shall only receive and read these
     * objects.
     */
    public ViewTreeNode() {
    }

    /**
     * Creates a ViewTreeNode from a parcel
     */
    protected ViewTreeNode(Parcel in) {
        children = in.createTypedArrayList(ViewTreeNode.CREATOR);
        contentDescription = in.readString();
        className = in.readString();
        inputType = in.readInt();
        textSelectionStart = in.readInt();
        textSelectionEnd = in.readInt();
        text = in.readString();
        viewIDResourceName = in.readString();
        boundsInScreen = in.readParcelable(Rect.class.getClassLoader());
        boundsInParent = in.readParcelable(Rect.class.getClassLoader());
        rangeInfo = in.readParcelable(RangeInfo.class.getClassLoader());
        checkable = in.readByte() != 0;
        checked = in.readByte() != 0;
        clickable = in.readByte() != 0;
        dismissable = in.readByte() != 0;
        editable = in.readByte() != 0;
        enabled = in.readByte() != 0;
        focusable = in.readByte() != 0;
        focused = in.readByte() != 0;
        longClickable = in.readByte() != 0;
        multiLine = in.readByte() != 0;
        password = in.readByte() != 0;
        scrollable = in.readByte() != 0;
        selected = in.readByte() != 0;
        visibleToUser = in.readByte() != 0;

        for (ViewTreeNode child : children)
            child.addParentReference(this);
    }

    public static final Creator<ViewTreeNode> CREATOR = new Creator<ViewTreeNode>() {
        @Override
        public ViewTreeNode createFromParcel(Parcel in) {
            return new ViewTreeNode(in);
        }

        @Override
        public ViewTreeNode[] newArray(int size) {
            return new ViewTreeNode[size];
        }
    };

    /**
     * Called internally when read from parcel, sets the reference to the parent.
     * We cannot save this directly, as it would result in an endless loop of
     * storing parents and children.
     * @param parent    Parent of this node
     */
    private void addParentReference(ViewTreeNode parent) {
        this.parent = parent;
    }

    private ViewTreeNode parent;
    private ArrayList<ViewTreeNode> children;

    private String contentDescription;
    private String className;
    private int inputType;
    private int textSelectionStart;
    private int textSelectionEnd;
    private String text;
    private String viewIDResourceName;

    private List<AccessibilityNodeInfo.AccessibilityAction> actionList;

    private Rect boundsInScreen;
    private Rect boundsInParent;

    private RangeInfo rangeInfo;

    private boolean checkable;
    private boolean checked;
    private boolean clickable;
    private boolean dismissable;
    private boolean editable;
    private boolean enabled;
    private boolean focusable;
    private boolean focused;
    private boolean longClickable;
    private boolean multiLine;
    private boolean password;
    private boolean scrollable;
    private boolean selected;
    private boolean visibleToUser;

    /**
     * Returns this node without any parent or children references
     */
    public ViewTreeNode getFlatNode() {
        ViewTreeNode result = new ViewTreeNode();
        result.parent = null;
        result.children = new ArrayList<>();
        result.contentDescription = contentDescription;
        result.className = className;
        result.inputType = inputType;
        result.textSelectionStart = textSelectionStart;
        result.textSelectionEnd = textSelectionEnd;
        result.text = text;
        result.viewIDResourceName = viewIDResourceName;

        result.actionList = new LinkedList<>();
        if (actionList != null)
            result.actionList.addAll(actionList);
        result.boundsInScreen = new Rect(boundsInScreen);
        result.boundsInParent = new Rect(boundsInParent);

        result.rangeInfo = rangeInfo == null ? null : new RangeInfo(rangeInfo);

        result.checkable = checkable;
        result.checked = checked;
        result.clickable = clickable;
        result.dismissable = dismissable;
        result.editable = editable;
        result.enabled = enabled;
        result.focusable = focusable;
        result.focused = focused;
        result.longClickable = longClickable;
        result.multiLine = multiLine;
        result.password = password;
        result.scrollable = scrollable;
        result.selected = selected;
        result.visibleToUser = visibleToUser;
        return result;
    }

    /**
     * Returns the next node that yields true when checked by the provided filter,
     * or null if no such node exists
     * @param nodes     Current queue of nodes
     * @param filter    Filter to apply to the nodes
     */
    private static ViewTreeNode findNext(Queue<ViewTreeNode> nodes, Filter filter) {
        while (!nodes.isEmpty()) {
            ViewTreeNode node = nodes.poll();
            for (ViewTreeNode child : node.children)
                nodes.add(child);
            if (filter.filter(node))
                return node;
        }
        return null;
    }

    /**
     * Indicates whether this tree has a node that passes the given filter
     * @param filter    Filter that the node must pass
     * @return True if such a node exists, false if not
     */
    public boolean hasNode(Filter filter) {
        Queue<ViewTreeNode> nodes = new LinkedList<>();
        nodes.add(this);
        return findNext(nodes, filter) != null;
    }

    /**
     * Finds the first node that passes the provided filter
     * @param filter    Filter that the node must pass
     * @return First node in the tree that passes the filter, or null if
     *         no such node exists
     */
    public ViewTreeNode findNode(Filter filter) {
        Queue<ViewTreeNode> nodes = new LinkedList<>();
        nodes.add(this);
        return findNext(nodes, filter);
    }

    /**
     * Finds all nodes that pass the provided filter
     * @param filter    Filter that the nodes must pass
     * @return All nodes in the tree that pass the filter (empty list if none)
     */
    public LinkedList<ViewTreeNode> findNodes(Filter filter) {
        Queue<ViewTreeNode> nodes = new LinkedList<>();
        nodes.add(this);
        LinkedList<ViewTreeNode> result = new LinkedList<>();
        ViewTreeNode next = findNext(nodes, filter);
        while (next != null) {
            result.add(next);
            next = findNext(nodes, filter);
        }
        return result;
    }

    public ViewTreeNode getParent() {
        return parent;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public List<ViewTreeNode> getChildren() {
        return children;
    }

    public ViewTreeNode getChild(int index) {
        return children.get(index);
    }

    public int getChildCount() {
        return children.size();
    }

    public boolean hasChildren() {
        return children.size() != 0;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public String getClassName() {
        return className;
    }

    public int getInputType() {
        return inputType;
    }

    public int getTextSelectionStart() {
        return textSelectionStart;
    }

    public int getTextSelectionEnd() {
        return textSelectionEnd;
    }

    public String getText() {
        return text;
    }

    /** Returns the text, or "" if the text is null */
    public String text() {
        return text != null ? text : "";
    }

    /** Also called Android ID (android:id) or Resource ID */
    public String getViewIDResourceName() {
        return viewIDResourceName;
    }

    /** Returns the resource ID, or "" if the resource ID is null */
    public String viewIDResourceName() {
        return viewIDResourceName != null ? viewIDResourceName : "";
    }

    public List<AccessibilityNodeInfo.AccessibilityAction> getActionList() {
        return actionList;
    }

    public void getBoundsInScreen(Rect outBounds) {
        outBounds.set(boundsInScreen);
    }

    public void getBoundsInParent(Rect outBounds) {
        outBounds.set(boundsInParent);
    }

    public RangeInfo getRangeInfo() {
        return rangeInfo;
    }

    public boolean isCheckable() {
        return checkable;
    }

    public boolean isChecked() {
        return checked;
    }

    public boolean isClickable() {
        return clickable;
    }

    public boolean isDismissable() {
        return dismissable;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isFocusable() {
        return focusable;
    }

    public boolean isFocused() {
        return focused;
    }

    public boolean isLongClickable() {
        return longClickable;
    }

    public boolean isMultiLine() {
        return multiLine;
    }

    public boolean isPassword() {
        return password;
    }

    public boolean isScrollable() {
        return scrollable;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isVisibleToUser() {
        return visibleToUser;
    }

    public void setParent(ViewTreeNode parent) {
        this.parent = parent;
    }

    public void setChildren(ArrayList<ViewTreeNode> children) {
        this.children = children;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
    }

    public void setTextSelectionStart(int textSelectionStart) {
        this.textSelectionStart = textSelectionStart;
    }

    public void setTextSelectionEnd(int textSelectionEnd) {
        this.textSelectionEnd = textSelectionEnd;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setViewIDResourceName(String viewIDResourceName) {
        this.viewIDResourceName = viewIDResourceName;
    }

    public void setActionList(List<AccessibilityNodeInfo.AccessibilityAction> actionList) {
        this.actionList = actionList;
    }

    public void setBoundsInScreen(Rect boundsInScreen) {
        this.boundsInScreen = boundsInScreen;
    }

    public void setBoundsInParent(Rect boundsInParent) {
        this.boundsInParent = boundsInParent;
    }

    public void setRangeInfo(RangeInfo rangeInfo) {
        this.rangeInfo = rangeInfo;
    }

    public void setCheckable(boolean checkable) {
        this.checkable = checkable;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public void setDismissable(boolean dismissable) {
        this.dismissable = dismissable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public void setLongClickable(boolean longClickable) {
        this.longClickable = longClickable;
    }

    public void setMultiLine(boolean multiLine) {
        this.multiLine = multiLine;
    }

    public void setPassword(boolean password) {
        this.password = password;
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setVisibleToUser(boolean visibleToUser) {
        this.visibleToUser = visibleToUser;
    }

    /**
     * Converts this tree to a string, showing each node's
     * viewIDResourceName, text, and class (only those not null).
     * Children are indented by 2 more spaces than their parent
     */
    @Override
    public String toString() {
        return toString(0);
    }

    /**
     * Converts this tree to a string
     * @param indent    By how many spaces to indent the string
     */
    public String toString(int indent) {
        String result = toStringFlat(indent);
        for (ViewTreeNode child : children)
            result += "\n\n" + child.toString(indent + 2);
        return result;
    }

    /**
     * Converts only this node (without parent or children) to
     * a string, showing its viewIDResourceName, text, and class
     * (only those not null).
     * @param indent    By how many spaces to indent the string
     */
    public String toStringFlat(int indent) {
        String resID = viewIDResourceName == null ? "" : "ID: " + viewIDResourceName;
        String txt = text == null ? "" : "Text: " + text;
        String clss = className == null ? "" : "Class: " + className;
        String resIDSep = resID.equals("") ? "" : ", ";
        String txtSep = txt.equals("") ? "" : ", ";

        String indentation = "";
        for (int i = 0; i < indent; ++i)
            indentation += " ";

        return indentation + "(" + resID + resIDSep + txt + txtSep + clss + ")";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(children);
        dest.writeString(contentDescription);
        dest.writeString(className);
        dest.writeInt(inputType);
        dest.writeInt(textSelectionStart);
        dest.writeInt(textSelectionEnd);
        dest.writeString(text);
        dest.writeString(viewIDResourceName);
        dest.writeParcelable(boundsInScreen, flags);
        dest.writeParcelable(boundsInParent, flags);
        dest.writeParcelable(rangeInfo, flags);
        dest.writeByte((byte) (checkable ? 1 : 0));
        dest.writeByte((byte) (checked ? 1 : 0));
        dest.writeByte((byte) (clickable ? 1 : 0));
        dest.writeByte((byte) (dismissable ? 1 : 0));
        dest.writeByte((byte) (editable ? 1 : 0));
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeByte((byte) (focusable ? 1 : 0));
        dest.writeByte((byte) (focused ? 1 : 0));
        dest.writeByte((byte) (longClickable ? 1 : 0));
        dest.writeByte((byte) (multiLine ? 1 : 0));
        dest.writeByte((byte) (password ? 1 : 0));
        dest.writeByte((byte) (scrollable ? 1 : 0));
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeByte((byte) (visibleToUser ? 1 : 0));
    }
}
