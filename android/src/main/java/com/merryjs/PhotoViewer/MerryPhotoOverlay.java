
package com.merryjs.PhotoViewer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSources;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.merryjs.PhotoViewer.R;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by bang on 26/07/2017.
 */

public class MerryPhotoOverlay extends RelativeLayout {
    private TextView tvTitle;
    private TextView tvTitlePager;

    private TextView tvDescription;
    private TextView tvShare;
    private TextView tvClose;
    private ImageViewer imageViewer;
    private String sharingText;
    private String attribution;
    public void setImageViewer(ImageViewer imageViewer){
        this.imageViewer = imageViewer;
    }
    public MerryPhotoOverlay(Context context) {
        super(context);
        init();
    }

    public MerryPhotoOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MerryPhotoOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setHideShareButton(Boolean hideShareButton) {
        tvShare.setVisibility(hideShareButton ? View.GONE : View.VISIBLE);
    }
    public void setHideCloseButton(Boolean hideCloseButton) {
        tvClose.setVisibility(hideCloseButton ? View.GONE : View.VISIBLE);
    }
    public void setPagerText(String text) {
        tvTitlePager.setText(text);
    }

    public void setPagerTextColor(String color) {
        tvTitlePager.setTextColor(Color.parseColor(color));
    }

    public void setDescription(String description) {
        tvDescription.setText(description);
    }

    public void setDescriptionTextColor(int color) {
        tvDescription.setTextColor(color);
    }

    public void setShareText(String text) {
        tvShare.setText(text);
    }

    public void setShareContext(String text) {
        this.sharingText = text;
    }

    public void setAttribution(String text) {
        this.attribution = text;
    }

    public void setShareTextColor(String color) {
        tvShare.setTextColor(Color.parseColor(color));
    }

    public void setTitleTextColor(int color) {
        tvTitle.setTextColor(color);
    }

    public void setTitleText(String text) {
        tvTitle.setText(text);
    }

    private void sendShareIntent() {
        ImageRequest imageRequest = ImageRequest.fromUri(this.sharingText);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchImageFromBitmapCache(imageRequest, getContext());
        try {
            CloseableReference<CloseableImage> result = DataSources.waitForFinalResult(dataSource);
            if (result != null) {
                CloseableImage closeableImage = result.get();
                if (closeableImage instanceof CloseableBitmap) {
                    // do something with the bitmap
                    Bitmap bitmap = ((CloseableBitmap)closeableImage).getUnderlyingBitmap();
                    // save bitmap to cache directory
                    try {

                        File cachePath = new File(getContext().getCacheDir(), "images");
                        cachePath.mkdirs(); // don't forget to make the directory
                        FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    File imagePath = new File(getContext().getCacheDir(), "images");
                    File newFile = new File(imagePath, "image.png");
                    Uri contentUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", newFile);

                    if (contentUri != null) {
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                        shareIntent.setDataAndType(contentUri, getContext().getContentResolver().getType(contentUri));
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, tvTitle.getText() + "\n" + this.attribution);
                        getContext().startActivity(Intent.createChooser(shareIntent, "Choose an app"));
                    }
                }
                Log.d("result", result.toString());
                // Do something with the image, but do not keep the reference to it!
                // The image may get recycled as soon as the reference gets closed below.
                // If you need to keep a reference to the image, read the following sections.
            }
        } catch (Throwable t) {
            Log.d("throwable", t.toString());
        } finally {
            dataSource.close();
        }

    }

    private void init() {
        View view = inflate(getContext(), R.layout.photo_viewer_overlay, this);

        tvTitlePager = (TextView) view.findViewById(R.id.tvTitlePager);
        tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        tvDescription = (TextView) view.findViewById(R.id.tvDescription);

        tvShare = (TextView) view.findViewById(R.id.btnShare);
        tvShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendShareIntent();
            }
        });
        tvClose = (TextView) view.findViewById(R.id.btnClose);
        tvClose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
               imageViewer.onDismiss();
            }
        });
    }
}
