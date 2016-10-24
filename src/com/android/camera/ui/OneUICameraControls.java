/*
 * Copyright (c) 2016, The Linux Foundation. All rights reserved.
 * Not a Contribution.
 *
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.codeaurora.snapcam.R;

import com.android.camera.Storage;
import com.android.camera.imageprocessor.filter.BeautificationFilter;

public class OneUICameraControls extends RotatableLayout {

    private static final String TAG = "CAM_Controls";

    private View mShutter;
    private View mVideoShutter;
    private View mFlashButton;
    private View mMute;
    private View mFrontBackSwitcher;
    private View mTsMakeupSwitcher;
    private View mPreview;
    private View mSceneModeSwitcher;
    private View mFilterModeSwitcher;
    private View mMakeupSeekBar;
    private View mMakeupSeekBarLowText;
    private View mMakeupSeekBarHighText;
    private View mMakeupSeekBarLayout;

    private ArrowTextView mRefocusToast;

    private static final int WIDTH_GRID = 5;
    private static final int HEIGHT_GRID = 7;
    private View[] mViews;
    private boolean mHideRemainingPhoto = false;
    private LinearLayout mRemainingPhotos;
    private TextView mRemainingPhotosText;
    private int mCurrentRemaining = -1;
    private int mOrientation;

    private static int mTop = 0;
    private static int mBottom = 0;

    private Paint mPaint;

    private static final int LOW_REMAINING_PHOTOS = 20;
    private static final int HIGH_REMAINING_PHOTOS = 1000000;
    private int mWidth;
    private int mHeight;
    private boolean mVisible;

    public OneUICameraControls(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setWillNotDraw(false);

        mRefocusToast = new ArrowTextView(context);
        addView(mRefocusToast);
        setClipChildren(false);

        setMeasureAllChildren(true);
        mPaint.setColor(getResources().getColor(R.color.camera_control_bg_transparent));

        mTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());
        mBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        mVisible = true;
    }

    public OneUICameraControls(Context context) {
        this(context, null);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mShutter = findViewById(R.id.shutter_button);
        mVideoShutter = findViewById(R.id.video_button);
        mFrontBackSwitcher = findViewById(R.id.front_back_switcher);
        mTsMakeupSwitcher = findViewById(R.id.ts_makeup_switcher);
        mMakeupSeekBarLowText = findViewById(R.id.makeup_low_text);
        mMakeupSeekBarHighText = findViewById(R.id.makeup_high_text);
        mMakeupSeekBar = findViewById(R.id.makeup_seekbar);
        mMakeupSeekBarLayout = findViewById(R.id.makeup_seekbar_layout);
        ((SeekBar)mMakeupSeekBar).setMax(100);
        mFlashButton = findViewById(R.id.flash_button);
        mMute = findViewById(R.id.mute_button);
        mPreview = findViewById(R.id.preview_thumb);
        mSceneModeSwitcher = findViewById(R.id.scene_mode_switcher);
        mFilterModeSwitcher = findViewById(R.id.filter_mode_switcher);
        mRemainingPhotos = (LinearLayout) findViewById(R.id.remaining_photos);
        mRemainingPhotosText = (TextView) findViewById(R.id.remaining_photos_text);
        mViews = new View[]{
                mSceneModeSwitcher, mFilterModeSwitcher, mFrontBackSwitcher,
                mTsMakeupSwitcher, mFlashButton, mShutter, mPreview, mVideoShutter
        };

        if(!BeautificationFilter.isSupportedStatic()) {
            mTsMakeupSwitcher.setVisibility(View.GONE);
            mTsMakeupSwitcher = null;
        }
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        r = r - l;
        b = b - t;
        l = 0;
        t = 0;
        mWidth = r;
        mHeight = b;
        setLocation(r - l, b - t);
        layoutRemaingPhotos();
        if(mMakeupSeekBar != null) {
            mMakeupSeekBar.setMinimumWidth(mWidth/2);
        }
    }

    public boolean isControlRegion(int x, int y) {
        return y <= mTop || y >= (mHeight - mBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mVisible) {
            int rotation = getUnifiedRotation();
            int w = canvas.getWidth(), h = canvas.getHeight();
            switch (rotation) {
                case 90:
                    canvas.drawRect(0, 0, mTop, h, mPaint);
                    canvas.drawRect(w - mBottom, 0, w, h, mPaint);
                    break;
                case 180:
                    canvas.drawRect(0, 0, w, mBottom, mPaint);
                    canvas.drawRect(0, h - mTop, w, h, mPaint);
                    break;
                case 270:
                    canvas.drawRect(0, 0, mBottom, h, mPaint);
                    canvas.drawRect(w - mTop, 0, w, h, mPaint);
                    break;
                default:
                    canvas.drawRect(0, 0, w, mTop, mPaint);
                    canvas.drawRect(0, h - mBottom, w, h, mPaint);
                    break;
            }
        }
    }

    private void setLocation(View v, boolean top, float idx) {
        if(v == null) {
            return;
        }
        int w = v.getMeasuredWidth();
        int h = v.getMeasuredHeight();
        if (top) {
            v.setY((mTop - h) / 2);
        } else {
            v.setY(mHeight - mBottom + (mBottom - h) / 2);
        }
        int bW = mWidth / 5;

        v.setX(bW * idx + (bW - w) / 2);
    }

    private void setLocationCustomBottom(View v, float x, float y) {
        if(v == null) {
            return;
        }
        int w = v.getMeasuredWidth();
        int h = v.getMeasuredHeight();
        int bW = mWidth / 5;
        int bH = mHeight / 6;
        v.setY(mHeight - mBottom + (mBottom - h) / 2 - bH * y);
        v.setX(bW * x);
    }

    private void setLocation(int w, int h) {
        int rotation = getUnifiedRotation();
        setLocation(mSceneModeSwitcher, true, 0);
        setLocation(mFilterModeSwitcher, true, 1);
        setLocation(mFrontBackSwitcher, true, 2);
        setLocation(mTsMakeupSwitcher, true, 3);
        setLocation(mFlashButton, true, 4);
        setLocation(mPreview, false, 0);
        setLocation(mShutter, false, 2);
        setLocation(mVideoShutter, false, 3.15f);
        setLocationCustomBottom(mMakeupSeekBarLayout, 0, 1);

        layoutToast(mRefocusToast, w, h, rotation);
    }

    private void layoutToast(final View v, int w, int h, int rotation) {
        int tw = v.getMeasuredWidth();
        int th = v.getMeasuredHeight();
        int l, t, r, b, c;
        switch (rotation) {
            case 90:
                c = (int) (h / WIDTH_GRID * (WIDTH_GRID - 0.5));
                t = c - th / 2;
                b = c + th / 2;
                r = (int) (w / HEIGHT_GRID * (HEIGHT_GRID - 1.25));
                l = r - tw;
                mRefocusToast.setArrow(tw, th / 2, tw + th / 2, th, tw, th);
                break;
            case 180:
                t = (int) (h / HEIGHT_GRID * 1.25);
                b = t + th;
                r = (int) (w / WIDTH_GRID * (WIDTH_GRID - 0.25));
                l = r - tw;
                mRefocusToast.setArrow(tw - th / 2, 0, tw, 0, tw, -th / 2);
                break;
            case 270:
                c = (int) (h / WIDTH_GRID * 0.5);
                t = c - th / 2;
                b = c + th / 2;
                l = (int) (w / HEIGHT_GRID * 1.25);
                r = l + tw;
                mRefocusToast.setArrow(0, 0, 0, th / 2, -th / 2, 0);
                break;
            default:
                l = w / WIDTH_GRID / 4;
                b = (int) (h / HEIGHT_GRID * (HEIGHT_GRID - 1.25));
                r = l + tw;
                t = b - th;
                mRefocusToast.setArrow(0, th, th / 2, th, 0, th * 3 / 2);
                break;
        }
        mRefocusToast.layout(l, t, r, b);
    }

    public void hideUI() {
        for (View v : mViews) {
            if (v != null)
                v.setVisibility(View.INVISIBLE);
        }
        mVisible = false;
    }

    public void showUI() {
        for (View v : mViews) {
            if (v != null)
                v.setVisibility(View.VISIBLE);
        }
        mVisible = true;
    }

    private void layoutRemaingPhotos() {
        int rl = mPreview.getLeft();
        int rt = mPreview.getTop();
        int rr = mPreview.getRight();
        int rb = mPreview.getBottom();
        int w = mRemainingPhotos.getMeasuredWidth();
        int h = mRemainingPhotos.getMeasuredHeight();
        int m = getResources().getDimensionPixelSize(R.dimen.remaining_photos_margin);

        int hc = (rl + rr) / 2;
        int vc = (rt + rb) / 2 - m;
        if (mOrientation == 90 || mOrientation == 270) {
            vc -= w / 2;
        }
        if (hc < w / 2) {
            mRemainingPhotos.layout(0, vc - h / 2, w, vc + h / 2);
        } else {
            mRemainingPhotos.layout(hc - w / 2, vc - h / 2, hc + w / 2, vc + h / 2);
        }
        mRemainingPhotos.setRotation(-mOrientation);
    }

    public void updateRemainingPhotos(int remaining) {
        long remainingStorage = Storage.getAvailableSpace() - Storage.LOW_STORAGE_THRESHOLD_BYTES;
        if ((remaining < 0 && remainingStorage <= 0) || mHideRemainingPhoto) {
            mRemainingPhotos.setVisibility(View.GONE);
        } else {
            for (int i = mRemainingPhotos.getChildCount() - 1; i >= 0; --i) {
                mRemainingPhotos.getChildAt(i).setVisibility(View.VISIBLE);
            }
            if (remaining < LOW_REMAINING_PHOTOS) {
                mRemainingPhotosText.setText("<" + LOW_REMAINING_PHOTOS + " ");
            } else if (remaining >= HIGH_REMAINING_PHOTOS) {
                mRemainingPhotosText.setText(">" + HIGH_REMAINING_PHOTOS);
            } else {
                mRemainingPhotosText.setText(remaining + " ");
            }
        }
        mCurrentRemaining = remaining;
    }

    public void showRefocusToast(boolean show) {
        mRefocusToast.setVisibility(show ? View.VISIBLE : View.GONE);
        if ((mCurrentRemaining > 0) && !mHideRemainingPhoto) {
            mRemainingPhotos.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void setOrientation(int orientation, boolean animation) {
        mOrientation = orientation;
        View[] views = {
                mSceneModeSwitcher, mFilterModeSwitcher, mFrontBackSwitcher,
                mTsMakeupSwitcher, mFlashButton, mPreview, mMute, mShutter, mVideoShutter,
                mMakeupSeekBarLowText, mMakeupSeekBarHighText
        };

        for (View v : views) {
            if (v != null) {
                ((Rotatable) v).setOrientation(orientation, animation);
            }
        }
        layoutRemaingPhotos();
    }

    private class ArrowTextView extends TextView {
        private static final int TEXT_SIZE = 14;
        private static final int PADDING_SIZE = 18;
        private static final int BACKGROUND = 0x80000000;

        private Paint mPaint;
        private Path mPath;

        public ArrowTextView(Context context) {
            super(context);

            setText(context.getString(R.string.refocus_toast));
            setBackgroundColor(BACKGROUND);
            setVisibility(View.GONE);
            setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            setTextSize(TEXT_SIZE);
            setPadding(PADDING_SIZE, PADDING_SIZE, PADDING_SIZE, PADDING_SIZE);

            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(BACKGROUND);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mPath != null) {
                canvas.drawPath(mPath, mPaint);
            }
        }

        public void setArrow(float x1, float y1, float x2, float y2, float x3, float y3) {
            mPath = new Path();
            mPath.reset();
            mPath.moveTo(x1, y1);
            mPath.lineTo(x2, y2);
            mPath.lineTo(x3, y3);
            mPath.lineTo(x1, y1);
        }
    }
}