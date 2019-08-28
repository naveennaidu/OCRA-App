package com.naveennaidu.opc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ImageAnnotationActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    ImageView annotationImageView;
    Button saveButton;
    Button undoButton;
    Button assessButton;
    TextView assessText;

    ArrayList<String> labelList = new ArrayList(Arrays.asList(new String[]{"Other low Malignancy risk", "Traumatic Ulcer", "Lichen Planus","Submucous Fibrosis", "Leukoplakia: homogeneous", "Leukoplakia: non-homogeneous", "Erythroplakia", "Likely malignant"}));
    Spinner labelSpinner;
    ArrayAdapter lab;

    Bitmap bmp;
    Bitmap alteredBitmap;
    Canvas canvas;
    Paint paint;
    Paint maskPaint;
    private Path path = new Path();

    Matrix matrix;
    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;

    Bitmap cropBitmap;
    Bitmap cbmp;
    Bitmap resultMaskBitmap;
    Bitmap getMaskBitmap;
    Bitmap finalBitmap;

    int maxValX;
    int maxValY;
    int minValX;
    int minValY;

    Bitmap maskBmp;

    Uri photoUri;
    ArrayList<Point> drawPoints = new ArrayList<>();
    ArrayList<Integer> coordinatesX = new ArrayList<>();
    ArrayList<Integer> coordinatesY = new ArrayList<>();

    int MAX_HEIGHT = 1024;
    int MAX_WIDTH = 1024;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Image Annotation");
        setContentView(R.layout.activity_annotation_image);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        annotationImageView = findViewById(R.id.annotationImageView);
        saveButton = findViewById(R.id.saveButton);
        undoButton = findViewById(R.id.undoButton);

        assessButton = findViewById(R.id.assessButton);
        assessText = findViewById(R.id.assessText);

        labelSpinner = findViewById(R.id.labelSpinner);
        lab = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, labelList);
        lab.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        labelSpinner.setAdapter(lab);


        saveButton.setOnClickListener(this);
        undoButton.setOnClickListener(this);

        initDrawCanvas();
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

//                maxValX = 0;
//                maxValY = 0;
//
//                for (int i=0; i<drawPoints.size(); i++){
//                    if (drawPoints.get(i).x > maxValX) {
//                        maxValX = drawPoints.get(i).x;
//                    }
//                    if (drawPoints.get(i).y > maxValY) {
//                        maxValY = drawPoints.get(i).y;
//                    }
//                }
//                minValX = maxValX;
//                minValY = maxValY;
//                for (int i=0; i<drawPoints.size(); i++){
//                    if (drawPoints.get(i).x < minValX){
//                        minValX = drawPoints.get(i).x;
//                    }
//                    if (drawPoints.get(i).y < minValY){
//                        minValY = drawPoints.get(i).y;
//                    }
//                }
//
//                performCrop(photoUri);

                String photoUriString = photoUri.toString();
                Log.e("photo name", photoUriString);
                String[] splitUri = photoUriString.split("/");

                for(int i=0; i<drawPoints.size(); i++){
                    coordinatesX.add(drawPoints.get(i).x);
                    coordinatesY.add(drawPoints.get(i).y);
                }

                JSONObject json = writeJSON(splitUri[8].substring(0, splitUri[8].length()-4), coordinatesX, coordinatesY);
                try {
                    Writer output = null;
                    File newfile = new File(Environment.getExternalStorageDirectory() + "/" + splitUri[6] + "/" + splitUri[7]+ "/" + "test" + ".json");
                    output = new BufferedWriter(new FileWriter(newfile));
                    output.write(json.toString());
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        } else if (view == undoButton) {
            initDrawCanvas();
            drawPoints.clear();
            path.reset();
        }
    }

    public JSONObject writeJSON(String name, ArrayList<Integer> cooridinatesX, ArrayList<Integer> cooridinatesY){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("X", cooridinatesX);
            jsonObject.put("Y", cooridinatesY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downx = motionEvent.getX();
                downy = motionEvent.getY();
                path.moveTo(downx, downy);
                break;
            case MotionEvent.ACTION_MOVE:
                upx = motionEvent.getX();
                upy = motionEvent.getY();
                canvas.drawLine(downx, downy, upx, upy, paint);
                annotationImageView.invalidate();
                downx = upx;
                downy = upy;
                drawPoints.add(new Point(Math.round(upx) , Math.round(upy)));
                path.lineTo(upx, upy);
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }





    private void initDrawCanvas(){
        photoUri = Uri.parse(getIntent().getStringExtra("imageUri"));

        try {
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inJustDecodeBounds = true;
            InputStream inputStream = getContentResolver().openInputStream(photoUri);
            BitmapFactory.decodeStream(inputStream, null, bmpFactoryOptions);
            inputStream.close();

            bmpFactoryOptions.inSampleSize = calculateInSampleSize(bmpFactoryOptions, MAX_WIDTH, MAX_HEIGHT);

            bmpFactoryOptions.inJustDecodeBounds = false;
            inputStream = getContentResolver().openInputStream(photoUri);
            bmp = BitmapFactory.decodeStream(inputStream, null, bmpFactoryOptions);

            bmp = rotateImageIfRequired(bmp, photoUri);

            alteredBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());


            canvas = new Canvas(alteredBitmap);

            paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(5);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);

//            matrix = new Matrix();
            canvas.drawBitmap(bmp, 0, 0, paint);

            annotationImageView.setImageBitmap(alteredBitmap);
            annotationImageView.setOnTouchListener(this);
        } catch (Exception e) {
            Log.v("ERROR", e.toString());
        }
    }

    private void performCrop(Uri uri){

        //drawn bitmap
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        try {
            cbmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, bmpFactoryOptions);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        bmpFactoryOptions.inJustDecodeBounds = false;
        try {
            cbmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, bmpFactoryOptions);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //mask bitmap
        maskBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        maskBmp.eraseColor(Color.TRANSPARENT);
        Canvas maskCanvas = new Canvas(maskBmp);
        maskPaint = new Paint();
        maskPaint.setColor(Color.rgb(255,255,255));
        maskPaint.setStyle(Paint.Style.FILL);
        maskCanvas.drawPath(path, maskPaint);

        finalBitmap = stackMaskingProcess(cbmp, maskBmp);


        cropBitmap = Bitmap.createBitmap(finalBitmap, minValX-5, minValY-5, maxValX-minValX+10, maxValY-minValY+10);
        FileOutputStream fileOutputStream = null;

        String photoUriString = photoUri.toString();
        String[] splitUri = photoUriString.split("/");
        Random random = new Random();
        String croppedUri = Environment.getExternalStorageDirectory() + "/" + splitUri[6]+ "/" + splitUri[7]+ "/" + splitUri[7] + random.nextInt(100) +  "_cropped.png";

        File file = new File(Uri.parse(croppedUri).getPath());
        try {
            fileOutputStream = new FileOutputStream(file);
            cropBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap stackMaskingProcess(Bitmap _originalBitmap, Bitmap _maskingBitmap) {
        try {
            if (_originalBitmap != null)
            {
                int intWidth = _originalBitmap.getWidth();
                int intHeight = _originalBitmap.getHeight();
                resultMaskBitmap = Bitmap.createBitmap(intWidth, intHeight, Bitmap.Config.ARGB_8888);
                getMaskBitmap = Bitmap.createScaledBitmap(_maskingBitmap, intWidth, intHeight, true);
                Canvas mCanvas = new Canvas(resultMaskBitmap);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                mCanvas.drawBitmap(_originalBitmap, 0, 0, null);
                mCanvas.drawBitmap(getMaskBitmap, 0, 0, paint);
                paint.setXfermode(null);
                paint.setStyle(Paint.Style.STROKE);
            }
        } catch (OutOfMemoryError o) {
            o.printStackTrace();
        }
        return resultMaskBitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }


    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        Log.e("orientation", ""+orientation);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.close_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.close_button) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Do you want to close the window?");
            builder.setMessage("You are about to delete the image. Do you really want to proceed ?");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }
}
