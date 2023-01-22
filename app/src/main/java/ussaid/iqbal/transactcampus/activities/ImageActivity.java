package ussaid.iqbal.transactcampus.activities;

import static android.content.ContentValues.TAG;
import static ussaid.iqbal.transactcampus.app.App.HideHUD;
import static ussaid.iqbal.transactcampus.app.App.ShowHUD;
import static ussaid.iqbal.transactcampus.utils.Constants.DISPLAY_IMAGE_OBJECT;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jsibbold.zoomage.ZoomageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ussaid.iqbal.transactcampus.R;

import ussaid.iqbal.transactcampus.models.ImagesModel;
import ussaid.iqbal.transactcampus.utils.TinyDB;

public class ImageActivity extends AppCompatActivity {

   private final Context context = this;
   private ZoomageView zoomImage;
   private ImagesModel imagesModel;
   private Bitmap loadedBitmap = null;
   private FloatingActionButton actionSetWallpaper, actionShareImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        SetUpViews();
    }

    private void SetUpViews(){
        TinyDB tinyDB = new TinyDB(context);
        zoomImage = findViewById(R.id.imgZoom);
        actionSetWallpaper = findViewById(R.id.actionSetWallpaper);
        actionShareImage = findViewById(R.id.actionShareImage);
        actionSetWallpaper.setVisibility(View.GONE);
        actionShareImage.setVisibility(View.GONE);
        imagesModel = tinyDB.getObject(DISPLAY_IMAGE_OBJECT, ImagesModel.class);
        DisplayImage();
        setTitle(imagesModel.getAuthor());
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        actionSetWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowHUD(context);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            wallpaperManager.setBitmap(loadedBitmap);
                            runOnUiThread (new Thread(new Runnable() {
                                public void run() {
                                    HideHUD();
                                    Toast.makeText(
                                            context,
                                            getString(R.string.msg_wallpaper),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }));
                        } catch (IOException e) {
                            runOnUiThread (new Thread(new Runnable() {
                                public void run() {
                                    HideHUD();
                                }
                            }));
                            e.printStackTrace();
                        }

                    }
                }).start();


            }
        });
        actionShareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(loadedBitmap);
            }
        });
    }

    private void DisplayImage(){
        String url = imagesModel.getUrl();
        Glide.with(context)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        zoomImage.setImageBitmap(resource);
                        loadedBitmap = resource;
                        actionSetWallpaper.setVisibility(View.VISIBLE);
                        actionShareImage.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });

        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.dummy)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(zoomImage);
    }


    private void saveImage(Bitmap image) {
        ShowHUD(context);
        //TODO - Should be processed in another thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                File imagesFolder = new File(getCacheDir(), "images");
                try {
                    imagesFolder.mkdirs();
                    File file = new File(imagesFolder, "shared_image.png");
                    FileOutputStream stream = new FileOutputStream(file);
                    image.compress(Bitmap.CompressFormat.PNG, 90, stream);
                    stream.flush();
                    stream.close();
                    Uri uri = FileProvider.getUriForFile(context, "ussaid.iqbal.transactcampus.fileprovider", file);
                    runOnUiThread (new Thread(new Runnable() {
                        public void run() {
                            HideHUD();
                            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setType("image/png");
                            startActivity(intent);
                        }
                    }));

                } catch (IOException e) {
                    runOnUiThread (new Thread(new Runnable() {
                        public void run() {
                            HideHUD();
                        }
                    }));
                    Log.d(TAG, "IOException while trying to write file for sharing: " + e.getMessage());
                }
            }
        }).start();

    }
}