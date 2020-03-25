package ru.usedesk.common_gui.internal;

import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

public class ImageUtils {

    private ImageUtils() {
    }

    public static void setImage(@NonNull ImageView imageImageView, @NonNull String pictureUrl, int errorResId) {
        imageImageView.setImageResource(errorResId);
        if (!TextUtils.isEmpty(pictureUrl)) {
            Glide.with(imageImageView)
                    .load(pictureUrl)
                    .error(errorResId)
                    .into(imageImageView);
        }
    }

    public static void setImageCenter(@NonNull ImageView target, @NonNull Uri uri, int errorId) {
        Glide.with(target)
                .load(uri)
                .centerCrop()
                .error(errorId)
                .into(target);
    }

    public static void setImage(@NonNull ImageView target, @NonNull String path) {
        Glide.with(target)
                .load(path)
                .into(target);
    }
}