package com.android.tv.settings.desktop;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.tv.settings.R;

import java.util.List;

/**
 * Created by Rain on 2016/7/13.
 */
public class ShowActivity extends Activity{

    private final static String TAG = "ShowActivity";
    private final boolean DEBUG = true;
    private ShowViewHolder showViewHolder;
    private static final int ACTIVITYRESULT = 10086;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallpaper_show);

        initMainView();
        setBackgroundThumbnail();
    }

    private void initMainView(){
        showViewHolder = new ShowViewHolder(ShowActivity.this);
        registListenter();
    }

    private void registListenter(){
        showViewHolder.getWallpaper_btn().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                populateWallpaperTypes();
            }
        });
    }

    private void populateWallpaperTypes() {
        // Search for activities that satisfy the ACTION_SET_WALLPAPER action
        Intent intent = new Intent("com.dazzle.mlauncher.SETWALLPAPERSET");
        PackageManager pm = getPackageManager();
        List<ResolveInfo> apps = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (apps == null || apps.size() == 0) {
            Log.d(TAG, "cannot find app with action : com.dazzle.mlauncher.SETWALLPAPERSET");

            return;
        }

        startActivityForResult(intent, ACTIVITYRESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITYRESULT) {
            setBackgroundThumbnail();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setBackgroundThumbnail() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        wallpaperManager.getBitmap();
        BitmapDrawable bd = new BitmapDrawable(this.getResources(), wallpaperManager.getBitmap());
        showViewHolder.getDesktop_img().setImageDrawable(bd);
    }

}
