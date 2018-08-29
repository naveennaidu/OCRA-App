package com.naveennaidu.opc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ImageAnnotationActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    ImageView annotationImageView;
    Button saveButton;
    Button undoButton;
//    RadioGroup toolBar;

    Bitmap bmp;
    Bitmap alteredBitmap;
    Canvas canvas;
    Paint paint;
//    Paint erasePaint;
//    Boolean eraser;
    Matrix matrix;
    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;

    Toolbar toolbar;
    Uri photoUri;
    ArrayList<Bitmap> drawBitmapsList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Image Annotation");
        setContentView(R.layout.activity_annotation_image);

//        toolbar = findViewById(R.id.app_bar);
//        setSupportActionBar(toolbar);

        annotationImageView = findViewById(R.id.annotationImageView);
        saveButton = findViewById(R.id.saveButton);
        undoButton = findViewById(R.id.undoButton);
//        toolBar = findViewById(R.id.toolbar);
//        eraser = false;

        saveButton.setOnClickListener(this);
        undoButton.setOnClickListener(this);

//        toolBar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                RadioButton rb = radioGroup.findViewById(i);
//                if (rb.getText() == "Erase") {
//                    eraser = true;
//                } else {
//                    eraser = false;
//                }
//            }
//        });

        photoUri = Uri.parse(getIntent().getStringExtra("imageUri"));
        try {
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inJustDecodeBounds = true;
            bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri), null, bmpFactoryOptions);

            bmpFactoryOptions.inJustDecodeBounds = false;
            bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri), null, bmpFactoryOptions);

            alteredBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
            canvas = new Canvas(alteredBitmap);

            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(5);

//            erasePaint = new Paint();
//            erasePaint.setAlpha(0);
//            erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
//            erasePaint.setAntiAlias(true);
//            erasePaint.setDither(true);
//            erasePaint.setStrokeWidth(10);

            matrix = new Matrix();
            canvas.drawBitmap(bmp, matrix, paint);

            annotationImageView.setImageBitmap(alteredBitmap);
            annotationImageView.setOnTouchListener(this);
        } catch (Exception e) {
            Log.v("ERROR", e.toString());
        }
    }

    @Override
    public void onClick(View view) {
        if (view == saveButton) {
            if (alteredBitmap != null) {

                FileOutputStream fileOutputStream = null;
                File file = new File(photoUri.getPath());
                try {
                    fileOutputStream = new FileOutputStream(file);
                    alteredBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    fileOutputStream.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        } else if (view == undoButton) {
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inJustDecodeBounds = true;
            try {
                bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri), null, bmpFactoryOptions);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            bmpFactoryOptions.inJustDecodeBounds = false;
            try {
                bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri), null, bmpFactoryOptions);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            alteredBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
            canvas = new Canvas(alteredBitmap);

            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(5);
            matrix = new Matrix();
            canvas.drawBitmap(bmp, matrix, paint);

            annotationImageView.setImageBitmap(alteredBitmap);
            annotationImageView.setOnTouchListener(this);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downx = motionEvent.getX();
                downy = motionEvent.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                upx = motionEvent.getX();
                upy = motionEvent.getY();
                canvas.drawLine(downx, downy, upx, upy, paint);
                annotationImageView.invalidate();
                downx = upx;
                downy = upy;
                break;
            case MotionEvent.ACTION_UP:
//                upx = motionEvent.getX();
//                upy = motionEvent.getY();
//                canvas.drawLine(downx, downy, upx, upy, paint);
//                annotationImageView.invalidate();
                paint.setColor(Color.GREEN);
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }
}
