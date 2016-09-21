package simonlang.coastdove.lib;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Container for data from an AccessibilityNodeInfo object
 */
public class ViewTreeNode implements Parcelable {
    /**
     * Creates a ViewTreeNode that represents an entire subtree of AccessibilityNodeInfos
     * @param rootNodeInfo    AccessibilityNodeInfo to start from, i.e., the root of the subtree
     * @return Copied representation of an AccessibilityNodeInfo subtree
     */
    public static ViewTreeNode fromAccessibilityNodeInfo(AccessibilityNodeInfo rootNodeInfo) {
        Stack<AccessibilityNodeInfo> nodeInfos = new Stack<>();
        nodeInfos.push(rootNodeInfo);
        Stack<ViewTreeNode> viewTreeNodes = new Stack<>();

        // Traverse tree with depth-first search
        AccessibilityNodeInfo currentNodeInfo;
        ViewTreeNode currentViewTreeNode;
        while (!nodeInfos.empty()) {
            currentNodeInfo = nodeInfos.pop();

            // Have we just finished traversing all children?
            if (currentNodeInfo == null) {
                List<ViewTreeNode> children = new LinkedList<>();

                // Collect children
                currentViewTreeNode = viewTreeNodes.pop();
                while (currentViewTreeNode != null) { // until we hit the parent
                    children.add(currentViewTreeNode);
                    currentViewTreeNode = viewTreeNodes.pop();
                }

                // Add children to parent, link parent to children
                ViewTreeNode parent = viewTreeNodes.peek();
                for (ViewTreeNode child : children) {
                    child.parent = parent;
                    parent.children.add(child);
                }
            }
            else { // or did we just start processing a new node?
                int childCount = currentNodeInfo.getChildCount();

                // Process the current node (without children)
                currentViewTreeNode = flatCopy(currentNodeInfo);

                // Add node to stack
                viewTreeNodes.add(currentViewTreeNode);

                if (childCount > 0) {
                    // Signal that this node has children
                    nodeInfos.push(null);
                    viewTreeNodes.push(null);
                    for (int i = 0; i < childCount; ++i)
                        nodeInfos.push(currentNodeInfo.getChild(i));
                }
            }
        }

        // Once the nodeInfo stack is empty, there should be exactly one element
        // left on the ViewTreeNode stack.
        return viewTreeNodes.pop();
    }

    /**
     * Copies all required data from an AccessibilityNodeInfo into a newly
     * created ViewTreeNode, omitting parents and children
     * @param nodeInfo    AccessibilityNodeInfo to copy from
     */
    public static ViewTreeNode flatCopy(AccessibilityNodeInfo nodeInfo) {
        ViewTreeNode result = new ViewTreeNode();
        result.parent = null;
        result.children = new ArrayList<>(nodeInfo.getChildCount());

        result.contentDescription = charSeqToString(nodeInfo.getContentDescription());
        result.className = charSeqToString(nodeInfo.getClassName());
        result.inputType = nodeInfo.getInputType();
        result.textSelectionStart = nodeInfo.getTextSelectionStart();
        result.textSelectionEnd = nodeInfo.getTextSelectionEnd();
        result.text = charSeqToString(nodeInfo.getText());
        result.viewIDResourceName = nodeInfo.getViewIdResourceName();

        if (Build.VERSION.SDK_INT >= 21)
            result.actionList = new LinkedList<>(nodeInfo.getActionList());
        else
            result.actionList = new LinkedList<>();

        result.checkable = nodeInfo.isCheckable();
        result.checked = nodeInfo.isChecked();
        result.clickable = nodeInfo.isClickable();
        result.dismissable = nodeInfo.isDismissable();
        result.editable = nodeInfo.isEditable();
        result.enabled = nodeInfo.isEnabled();
        result.focusable = nodeInfo.isFocusable();
        result.focused = nodeInfo.isFocused();
        result.longClickable = nodeInfo.isLongClickable();
        result.multiLine = nodeInfo.isMultiLine();
        result.password = nodeInfo.isPassword();
        result.scrollable = nodeInfo.isScrollable();
        result.selected = nodeInfo.isSelected();
        result.visibleToUser = nodeInfo.isVisibleToUser();

        return result;
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

    private static String charSeqToString(CharSequence seq) {
        return seq == null ? null : seq.toString();
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
     * Creates an empty ViewTreeNode. Use flatCopy or fromAccessibilityNodeInfo
     * instead.
     */
    private ViewTreeNode() {
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
}
