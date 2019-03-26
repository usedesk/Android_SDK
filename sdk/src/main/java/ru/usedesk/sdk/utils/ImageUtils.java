package ru.usedesk.sdk.utils;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ImageUtils {

    private ImageUtils() {
    }

    public static void checkForDisplayImage(ImageView imageImageView, String pictureUrl, int errorResId) {
        if (!TextUtils.isEmpty(pictureUrl)) {
            Picasso.with(imageImageView.getContext())
                    .load(pictureUrl)
                    .error(errorResId)
                    .into(imageImageView);
        } else {
            imageImageView.setImageResource(errorResId);
        }
    }

    public static void checkForDisplayImage(ImageView imageImageView, final ProgressBar progressBar, String pictureUrl) {
        if (!TextUtils.isEmpty(pictureUrl)) {
            Picasso.with(imageImageView.getContext())
                    .load(pictureUrl)
                    .into(imageImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            hideProgress(progressBar);
                        }

                        @Override
                        public void onError() {
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