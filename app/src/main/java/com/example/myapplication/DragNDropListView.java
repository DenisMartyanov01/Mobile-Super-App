package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;

public class DragNDropListView extends ListView {

    private boolean dragEnabled = false;
    private boolean isDragging = false;
    private int dragStartPosition;
    private int dragPosition;
    private float dragPointY;
    private float dragOffsetY;
    private int touchSlop;

    private WindowManager windowManager;
    private ImageView dragImageView;
    private Bitmap dragBitmap;
    private View dragView;

    private DropListener dropListener;
    private DragListener dragListener;

    public interface DropListener {
        void onDrop(int from, int to);
    }

    public interface DragListener {
        void onDragStarted(int from);
    }

    public DragNDropListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void setDragEnabled(boolean enabled) {
        this.dragEnabled = enabled;
    }

    public boolean isDragEnabled() {
        return dragEnabled;
    }

    public void setDropListener(DropListener listener) {
        this.dropListener = listener;
    }

    public void setDragListener(DragListener listener) {
        this.dragListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!dragEnabled) {
            return super.onTouchEvent(ev);
        }

        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                dragStartPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
                if (dragStartPosition != INVALID_POSITION) {
                    dragPosition = dragStartPosition;
                    dragPointY = ev.getY();
                    dragOffsetY = ev.getRawY() - ev.getY();
                    isDragging = false;
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (dragStartPosition != INVALID_POSITION && !isDragging) {
                    float deltaY = ev.getY() - dragPointY;
                    if (Math.abs(deltaY) > touchSlop) {
                        startDragging(dragStartPosition, ev);
                        isDragging = true;
                        return true;
                    }
                }

                if (isDragging && dragImageView != null) {
                    int y = (int) (ev.getRawY() - (dragView.getHeight() / 2));
                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) dragImageView.getLayoutParams();
                    params.y = y;
                    windowManager.updateViewLayout(dragImageView, params);

                    int newPosition = pointToPosition((int) ev.getX(), (int) ev.getY());

                    if (newPosition != INVALID_POSITION && newPosition != dragPosition) {
                        if (dragPosition < newPosition) {
                            for (int i = dragPosition; i < newPosition; i++) {
                                swapItems(i, i + 1);
                            }
                        } else {
                            for (int i = dragPosition; i > newPosition; i--) {
                                swapItems(i, i - 1);
                            }
                        }

                        dragPosition = newPosition;

                        ensureVisible(dragPosition);
                    }

                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    stopDragging();
                    if (dropListener != null && dragStartPosition != INVALID_POSITION &&
                            dragPosition != INVALID_POSITION && dragStartPosition != dragPosition) {
                        dropListener.onDrop(dragStartPosition, dragPosition);
                    }
                    dragStartPosition = INVALID_POSITION;
                    isDragging = false;
                    return true;
                }
                break;
        }

        return super.onTouchEvent(ev);
    }

    private void swapItems(int from, int to) {
        NoteAdapter adapter = (NoteAdapter) getAdapter();
        if (adapter != null) {
            adapter.swapItems(from, to);
        }
    }

    private void ensureVisible(int position) {
        int firstVisible = getFirstVisiblePosition();
        int lastVisible = getLastVisiblePosition();

        if (position < firstVisible) {
            setSelection(position);
        } else if (position > lastVisible) {
            setSelection(position);
        }
    }

    private void startDragging(int position, MotionEvent ev) {
        dragView = getChildAt(position - getFirstVisiblePosition());

        if (dragView != null) {
            dragView.setAlpha(0.5f);

            dragView.setDrawingCacheEnabled(true);
            dragBitmap = Bitmap.createBitmap(dragView.getDrawingCache());
            dragView.setDrawingCacheEnabled(false);

            dragImageView = new ImageView(getContext());
            dragImageView.setImageBitmap(dragBitmap);

            int[] location = new int[2];
            dragView.getLocationOnScreen(location);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = location[0];
            params.y = (int) (ev.getRawY() - (dragView.getHeight() / 2));
            params.width = dragView.getWidth();
            params.height = dragView.getHeight();
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            params.format = PixelFormat.TRANSLUCENT;
            params.windowAnimations = 0;

            windowManager.addView(dragImageView, params);

            if (dragListener != null) {
                dragListener.onDragStarted(position);
            }
        }
    }

    private void stopDragging() {
        if (dragImageView != null) {
            windowManager.removeView(dragImageView);
            dragImageView = null;
        }

        if (dragView != null) {
            dragView.setAlpha(1.0f);
            dragView = null;
        }

        if (dragBitmap != null && !dragBitmap.isRecycled()) {
            dragBitmap.recycle();
            dragBitmap = null;
        }
    }
}