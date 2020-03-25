package ru.usedesk.chat_gui.internal.utils;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class ImageUtils {

    private ImageUtils() {
    }

    public static void checkForDisplayImage(@NonNull ImageView imageImageView,
                                            @NonNull String pictureUrl, int errorResId) {
        imageImageView.setImageResource(errorResId);
        if (!TextUtils.isEmpty(pictureUrl)) {
            Glide.with(imageImageView)
                    .load(pictureUrl)
                    .error(errorResId)
                    .into(imageImageView);
        }
    }

    public static void setImage(@NonNull ImageView target,
                                @NonNull Uri uri,
                                int errorId) {
        Glide.with(target)
                .load(uri)
                .centerCrop()
                .error(errorId)
                .into(target);
    }

    public static void checkForDisplayImage(@NonNull ImageView imageImageView,
                                            @NonNull ProgressBar progressBar,
                                            @NonNull String pictureUrl) {
        if (!TextUtils.isEmpty(pictureUrl)) {
            Glide.with(imageImageView)
                    .load(pictureUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            hideProgress(progressBar);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            hideProgress(progressBar);
                            return false;
                        }
                    })
                    .into(imageImageView);

        } else {
            hideProgress(progressBar);
        }
    }

    private static void hideProgress(ProgressBar progressBar) {
        if (progressBar != null) {
            progressBar.setProgress(0);
            progressBar.setVisibility(View.GONE);
        }
    }
}