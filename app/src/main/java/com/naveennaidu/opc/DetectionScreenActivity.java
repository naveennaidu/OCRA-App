package com.naveennaidu.opc;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class DetectionScreenActivity extends AppCompatActivity {

    //Load the tensorflow inference library
    static {
        System.loadLibrary("tensorflow_inference");
    }

    //PATH TO OUR MODEL FILE AND NAMES OF THE INPUT AND OUTPUT NODES
    private String MODEL_PATH = "file:///android_asset/squeezenet.pb";
    private String INPUT_NAME = "input_1";
    private String OUTPUT_NAME = "output_1";
    private TensorFlowInferenceInterface tf;

    ImageView cancerImageView;
    Button takeImage;
    TextView resultsTextView;
    Button predictButton;
    ProgressBar progressBar;
    Uri resultUri;

    private static final int OPEN_CAMERA_CODE = 1;
    Uri selectedImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_detection);
        setTitle("Detect");

        cancerImageView = findViewById(R.id.cancerImageView);
        takeImage = findViewById(R.id.capture_detect);
        resultsTextView = findViewById(R.id.result_detect);
        predictButton = findViewById(R.id.predict_detect);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performCrop(selectedImage);
            }
        });

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resultUri != null) {
                    Toast.makeText(getApplicationContext(), "Please Upload the Image", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void performCrop(Uri selectedImageUri){
        CropImage.activity(selectedImageUri)
                .setMinCropResultSize(220,220)
                .setMaxCropResultSize(224,224)
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


}
