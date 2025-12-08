package com.android.systemui.xperience.dynamicisland;

import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.util.Log;

/**
 * Manager class for Dynamic Island feature
 * Provides the main entry point for SystemUI integration
 */
public class DynamicIslandManager {
    private static final String TAG = "DynamicIslandManager";

    private final Context mContext;
    private DynamicIslandController mController;
    private DynamicIslandView mView;
    private MediaController mMediaController;

    public static final String ACTION_TOGGLE_DYNAMIC_ISLAND =
        "com.android.systemui.xperience.TOGGLE_DYNAMIC_ISLAND";
    public static final String EXTRA_ISLAND_VISIBLE =
        "is_island_visible";

    public DynamicIslandManager(Context context) {
        mContext = context;
        mController = new DynamicIslandController(context);
    }

    public void start() {
        Log.d(TAG, "Starting Dynamic Island Manager");
        mController.start();
    }

    public void setView(DynamicIslandView view) {
        mView = view;
        mController.setView(view);
        Log.d(TAG, "Dynamic Island view attached to manager");

        mView.setOnDynamicIslandClickListener(
            new DynamicIslandView.OnDynamicIslandClickListener() {
                @Override
                public void onDynamicIslandClick() {
                    handleDynamicIslandClick();
                }

                @Override
                public void onDynamicIslandLongClick() {
                    handleDynamicIslandLongClick();
                }
            }
        );
    }

    public void setMediaController(MediaController mediaController) {
        mMediaController = mediaController;
    }

    public void destroy() {
        if (mController != null) {
            mController.destroy();
        }
        Log.d(TAG, "Dynamic Island Manager destroyed");
    }

    private void handleDynamicIslandClick() {
        Log.d(TAG, "🎵 Click en Dynamic Island - controles de música");

        toggleMusicPlayback();
    }

    private void handleDynamicIslandLongClick() {
        Log.d(TAG, "🎵 Long Click - opciones avanzadas");

        showMusicOptions();
    }

    private void toggleMusicPlayback() {
        Log.d(TAG, "⏯️ Pausar/Reproducir música");

        // Controlar la reproducción de música actual
        if (mMediaController != null) {
            if (mMediaController.getPlaybackState() != null &&
                mMediaController.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
                mMediaController.getTransportControls().pause();
            } else {
                mMediaController.getTransportControls().play();
            }
        } else {
            Log.d(TAG, "⚠️ No hay MediaController disponible");
        }
    }

    private void showMusicOptions() {
        Log.d(TAG, "📋 Mostrando opciones de música");
        // Aquí puedes implementar un popup menu o diálogo
        // con opciones como: Siguiente, Anterior, Like, etc.
    }

    /**
    * Muestra la Dynamic Island con la información de la metadata.
    * Este método es llamado por DynamicIslandController (para música real)
    * y por CentralSurfacesImpl (para pruebas de ADB).
    */
    public void show(MediaMetadata metadata) {
        if (mView != null) {
            mView.showMusicPlayback(metadata);

            // La lógica de DynamicIslandController debería estar aquí,
            // pero por ahora solo pasamos la metadata a la vista.

            // Log para confirmar que la prueba de ADB llegó al manager
            Log.d(TAG, "Dynamic Island Show triggered with metadata from ADB/Controller.");
        }
    }

    public void hide() {
        if (mView != null) {
            mView.hide();
        }
    }
}
