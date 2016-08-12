package com.android.tv.settings.desktop;

import android.widget.Button;
import android.widget.ImageView;

import com.android.tv.settings.R;

/**
 * Created by Rain on 2016/7/13.
 */
public class ShowViewHolder {
    private final static String TAG = "ShowViewHolder";
    private final boolean DEBUG = true;
    private ShowActivity showActivity;
    private ImageView desktop_img = null;
    private Button wallpaper_btn = null;

    public ShowViewHolder(ShowActivity showActivity) {
        this.showActivity = showActivity;
        findView();
    }

    private void findView(){
        wallpaper_btn = (Button) showActivity.findViewById(R.id.wallpaper_btn);
        desktop_img = (ImageView) showActivity.findViewById(R.id.cur_wallpaper);

    }

    public Button getWallpaper_btn(){
        return wallpaper_btn;
    }
    public ImageView getDesktop_img(){
        return desktop_img;
    }
}
