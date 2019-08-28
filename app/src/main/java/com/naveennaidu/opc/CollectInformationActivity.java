package com.naveennaidu.opc;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectInformationActivity extends AppCompatActivity {

    EditText nameText;
    EditText ageText;
    Button cameraButton;
    ArrayList<String> genderList = new ArrayList(Arrays.asList(new String[]{"Male", "Female", "Other"}));
    Spinner genderSpinner;

    String doctor;
    Uri file;
    String hospital;

    ArrayList<String> imageName = new ArrayList();
    ArrayList<Uri> imagesUri = new ArrayList();
    LinearLayout inHorizontalScrollView;

    private String[] permissions = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA"};
    Uri photoUri;
    ProgressDialog progressDialog;
    File reDirect;

    Button submitButton;
    String searchUid;
    ArrayList<Integer> uids = new ArrayList<>();

    ArrayList<String> uploadedImageUri = new ArrayList<>();
    Boolean saved = false;
    Uri sessionUri;
    StorageReference storageReferenceProfilePic;

    ArrayAdapter gen;
    String genderMini;

    FirebaseFirestore db;

    CheckBox cb1, cb2, cb3, cb4, cb5, cb6, cb7, cb8;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_collect);
        setTitle("Patient Information");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());


        nameText = findViewById(R.id.name);
        ageText = findViewById(R.id.age);
        cameraButton = findViewById(R.id.cameraButton);
        submitButton = findViewById(R.id.submitButton);
        hospital = getIntent().getStringExtra("hospital");
        doctor = getIntent().getStringExtra("doctor");

        cb1 = findViewById(R.id.checkBox_tobocco);
        cb2 = findViewById(R.id.checkBox_betel);
        cb3 = findViewById(R.id.checkBox_smoking);
        cb4 = findViewById(R.id.checkBox_alcohol);
        cb5 = findViewById(R.id.checkBox_hpv);
        cb6 = findViewById(R.id.checkBox_lesion_history);
        cb7 = findViewById(R.id.checkBox_artificial_tooth);
        cb8 = findViewById(R.id.checkBox_family_history);

        if (getIntent().getExtras().containsKey("uid")){
            searchUid = getIntent().getStringExtra("uid");
            uids.add(Integer.parseInt(searchUid));
            final Integer[] arrUid = uids.toArray(new Integer[0]);
        }

        genderSpinner = findViewById(R.id.genderSpinner);
        gen = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, genderList);
        gen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(gen);

        nameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                nameText.removeTextChangedListener(this);
                nameText.setSelection(editable.length()); //moves the pointer to end
                nameText.addTextChangedListener(this);
            }
        });

        ageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ageText.removeTextChangedListener(this);
                ageText.setSelection(editable.length()); //moves the pointer to end
                ageText.addTextChangedListener(this);
            }
        });


        inHorizontalScrollView = findViewById(R.id.inscroll_images);
        progressDialog = new ProgressDialog(this);

        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

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

                    if (nameText.getText().toString().matches("")) {
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
                genderMini = genderSpinner.getSelectedItem().toString() == "Male" ? "M" : "F";
                String mainFolderName = "/OPC_" + hospital + "_" + doctor;
                String subFolderName = nameText.getText().toString() + "_" + ageText.getText().toString() + "_" + genderMini;

                if (nameText.getText().toString().matches("") || ageText.getText().toString().matches("")) {
                    Toast.makeText(getApplicationContext(), "Name or Age is empty", Toast.LENGTH_SHORT).show();
                }
//                else {
//                    String fileName = Environment.getExternalStorageDirectory() + mainFolderName + "/" + nameText.getText().toString() + "_" + ageText.getText().toString() + "_" + genderSpinner.getSelectedItem().toString();
//                    File direct = new File(fileName);
//                    if (direct.exists()) {
//                        String fullPath = Environment.getExternalStorageDirectory() + mainFolderName + "/" + subFolderName;
//                        reDirect = new File(fullPath);
//                        direct.renameTo(reDirect);
//                    }
//                }
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();

                uploadImage(mainFolderName, subFolderName, nameText.getText().toString(), ageText.getText().toString(), genderMini);



                nameText.setText("");
                ageText.setText("");
                inHorizontalScrollView.removeAllViews();
                genderSpinner.setSelection(0);
                if (cb1.isChecked()) {
                    cb1.setChecked(false);
                }
                if (cb2.isChecked()) {
                    cb2.setChecked(false);
                }
                if (cb2.isChecked()) {
                    cb2.setChecked(false);
                }
                if (cb3.isChecked()) {
                    cb3.setChecked(false);
                }
                if (cb4.isChecked()) {
                    cb4.setChecked(false);
                }
                if (cb5.isChecked()) {
                    cb5.setChecked(false);
                }
                if (cb6.isChecked()) {
                    cb6.setChecked(false);
                }
                if (cb7.isChecked()) {
                    cb7.setChecked(false);
                }
                if (cb8.isChecked()) {
                    cb8.setChecked(false);
                }
                imagesUri.clear();
            }
        });

    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkBox_tobocco:
                if (checked) {

                } else {

                    break;
                }
            case R.id.checkBox_betel:
                if (checked) {

                } else {

                    break;
                }
            case R.id.checkBox_smoking:
                if (checked) {

                } else {

                    break;
                }
            case R.id.checkBox_alcohol:
                if (checked) {

                } else {

                    break;
                }
            case R.id.checkBox_hpv:
                if (checked) {

                } else {

                    break;
                }
            case R.id.checkBox_lesion_history:
                if (checked) {

                } else {

                    break;
                }
            case R.id.checkBox_artificial_tooth:
                if (checked) {

                } else {

                    break;
                }
            case R.id.checkBox_family_history:
                if (checked) {

                } else {

                    break;
                }
        }
    }


    private void uploadImage(String mainFolder, String patientFolder, final String patientName, final String age, final String gender) {
        storageReferenceProfilePic = FirebaseStorage.getInstance().getReference();
        for (int i = 0; i < this.imagesUri.size(); i++) {
            String name = mainFolder + "/" + patientFolder + "/" + imageName.get(i) + ".jpg";
            final StorageReference ref = storageReferenceProfilePic.child(name);
            UploadTask uploadTask = ref.putFile(imagesUri.get(i), new StorageMetadata.Builder().build(), sessionUri);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                }
            })
            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    //displaying percentage in progress dialog
                    progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    sessionUri = taskSnapshot.getUploadSessionUri();
                    if (sessionUri != null && !saved) {
                        saved = true;
                        // A persisted session has begun with the server.
                        // Save this to persistent storage in case the process dies.
                    }
                }
            });

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        assert downloadUri != null;
                        uploadedImageUri.add(downloadUri.toString());
                        Log.e("Name", ""+patientName);
                        Log.e("URL", ""+uploadedImageUri);
                        uploadToDatabase(patientName, age, gender, uploadedImageUri);

                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });

        }
    }

    private void uploadToDatabase(String name, String age, String gender, ArrayList urls){


        PatientModel patient = new PatientModel(name, age, gender, urls);

//        Map<String, Object> patient = new HashMap<>();
//        patient.put("name", name);
//        patient.put("age", age);
//        patient.put("gender", gender);
//        patient.put("phone", phone);
//        patient.put("imageUrls", urls);

//        Log.e("upload", "uploadToDatabase:" + name.toUpperCase() +age);

        db.collection("patientstest").document(name.toUpperCase() +age)
                .set(patient)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("dataset", "DocumentSnapshot added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("dataset", "Error adding document", e);
                        Toast.makeText(getApplicationContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private File createImageFile(String name, String age, String gender) throws IOException {
        String currentDateandTime = new SimpleDateFormat("dd-MM-yyyy:HH:mm:ss").format(new Date());
        String timeStamp = name + "_" + age + "_" + gender + "_" + currentDateandTime;
        imageName.add(timeStamp);

        File direct = new File(Environment.getExternalStorageDirectory() + "/OPC_" + hospital + "_" + doctor);
        if (!direct.exists()) {
            direct.mkdir();
        }

        File subFolder = new File(Environment.getExternalStorageDirectory() + "/OPC_" + hospital + "_" + doctor + "/" + name + "_" + age + "_" + gender);
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
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndType(photoUri, "image/*");
                startActivity(intent);

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
        }

        if (requestCode == 100 && resultCode == RESULT_OK){
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
            if (!nameText.getText().toString().matches("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Do you want to close the window?");
                builder.setMessage("You are about to delete all records of form. Do you really want to proceed ?");
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
            } else {
                finish();
            }

        }
        return super.onOptionsItemSelected(item);
    }
}
