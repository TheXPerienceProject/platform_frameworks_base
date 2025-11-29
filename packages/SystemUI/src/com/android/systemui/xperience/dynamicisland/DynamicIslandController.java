package com.android.systemui.xperience.dynamicisland;

import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller for Dynamic Island feature
 * Integrated directly into SystemUI
 */
public class DynamicIslandController {
    private static final String TAG = "DynamicIsland";
    private static final boolean DEBUG = true;

    private final Context mContext;
    private final Handler mHandler;
    private DynamicIslandView mView;
    private MediaSessionManager mMediaSessionManager;
    private List<MediaController> mActiveControllers = new ArrayList<>();

    // Trusted music apps whitelist
    private static final Set<String> TRUSTED_MUSIC_APPS = new HashSet<String>() {{
        add("com.spotify.music");
        add("com.google.android.music");
        add("com.google.android.apps.youtube.music");
        add("com.apple.android.music");
        add("com.amazon.music");
        add("com.soundcloud.android");
        add("com.deezer.android.app");
        add("com.tencent.qqmusic");
        add("com.netease.cloudmusic");
        add("com.spotify.lite");
        add("com.shazam.android");
    }};

    // Music genres for metadata filtering
    private static final Set<String> MUSIC_GENRES = new HashSet<String>() {{
        add("music");
        add("rock");
        add("pop");
        add("jazz");
        add("hip hop");
        add("rap");
        add("electronic");
        add("classical");
        add("country");
        add("r&b");
        add("latin");
        add("metal");
        add("blues");
        add("folk");
        add("reggae");
    }};

    public DynamicIslandController(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        if (DEBUG) Log.d(TAG, "DynamicIslandController created");
    }

    public void start() {
        if (DEBUG) Log.d(TAG, "Starting Dynamic Island");
        setupMediaSessions();
    }

    public void setView(DynamicIslandView view) {
        mView = view;
        if (DEBUG) Log.d(TAG, "Dynamic Island view set");
    }

    private void setupMediaSessions() {
        try {
            Log.d(TAG, "Setting up MediaSession listener");
            mMediaSessionManager = mContext.getSystemService(MediaSessionManager.class);
            MediaSessionManager.OnActiveSessionsChangedListener sessionListener =
                new MediaSessionManager.OnActiveSessionsChangedListener() {
                    @Override
                    public void onActiveSessionsChanged(List<MediaController> controllers) {
                        Log.d(TAG, "MediaSession callback triggered, controllers: " + (controllers != null ? controllers.size() : 0));
                        handleActiveSessionsChanged(controllers);
                    }
                };

            mMediaSessionManager.addOnActiveSessionsChangedListener(sessionListener, null);
            Log.d(TAG, "Media session listener registered successfully");

            // También verifica las sesiones activas actuales
            List<MediaController> currentSessions = mMediaSessionManager.getActiveSessions(null);
            Log.d(TAG, "Current active sessions: " + (currentSessions != null ? currentSessions.size() : 0));
            if (currentSessions != null) {
                for (MediaController controller : currentSessions) {
                    Log.d(TAG, "Current session: " + controller.getPackageName());
                }
            }

        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for media sessions", e);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up media sessions", e);
        }
    }

    private void handleActiveSessionsChanged(List<MediaController> controllers) {
        mHandler.post(() -> {
            mActiveControllers.clear();
            if (controllers != null) {
                mActiveControllers.addAll(controllers);
            }

            if (DEBUG) Log.d(TAG, "Active sessions changed: " + mActiveControllers.size());

            // Find the first valid music playback to show
            for (MediaController controller : mActiveControllers) {
                if (shouldShowMusicInIsland(controller)) {
                    showMusicPlayback(controller);
                    return;
                }
            }

            // No music playback found, hide the island
            hideMusicPlayback();
        });
    }

    /**
     * Hybrid filtering system for music detection
     */
    private boolean shouldShowMusicInIsland(MediaController controller) {
        if (controller == null) return false;

        int score = 0;
        String packageName = controller.getPackageName();

        // +50 points for trusted music apps
        if (TRUSTED_MUSIC_APPS.contains(packageName)) {
            score += 50;
            if (DEBUG) Log.d(TAG, "Trusted music app: " + packageName);
        }

        // Analyze metadata
        MediaMetadata metadata = controller.getMetadata();
        if (isMusicByMetadata(metadata)) {
            score += 30;
            if (DEBUG) Log.d(TAG, "Music detected by metadata");
        }

        // Check playback state
        PlaybackState playbackState = controller.getPlaybackState();
        if (playbackState != null &&
            playbackState.getState() == PlaybackState.STATE_PLAYING) {
            score += 20;
            if (DEBUG) Log.d(TAG, "Currently playing");
        }

        // Additional check: block known video apps
        if (isVideoApp(packageName)) {
            score -= 100; // Immediately disqualify
            if (DEBUG) Log.d(TAG, "Video app blocked: " + packageName);
        }

        boolean shouldShow = score >= 50;
        if (DEBUG) Log.d(TAG, "App " + packageName + " score: " + score + ", show: " + shouldShow);

        return shouldShow;
    }

    private boolean isMusicByMetadata(MediaMetadata metadata) {
        if (metadata == null) return false;

        boolean isMusic = false;

        // Check genre
        String genre = metadata.getString(MediaMetadata.METADATA_KEY_GENRE);
        if (genre != null) {
            String lowerGenre = genre.toLowerCase();
            for (String musicGenre : MUSIC_GENRES) {
                if (lowerGenre.contains(musicGenre)) {
                    isMusic = true;
                    break;
                }
            }
        }

        // Check duration (music typically > 90 seconds, short videos < 90)
        long duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
        if (duration > 90000) { // > 1.5 minutes
            isMusic = true;
        } else if (duration > 0 && duration < 30000) { // < 30 seconds
            isMusic = false; // Likely a short video
        }

        // Check structure (music usually has both title and artist)
        boolean hasMusicStructure =
            metadata.getString(MediaMetadata.METADATA_KEY_TITLE) != null &&
            metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) != null;

        return isMusic && hasMusicStructure;
    }

    private boolean isVideoApp(String packageName) {
        return packageName != null && (
            packageName.contains("youtube") ||
            packageName.contains("netflix") ||
            packageName.contains("tiktok") ||
            packageName.contains("instagram") ||
            packageName.contains("twitch") ||
            packageName.contains("vimeo") ||
            packageName.contains("dailymotion")
        );
    }

    private void showMusicPlayback(MediaController controller) {
        if (mView == null) return;

        MediaMetadata metadata = controller.getMetadata();
        if (metadata != null) {
            mView.showMusicPlayback(metadata);
            if (DEBUG) Log.d(TAG, "Showing music playback in Dynamic Island");
        }
    }

    private void hideMusicPlayback() {
        if (mView != null) {
            mView.hide();
            if (DEBUG) Log.d(TAG, "Hiding Dynamic Island");
        }
    }

    public void destroy() {
        if (mMediaSessionManager != null) {
            // Note: We'd need to keep reference to the listener to remove it properly
            // For now, we'll rely on SystemUI lifecycle
        }
        mActiveControllers.clear();
        if (DEBUG) Log.d(TAG, "Dynamic Island controller destroyed");
    }
}
