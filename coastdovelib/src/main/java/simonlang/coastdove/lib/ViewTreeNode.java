package simonlang.coastdove.lib;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for data from an AccessibilityNodeInfo object
 */
public class ViewTreeNode implements Parcelable {
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

    /**
     * Creates an empty ViewTreeNode. This is supposed to be created from
     * Coast Dove core only. Modules shall only receive and read these
     * objects.
     */
    public ViewTreeNode() {
    }

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

    /** Also called Android ID (android:id) or Resource ID */
    public String getViewIDResourceName() {
        return viewIDResourceName;
    }

    public List<AccessibilityNodeInfo.AccessibilityAction> getActionList() {
        return actionList;
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

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        String result = toStringFlat(indent);
        for (ViewTreeNode child : children)
            result += "\n\n" + child.toString(indent + 2);
        return result;
    }

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
}
