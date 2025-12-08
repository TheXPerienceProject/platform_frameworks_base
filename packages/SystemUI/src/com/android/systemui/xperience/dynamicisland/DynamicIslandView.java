package com.android.systemui.xperience.dynamicisland;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.res.R;

/**
 * Dynamic Island view that shows music playback and other system activities
 * Based on the original APK implementation
 */
public class DynamicIslandView extends FrameLayout {
    private static final String TAG = "DynamicIslandView";
    private static final boolean DEBUG = true;

    // View components
    private ImageView mAlbumArt;
    private TextView mTitle;
    private TextView mArtist;
    private View mBackground;

    // Animation
    private ValueAnimator mExpandAnimator;
    private ValueAnimator mCollapseAnimator;
    private boolean mIsExpanded = false;

    // Dimensions
    private int mCollapsedWidth;
    private int mExpandedWidth;
    private int mCollapsedHeight;
    private int mExpandedHeight;

    public DynamicIslandView(Context context) {
        this(context, null);
    }

    public DynamicIslandView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DynamicIslandView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (DEBUG)
        {
            Log.d(TAG, "Initializing Dynamic Island view - Instance: " + this.hashCode());
            Log.d(TAG, "Thread: " + Thread.currentThread().getName());
            // Añade un stack trace para ver de dónde se está llamando
            Log.d(TAG, "Call stack:", new Exception("Debug stack trace"));
            //Log.d(TAG, "Initializing Dynamic Island view");
        }

        // inflate layout
        LayoutInflater.from(mContext).inflate(com.android.systemui.res.R.layout.dynamic_island_content, this, true);

        // Load dimensions
        mCollapsedWidth = getResources().getDimensionPixelSize(com.android.systemui.res.R.dimen.dynamic_island_width_collapsed);
        mExpandedWidth = getResources().getDimensionPixelSize(com.android.systemui.res.R.dimen.dynamic_island_width_expanded);
        mCollapsedHeight = getResources().getDimensionPixelSize(com.android.systemui.res.R.dimen.dynamic_island_height_collapsed);
        mExpandedHeight = getResources().getDimensionPixelSize(com.android.systemui.res.R.dimen.dynamic_island_height_expanded);

        setupAnimations();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (DEBUG) Log.d(TAG, "Dynamic Island view inflated");

        // Find views
        mBackground = findViewById(com.android.systemui.res.R.id.dynamic_island_background);
        mAlbumArt = findViewById(com.android.systemui.res.R.id.dynamic_island_album_art);
        mTitle = findViewById(com.android.systemui.res.R.id.dynamic_island_title);
        mArtist = findViewById(com.android.systemui.res.R.id.dynamic_island_artist);

        if (DEBUG){
        // DEBUG: Verificar si las vistas se encontraron
        Log.d(TAG, "Background found: " + (mBackground != null));
        Log.d(TAG, "AlbumArt found: " + (mAlbumArt != null));
        Log.d(TAG, "Title found: " + (mTitle != null));
        Log.d(TAG, "Artist found: " + (mArtist != null));

        }
        // Set initial state (collapsed)
        setCollapsedState();
    }

    private void setCollapsedState() {
        if (mBackground != null) {
            LayoutParams params = (LayoutParams) mBackground.getLayoutParams();
            params.width = mCollapsedWidth;
            params.height = mCollapsedHeight;
            mBackground.setLayoutParams(params);
        }
        mIsExpanded = false;
    }

    private void setupAnimations() {
        // Expand animation
        mExpandAnimator = ValueAnimator.ofFloat(0f, 1f);
        mExpandAnimator.setDuration(300);
        mExpandAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mExpandAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            updateExpansion(value, false);
        });
        mExpandAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsExpanded = true;
            }
        });

        // Collapse animation
        mCollapseAnimator = ValueAnimator.ofFloat(1f, 0f);
        mCollapseAnimator.setDuration(300);
        mCollapseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mCollapseAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            updateExpansion(value, true);
        });
        mCollapseAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsExpanded = false;
                setVisibility(View.GONE);
            }
        });
    }

    private void updateExpansion(float progress, boolean collapsing) {
        if (mBackground == null) return;

        int targetWidth, targetHeight;

        if (collapsing) {
            targetWidth = (int) (mExpandedWidth - (mExpandedWidth - mCollapsedWidth) * progress);
            targetHeight = (int) (mExpandedHeight - (mExpandedHeight - mCollapsedHeight) * progress);
        } else {
            targetWidth = (int) (mCollapsedWidth + (mExpandedWidth - mCollapsedWidth) * progress);
            targetHeight = (int) (mCollapsedHeight + (mExpandedHeight - mCollapsedHeight) * progress);
        }

        LayoutParams params = (LayoutParams) mBackground.getLayoutParams();
        params.width = targetWidth;
        params.height = targetHeight;
        mBackground.setLayoutParams(params);

        // Update content alpha
        if (mTitle != null && mArtist != null && mAlbumArt != null) {
            float contentAlpha = collapsing ? 1f - progress : progress;
            mTitle.setAlpha(contentAlpha);
            mArtist.setAlpha(contentAlpha);
            mAlbumArt.setAlpha(contentAlpha);
        }
    }

    public void showMusicPlayback(MediaMetadata metadata) {
        if (DEBUG) Log.d(TAG, "Showing music playback");

        if (DEBUG) Log.d(TAG, "Views - Background: " + mBackground + ", Title: " + mTitle + ", Artist: " + mArtist + ", AlbumArt: " + mAlbumArt);

        // Update content
        if (metadata != null) {
            String title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            String artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
            Bitmap albumArt = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);

            if (DEBUG) Log.d(TAG, "Music data - Title: " + title + ", Artist: " + artist + ", AlbumArt: " + (albumArt != null));

            if (mTitle != null) {
                mTitle.setText(title != null ? title : getResources().getString(com.android.systemui.res.R.string.dynamic_island_unknown_title));
                if (DEBUG) Log.d(TAG, "Title set to: " + mTitle.getText());
            }

            if (mArtist != null) {
                mArtist.setText(artist != null ? artist : getResources().getString(com.android.systemui.res.R.string.dynamic_island_unknown_artist));
                Log.d(TAG, "Artist set to: " + mArtist.getText());
            } else {
                Log.w(TAG, "Artist view is NULL!");
            }

            if (mAlbumArt != null) {
                if (albumArt != null) {
                    mAlbumArt.setImageBitmap(albumArt);
                    mAlbumArt.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Album art set and visible");
                } else {
                    mAlbumArt.setVisibility(View.GONE);
                    Log.d(TAG, "Album art hidden (no image)");
                }
            } else {
                Log.w(TAG, "AlbumArt view is NULL!");
            }

        // Start expand animation if not already expanded
        if (!mIsExpanded && mExpandAnimator != null && !mExpandAnimator.isRunning()) {
            if (mCollapseAnimator != null && mCollapseAnimator.isRunning()) {
                mCollapseAnimator.cancel();
            }
            mExpandAnimator.start();
            Log.d(TAG, "Expand animation started");
        }
      }
   }

    public void hide() {
        if (DEBUG) Log.d(TAG, "Hiding Dynamic Island");

        if (mIsExpanded && mCollapseAnimator != null && !mCollapseAnimator.isRunning()) {
            if (mExpandAnimator != null && mExpandAnimator.isRunning()) {
                mExpandAnimator.cancel();
            }
            mCollapseAnimator.start();
        } else if (!mIsExpanded) {
            setVisibility(View.GONE);
        }
    }

    public boolean isExpanded() {
        return mIsExpanded;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Clean up animations
        if (mExpandAnimator != null) {
            mExpandAnimator.cancel();
        }
        if (mCollapseAnimator != null) {
            mCollapseAnimator.cancel();
        }
    }
}
