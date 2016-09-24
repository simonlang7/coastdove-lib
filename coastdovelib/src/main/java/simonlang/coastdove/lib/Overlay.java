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

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

/**
 * This class allows simplified creation of overlays
 */
public class Overlay {
    /** App context */
    private Context mContext;
    /** Layout resource to inflate */
    private int mResource;
    /** The system's window manager */
    private WindowManager mWindowManager;
    /** Used to inflate the layout specified in mResource */
    private LayoutInflater mInflater;
    /** Parameters indicating how to display the layout */
    private WindowManager.LayoutParams mParams;
    /** Actual view of the layout, once inflated */
    private View mView;
    /** Indicates whether the layout is inflated or not */
    private boolean mInflated;

    /**
     * Creates an overlay with default parameters: wrap content, not focusable,
     * does not obstruct keyboard/statusbar, (0, 0) is the actual top-left corner of
     * the screen, translucent pixel format (set your own background if necessary),
     * gravity: top
     *
     * @param context     App context
     * @param resource    Layout resource to inflate
     */
    public Overlay(@NonNull Context context, int resource) {
        mContext = context;
        mResource = resource;
        mView = null;
        mInflated = false;

        mInflater = LayoutInflater.from(mContext);
        mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE, // place above other apps
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE // button events will go to app below
                        | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM // place behind status bar / keyboard
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, // ignore status bar when giving coordinates
                PixelFormat.TRANSLUCENT // see-through
        );
        mParams.gravity = Gravity.TOP;
    }

    /**
     * Creates an overlay
     * @param context     App context
     * @param resource    Layout resource to inflate
     * @param params      Custom layout parameters
     */
    public Overlay(@NonNull Context context, int resource, @NonNull WindowManager.LayoutParams params) {
        mContext = context;
        mResource = resource;
        mParams = params;
        mView = null;
        mInflated = false;

        mInflater = LayoutInflater.from(mContext);
        mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * Inflates the view and adds it to the window manager. Safe to call even if already inflated.
     */
    public void inflate() {
        if (mView == null)
            mView = mInflater.inflate(mResource, null, false);

        if (!mInflated)
            mWindowManager.addView(mView, mParams);
        mInflated = true;
    }

    /**
     * Removes the view from the window manager. Shall be called beforen destroying the overlay, and
     * possibly when exiting the associated app. Safe to call when already destroyed.
     */
    public void remove() {
        if (mView != null && mInflated)
            mWindowManager.removeView(mView);
        mInflated = false;
    }

    /**
     * Sets the layout's visibility to visible, inflating it beforehand if it wasn't
     */
    public void show() {
        if (!mInflated)
            inflate();
        if (mInflated)
            mView.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the layout's visibility to gone. Does nothing if the layout is not inflated.
     */
    public void hide() {
        if (mInflated)
            mView.setVisibility(View.GONE);
    }

    /**
     * Finds a view with the given id (including the overlay's main view)
     *
     * @param id    ID to look for
     * @return The view matching the given id, or null of not found or if the overlay is not inflated
     */
    public View findViewById(int id) {
        if (mView != null)
            return mView.findViewById(id);

        return null;
    }

    /**
     * Indicates whether the overlay is inflated
     * @return True if inflated
     */
    public boolean isInflated() {
        return mInflated;
    }

    /**
     * Sets the layout's position
     */
    public void setPosition(int x, int y) {
        mParams.x = x;
        mParams.y = y;
        updateView();
    }

    /**
     * Sets the layout's size
     */
    public void setSize(int width, int height) {
        mParams.width = width;
        mParams.height = height;
        updateView();
    }

    /**
     * Sets the layout's position and size
     */
    public void setBounds(int x, int y, int width, int height) {
        mParams.x = x;
        mParams.y = y;
        mParams.width = width;
        mParams.height = height;
        updateView();
    }

    /**
     * Sets the layout's bounds, i.e.,
     * x = left
     * y = top
     * width = right - left
     * height = bottom - top
     */
    public void setBounds(Rect bounds) {
        mParams.x = bounds.left;
        mParams.y = bounds.top;
        mParams.width = bounds.right - bounds.left;
        mParams.height = bounds.bottom - bounds.top;
        updateView();
    }

    /**
     * Sets custom layout parameters
     * @param params    Parameters to set - be sure to include everything necessary for overlays
     */
    public void setLayoutParams(WindowManager.LayoutParams params) {
        mParams = params;
        updateView();
    }

    /**
     * Updates the view (automatically called when layout parameters are changed)
     */
    protected void updateView() {
        if (mView != null && mInflated)
            mWindowManager.updateViewLayout(mView, mParams);
    }
}
