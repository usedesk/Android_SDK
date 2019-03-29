package ru.usedesk.sdk.utils;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

public class ImageUtils {

    private ImageUtils() {
    }

    public static void checkForDisplayImage(ImageView imageImageView, String pictureUrl, int errorResId) {
        if (!TextUtils.isEmpty(pictureUrl)) {
            GlideApp.with(imageImageView)
                    .load(pictureUrl)
                    .error(errorResId)
                    .into(imageImageView);
        } else {
            imageImageView.setImageResource(errorResId);
        }
    }

    public static void checkForDisplayImage(ImageView imageImageView, final ProgressBar progressBar,
                                            String pictureUrl) {
        if (!TextUtils.isEmpty(pictureUrl)) {
            GlideApp.with(imageImageView)
                    .load(pictureUrl)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            hideProgress(progressBar);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);

                            hideProgress(progressBar);
                        }
                    });

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