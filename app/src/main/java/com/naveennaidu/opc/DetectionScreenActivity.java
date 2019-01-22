package com.naveennaidu.opc;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.ml.custom.model.FirebaseLocalModelSource;
import com.theartofdev.edmodo.cropper.CropImage;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class DetectionScreenActivity extends AppCompatActivity {

    private String[] permissions = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA"};


    ImageView cancerImageView;
    Button takeImage;
    TextView resultsTextView;
    Button predictButton;
    Uri resultUri;

    private static final int OPEN_CAMERA_CODE = 1;
    Uri selectedImage;

    FirebaseModelInterpreter firebaseInterpreter;
    FirebaseModelInputOutputOptions inputOutputOptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_detection);
        setTitle("Detect");

        FirebaseLocalModelSource localSource = new FirebaseLocalModelSource.Builder("oral_detector_local")  // Assign a name to this model
                        .setAssetFilePath("model.tflite")
                        .build();
        FirebaseModelManager.getInstance().registerLocalModelSource(localSource);

        FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                .setLocalModelName("oral_detector_local")
                .build();
        try {
            firebaseInterpreter = FirebaseModelInterpreter.getInstance(options);
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }

        try {
            inputOutputOptions = new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 224, 224, 3})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 2})
                            .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }

        cancerImageView = findViewById(R.id.cancerImageView);
        takeImage = findViewById(R.id.capture_detect);
        resultsTextView = findViewById(R.id.result_detect);
        predictButton = findViewById(R.id.predict_detect);

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= 23) {
            if (!arePermissionsEnabled()) {
                requestMultiplePermissions();
            }
        }

        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!arePermissionsEnabled()) {
                        requestMultiplePermissions();
                    }
                }

                performCrop(selectedImage);
            }
        });

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("resulturi", "onClick: " + resultUri );
                if (resultUri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                        predictButton.setVisibility(View.GONE);
                        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                        predict(bitmap);
                    } catch (IOException | FirebaseMLException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please Upload the Image", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    public void predict(final Bitmap bitmap) throws FirebaseMLException {

        Bitmap inbitmap = bitmap;
        inbitmap = Bitmap.createScaledBitmap(inbitmap, 224, 224, true);

        int batchNum = 0;
        float[][][][] input = new float[1][224][224][3];
        for (int x = 0; x < 224; x++) {
            for (int y = 0; y < 224; y++) {
                int pixel = inbitmap.getPixel(x, y);
                // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                // model. For example, some models might require values to be normalized
                // to the range [0.0, 1.0] instead.
                input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 128.0f;
                input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 128.0f;
                input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 128.0f;
            }
        }

        FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                .add(input)  // add() as many input arrays as your model requires
                .build();
        firebaseInterpreter.run(inputs, inputOutputOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseModelOutputs>() {
                            @Override
                            public void onSuccess(FirebaseModelOutputs result) {
                                // ...
                                float[][] output = result.getOutput(0);
                                float[] probabilities = output[0];
                                Log.e("prob", "onSuccess: " + probabilities.length );
                                for (int i = 0; i < probabilities.length; i++) {
                                    Log.e("MLKit", String.format("%1.4f", probabilities[i]));
                                }
                                String result_data = "Malignant with prob: " + String.format("%1.4f", probabilities[1]);
                                resultsTextView.setText(result_data);

                                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                                predictButton.setVisibility(View.VISIBLE);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                                Log.e("failed", "onFailure: " );
                            }
                        });
    }


    private void performCrop(Uri selectedImageUri){
        CropImage.activity(selectedImageUri)
                .setAspectRatio(1, 1)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == OPEN_CAMERA_CODE && resultCode == RESULT_OK){
            if (data != null) {
                selectedImage = data.getData();
                performCrop(selectedImage);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result != null) {
                resultUri = result.getUri();
                cancerImageView.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }


    @RequiresApi(api = 23)
    private boolean arePermissionsEnabled() {
        for (String permission : this.permissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != 0) {
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = 23)
    private void requestMultiplePermissions() {
        List<String> remainingPermissions = new ArrayList();
        for (String permission : this.permissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != 0) {
                remainingPermissions.add(permission);
            }
        }
        requestPermissions((String[]) remainingPermissions.toArray(new String[remainingPermissions.size()]), 101);
    }

    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != 0) {
                    if (shouldShowRequestPermissionRationale(permissions[i])) {
                        new AlertDialog.Builder(getApplicationContext())
                                .setMessage("Please allow access to both permissions to access this feature")
                                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        requestMultiplePermissions();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .create().show();
                    }
                    return;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.close_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.close_button) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


}
