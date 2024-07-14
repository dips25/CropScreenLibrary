package com.layout.swiiiipe.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;

public class MainActivity2 extends AppCompatActivity {

    CropView cropView;

    Bitmap bitmap;
    FrameLayout mainFRame;

    Rect srcRect, destRect;

    ImageView erase;
    ImageView scale;
    ImageView mWand;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);



        Intent intent = getIntent();
        String path = intent.getStringExtra("data");

        File file =  new File(path);



        //byte[] bytes = intent.getByteArrayExtra("data");




        mainFRame = (FrameLayout) findViewById(R.id.imageFrame);
        erase = (android.widget.ImageView) findViewById(R.id.erase);
        scale = (android.widget.ImageView) findViewById(R.id.scale);
        mWand=  (android.widget.ImageView) findViewById(R.id.mwand);



        ColorStateList colorStateList = ContextCompat.getColorStateList(this , R.color.icon_tint);
        erase.setImageTintList(colorStateList);

        erase.setSelected(false);
        erase.invalidate();

        scale.setSelected(false);
        scale.invalidate();

        mWand.setSelected(false);
        mWand.invalidate();



        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

        srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destRect = new Rect(0, 0, Utils.getScreenWidth(this)
                , Utils.getScreenHeight(this)-Utils.dPtoPixel(this , 50));

        float srcAspect = bitmap.getWidth() / bitmap.getHeight();
        float destAspect = Utils.getScreenWidth(this)/Utils.getScreenHeight(this);

        if (bitmap.getWidth() > destRect.width()) {

            destRect = new Rect(0, 0, Utils.getScreenWidth(this), (int) (Utils.getScreenHeight(this) / srcAspect));

        } else {

            destRect = new Rect(0, 0, (int) (bitmap.getHeight() * srcAspect)
                    , Utils.getScreenHeight(this)-Utils.dPtoPixel(this , 50));
//
//        }

        }

        Bitmap bitmap1 = Bitmap.createBitmap(destRect.width() , destRect.height() , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap1);
        canvas.drawBitmap(bitmap , srcRect , destRect , null);

        MyDrawingSurface md = new MyDrawingSurface(this , bitmap1);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(destRect.width() , destRect.height());
        params.gravity = Gravity.CENTER;
        params.bottomMargin = Utils.PixeltoDp(this , 50);
        md.setLayoutParams(params);

        erase.setOnClickListener((v) -> {

            erase.setSelected(true);
            erase.invalidate();

            md.setMode(1);


        });

        scale.setOnClickListener((v) -> {

            scale.setSelected(true);
            scale.invalidate();

            md.setMode(2);

        });

        mWand.setOnClickListener((v)->{

            mWand.setSelected(true);
            mWand.invalidate();

            md.setMode(3);
        });

        mainFRame.addView(md);
    }
}
