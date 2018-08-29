package com.naveennaidu.opc;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CollectInformationActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1;
    EditText ageText;
    ImageButton cameraButton;
    ArrayList<String> diagnosisList = new ArrayList(Arrays.asList(new String[]{"Leukoplakia", "Erythroplakia", "Lichen Planus", "Traumatic Ulcer", "Other", "None"}));
    Spinner diagnosisSpinner;
    String doctor;
    Uri file;
    ArrayList<String> genderList = new ArrayList(Arrays.asList(new String[]{"Male", "Female"}));
    Spinner genderSpinner;
    String hospital;
    ArrayList<String> imageName = new ArrayList();
    ArrayList<Uri> imagesUri = new ArrayList();
    LinearLayout inHorizontalScrollView;
    EditText nameText;
    EditText otherText;
    String patientFolder;
    private String[] permissions = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA"};
    Uri photoUri;
    ProgressDialog progressDialog;
    File reDirect;
    ArrayList<String> resultList = new ArrayList(Arrays.asList(new String[]{"Biopsy needed", "Biopsy not needed"}));
    Spinner resultSpinner;
    Button submitButton;
    ArrayList<Uri> uploadedImageUri = new ArrayList<>();
    Boolean saved = false;
    Uri sessionUri;
    StorageReference storageReferenceProfilePic;
    String searchUid;
    ArrayList<Integer> uids = new ArrayList<>();

    ArrayAdapter gen;
    ArrayAdapter dia;
    ArrayAdapter re;
    String imageUris;
    AppDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_collect);
        setTitle("Patient Information");

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "patients-database")
                .fallbackToDestructiveMigration()
                .build();

        nameText = findViewById(R.id.name);
        ageText = findViewById(R.id.age);
        cameraButton = findViewById(R.id.cameraButton);
        submitButton = findViewById(R.id.submitButton);
        otherText = findViewById(R.id.otherEditText);
        otherText.setVisibility(View.GONE);
        hospital = getIntent().getStringExtra("hospital");
        doctor = getIntent().getStringExtra("doctor");

        if (getIntent().getExtras().containsKey("uid")){
            searchUid = getIntent().getStringExtra("uid");
            uids.add(Integer.parseInt(searchUid));
            final Integer[] arrUid = uids.toArray(new Integer[0]);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<Patient> patient = db.patientDao().loadAllByIds(arrUid);
                    nameText.setText(patient.get(0).getName());
                    ageText.setText(patient.get(0).getAge());
                    int genderPos = gen.getPosition(patient.get(0).getGender());
                    genderSpinner.setSelection(genderPos);

                    if (dia.getPosition(patient.get(0).getDiagnosis()) != -1){
                        int diaPos = dia.getPosition(patient.get(0).getDiagnosis());
                        diagnosisSpinner.setSelection(diaPos);
                    } else {
                        diagnosisSpinner.setSelection(4);
                        otherText.setText(patient.get(0).getDiagnosis());
                    }
                    int rePos = re.getPosition(patient.get(0).getResult());
                    resultSpinner.setSelection(rePos);
                    imageUris = patient.get(0).getImageUrls();
                    String subimageUris = imageUris.substring(1, imageUris.length()-1);
                    final String[] allImageUri = subimageUris.split(",");
                    for(int i=0; i<allImageUri.length;i++){
                        imagesUri.add(Uri.parse(allImageUri[i]));
                        final int finalI = i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("image uri", allImageUri[finalI]);
                                addImageView(inHorizontalScrollView, Uri.parse(allImageUri[finalI]));
                            }
                        });
                    }

                }
            }).start();
        }

        genderSpinner = findViewById(R.id.genderSpinner);
        gen = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, genderList);
        gen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(gen);

        diagnosisSpinner = findViewById(R.id.diagnosisSpinner);
        dia = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, diagnosisList);
        dia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        diagnosisSpinner.setAdapter(dia);

        resultSpinner = findViewById(R.id.resultSpinner);
        re = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, resultList);
        re.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resultSpinner.setAdapter(re);

        inHorizontalScrollView = findViewById(R.id.inscroll_images);
        progressDialog = new ProgressDialog(this);

        if (Build.VERSION.SDK_INT >= 23) {
            if (!arePermissionsEnabled()) {
                requestMultiplePermissions();
            }
        }
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!arePermissionsEnabled()) {
                        requestMultiplePermissions();
                    }

                    if (nameText.getText().toString().matches("") || ageText.getText().toString().matches("")) {
                        Toast.makeText(getApplicationContext(), "Name or Age is empty", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (getPackageManager() != null) {
                            File file = null;
                            try {
                                file = createImageFile(nameText.getText().toString(), ageText.getText().toString(), genderSpinner.getSelectedItem().toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            photoUri = null;
                            if (file != null) {
                                photoUri = Uri.fromFile(file);
                                imagesUri.add(photoUri);
                                cam.putExtra("output", photoUri);
                                startActivityForResult(cam, 1);
                            }
                        }
                    }
                }
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                String mainFolderName = "/OPC_" + hospital + "_" + doctor;
//                String subFolderName = nameText.getText().toString() + "_" + ageText.getText().toString() + "_"
//                        + genderSpinner.getSelectedItem().toString() == "Male" ? "M" : "F" + "_"
//                        + (otherText.getText().toString().matches("") ? diagnosisSpinner.getSelectedItem() : otherText.getText()).toString().replaceAll("\\s+", "")
//                        + "_" + resultSpinner.getSelectedItem().toString() == "Biopsy needed" ? "B" : "NB" ;
                Log.e("Image Uri", ""+imageUris);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Patient patient =new Patient();
                        patient.setName(nameText.getText().toString());
                        patient.setAge(ageText.getText().toString());
                        patient.setGender(genderSpinner.getSelectedItem().toString());
                        patient.setImageUrls(imagesUri.toString());
                        patient.setDiagnosis(diagnosisSpinner.getSelectedItem().toString() == "Other" ? otherText.getText().toString() : diagnosisSpinner.getSelectedItem().toString());
                        patient.setResult(resultSpinner.getSelectedItem().toString());
                        db.patientDao().insert(patient);
                    }
                }) .start();

                if (nameText.getText().toString().matches("") || ageText.getText().toString().matches("")) {
                    Toast.makeText(getApplicationContext(), "Name or Age is empty", Toast.LENGTH_SHORT).show();
                } else {
                    String fileName = Environment.getExternalStorageDirectory() + "/OPC_" + hospital + "_" + doctor + "/" + nameText.getText().toString() + "_" + ageText.getText().toString() + "_" + genderSpinner.getSelectedItem().toString();
                    File direct = new File(fileName);
                    Log.e("direct", otherText.getText().toString().matches("")+"");
                    if (direct.exists()) {
                        if (otherText.getText().toString().matches("")) {
                            String fullPath = Environment.getExternalStorageDirectory() + "/OPC_" + hospital + "_" + doctor + "/" + nameText.getText().toString() + "_" + ageText.getText().toString() + "_" + genderSpinner.getSelectedItem().toString() == "Male" ? "M" : "F" + "_" + diagnosisSpinner.getSelectedItem().toString().replaceAll("\\s+", "") + "_" + resultSpinner.getSelectedItem().toString() == "Biopsy needed" ? "B" : "NB";
                            Log.e("fullpath", fullPath);
                            reDirect = new File(fullPath);
//                            patientFolder = nameText.getText().toString() + "_" + ageText.getText().toString() + "_" + genderSpinner.getSelectedItem().toString() == "Male" ? "M" : "F"
//                                    + "_" + diagnosisSpinner.getSelectedItem().toString().replaceAll("\\s+", "") + "_"
//                                    + resultSpinner.getSelectedItem().toString() == "Biopsy needed" ? "B" : "NB";
                        } else {
                            String fullPath = Environment.getExternalStorageDirectory() + "/OPC_" + hospital + "_" + doctor + "/" + nameText.getText().toString()
                                    + "_" + ageText.getText().toString() + "_" + genderSpinner.getSelectedItem().toString() == "Male" ? "M" : "F"
                                    + "_" + diagnosisSpinner.getSelectedItem().toString().replaceAll("\\s+", "") + "_"
                                    + resultSpinner.getSelectedItem().toString() == "Biopsy needed" ? "B" : "NB";

                            reDirect = new File(fullPath);
                            patientFolder = nameText.getText().toString() + "_" + ageText.getText().toString() + "_" + genderSpinner.getSelectedItem().toString() == "Male" ? "M" : "F"
                                    + "_" + diagnosisSpinner.getSelectedItem().toString().replaceAll("\\s+", "") + "_"
                                    + resultSpinner.getSelectedItem().toString() == "Biopsy needed" ? "B" : "NB";
                        }
                        direct.renameTo(reDirect);
                    }
                }
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                nameText.setText("");
                ageText.setText("");
                inHorizontalScrollView.removeAllViews();
                genderSpinner.setSelection(0);
                diagnosisSpinner.setSelection(0);
                resultSpinner.setSelection(0);
                otherText.setText("");
                otherText.setVisibility(View.GONE);
                imagesUri.clear();
            }
        });

        diagnosisSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 4) {
                    otherText.setVisibility(View.VISIBLE);
                } else {
                    otherText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

//    private void uploadImage(String mainFolder, String patientFolder) {
//        storageReferenceProfilePic = FirebaseStorage.getInstance().getReference();
//        for (int i = 0; i < this.imagesUri.size(); i++) {
//            String name = mainFolder + "/" + patientFolder + "/" + imageName.get(i) + ".jpg";
//            final StorageReference ref = storageReferenceProfilePic.child(name);
//            UploadTask uploadTask = ref.putFile(imagesUri.get(i), new StorageMetadata.Builder().build(), sessionUri);
//
//            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    progressDialog.dismiss();
//                }
//            })
//            .addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    progressDialog.dismiss();
//                }
//            })
//            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//
//                    //displaying percentage in progress dialog
//                    progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
//                    sessionUri = taskSnapshot.getUploadSessionUri();
//                    if (sessionUri != null && !saved) {
//                        saved = true;
//                        // A persisted session has begun with the server.
//                        // Save this to persistent storage in case the process dies.
//                    }
//                }
//            });
//
//            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                @Override
//                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                    if (!task.isSuccessful()) {
//                        throw task.getException();
//                    }
//
//                    // Continue with the task to get the download URL
//                    return ref.getDownloadUrl();
//                }
//            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    if (task.isSuccessful()) {
//                        Uri downloadUri = task.getResult();
//                        uploadedImageUri.add(downloadUri);
//                        Log.e("URL", ""+uploadedImageUri);
//                    } else {
//                        // Handle failures
//                        // ...
//                    }
//                }
//            });
//
//        }
//    }

    private File createImageFile(String name, String age, String gender) throws IOException {
        String currentDateandTime = new SimpleDateFormat("dd-MM-yyyy:HH:mm:ss").format(new Date());
        String timeStamp = name + "_" + age + "_" + gender + "_" + currentDateandTime;
        imageName.add(timeStamp);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Environment.getExternalStorageDirectory());
        stringBuilder.append("/OPC_");
        stringBuilder.append(hospital);
        stringBuilder.append("_");
        stringBuilder.append(doctor);
        File direct = new File(stringBuilder.toString());
        if (!direct.exists()) {
            direct.mkdir();
        }
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(Environment.getExternalStorageDirectory());
        stringBuilder2.append("/OPC_");
        stringBuilder2.append(hospital);
        stringBuilder2.append("_");
        stringBuilder2.append(doctor);
        stringBuilder2.append("/");
        stringBuilder2.append(name);
        stringBuilder2.append("_");
        stringBuilder2.append(age);
        stringBuilder2.append("_");
        stringBuilder2.append(gender);
        File subFolder = new File(stringBuilder2.toString());
        if (!subFolder.exists()) {
            subFolder.mkdir();
        }
        return File.createTempFile(timeStamp, ".jpg", subFolder);
    }

    private void addImageView(LinearLayout layout, final Uri photoUri) {
        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setImageURI(photoUri);
        layout.addView(imageView);
        imageView.setPadding(5, 0, 5, 0);
        imageView.requestLayout();
        imageView.getLayoutParams().height = 300;
        imageView.getLayoutParams().width = 200;
        imageView.setClickable(true);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent("android.intent.action.VIEW");
//                intent.setDataAndType(photoUri, "image/*");
//                startActivity(intent);
                Intent goToAnnotation = new Intent(CollectInformationActivity.this, ImageAnnotationActivity.class);
                goToAnnotation.putExtra("imageUri", ""+photoUri);
                startActivityForResult(goToAnnotation, 100);

            }
        });
    }

    private boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && photoUri != null) {
            Intent goToAnnotation = new Intent(CollectInformationActivity.this, ImageAnnotationActivity.class);
            goToAnnotation.putExtra("imageUri", ""+photoUri);
            startActivityForResult(goToAnnotation, 100);
//            addImageView(inHorizontalScrollView, photoUri);
        }

        if (requestCode == 100 && resultCode == RESULT_OK){
//            String test = data.getStringExtra("image");
            addImageView(inHorizontalScrollView, photoUri);
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
