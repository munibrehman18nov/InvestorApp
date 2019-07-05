package com.example.user.investorapplication;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import Model.Driver;
import Model.Vehicle;
import butterknife.ButterKnife;
import butterknife.OnClick;
import stanford.androidlib.SimpleActivity;

public class iCategory2 extends SimpleActivity
{
    private boolean menu;
    private FirebaseDatabase db;
    private DatabaseReference users;

    private DatabaseReference vehicles;
    private ArrayList<Vehicle> vList;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor prefsEditor;
    private String investorId;
    private TextView tVU_inv_TotalVehicles;
    private long totalVehicles;
    private String investorName;
    private TextView invNameTVu;
    private ImageView changeProfile;
    private ImageView profileImageView;
    private Bitmap mBitMap;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i_category2);

        ButterKnife.bind(this);
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        vehicles = users.child("Vehicles");
        vList = new ArrayList<>();

        tVU_inv_TotalVehicles = (TextView) findViewById(R.id.totalVehicles);
        investorId = getIntent().getStringExtra("INVESTOR_ID").toString().trim();
        investorName = getIntent().getStringExtra("INVESTOR_NAME").toString().trim();
        totalVehicles = 0;
        invNameTVu = findViewById(R.id.invNameTVu);
        invNameTVu.setText(investorName);

        setSharedPreferences();
        setInvestorsVehicles();
        setTotalVehicles();
        saveDataToSharedPreferences();

        changeProfile = findViewById(R.id.changeProfile);
        changeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();

            }
        });


        menu = false;

    }

    public void pickImage()
    {
        CropImage.startPickImageActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //System.out.println("CHECK");
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            Uri imageUri = CropImage.getPickImageResultUri(this,data);
            cropRequest(imageUri);
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    profileImageView = findViewById(R.id.profileImageView);
                    String imagePath = saveToInternalStorage(bitmap);
                    setSharedPreferences();
                    prefsEditor.putString("imagePath"+investorId.trim(),imagePath.trim());
                    prefsEditor.commit();
                    StorageReference filePath = FirebaseStorage.getInstance().getReference();
                    //DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Investors_ProfilePictures");
                    uploadImage(resultUri, filePath);
                    mBitMap = bitmap;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadImage(final Uri resultUri, StorageReference filePath)
    {
        if(resultUri != null)
        {
            StorageReference fileRef = filePath.child(investorId+".jpg");
            fileRef.putFile(resultUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //dbRef.child(investorId).setValue(taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                            profileImageView.setImageBitmap(mBitMap);
                            toast("Profile Uploaded");
                        }
                    });
        }
        else
        {
            toast("No image selected");
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage)
    {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile"+investorId.trim()+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            //profileImageView.setImageBitmap(bitmapImage);
            //System.out.println("CHECK ME");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void loadImageFromStorage(String path)
    {

        try {
            File f=new File(path, "profile"+investorId+".jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            if(b!=null) {
                ImageView img = (ImageView) findViewById(R.id.profileImageView);
                img.setImageBitmap(b);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    private boolean getAndSetProfileImageFromFirebase()
    {
        profileImageView = findViewById(R.id.profileImageView);
        StorageReference filePath = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = filePath.child(investorId+".jpg");
        if(fileRef!=null)
        {
            GlideApp.with(this)
                    .load(fileRef)
                    .error(R.drawable.noprofile)
                    .into(profileImageView);

            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri)
                {
                    System.out.println("ON SUCCESS");
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(iCategory2.this.getContentResolver(), uri);
                        profileImageView.setImageBitmap(bitmap);
                        //System.out.println("BITMAP: " + bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String imagePath = saveToInternalStorage(bitmap);
                    setSharedPreferences();
                    prefsEditor.putString("imagePath"+investorId.trim(), imagePath.trim());
                    prefsEditor.commit();
                }
            });
            return true;
        }
        return false;
    }

    private void cropRequest(Uri imageUri)
    {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);
    }



    private void setSharedPreferences()
    {
        mPrefs = getApplicationContext().getSharedPreferences("mPrefs", 0); // 0 - for private mode
        prefsEditor = mPrefs.edit();
    }

    private void setInvestorsVehiclesList(DataSnapshot dataSnapshot)
    {
        int investorsTotalVehicles = 0;
        DataSnapshot vehiclesSS = dataSnapshot.child("Vehicles");
        Iterable<DataSnapshot> vehiclesChildren = vehiclesSS.getChildren();
        for(DataSnapshot temp : vehiclesChildren)
        {
            if(temp.child("owner").getValue().toString().trim().equals(investorId))
            {
                Vehicle v = new Vehicle(temp.child("id").getValue().toString().trim(), temp.child("owner").getValue().toString().trim(),temp.child("model").getValue().toString().trim(),temp.child("name").getValue().toString().trim(),temp.child("vin").getValue().toString().trim());
                vList.add(v);
                investorsTotalVehicles++;
            }
        }
        tVU_inv_TotalVehicles.setText(investorsTotalVehicles+"");
        prefsEditor.putString("investorsTotalVehicles",investorsTotalVehicles+"");
        prefsEditor.commit();

    }

    private void setInvestorsVehicles()
    {
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                setInvestorsVehiclesList(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setTotalVehicles()
    {

        vehicles.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    totalVehicles = (dataSnapshot.getChildrenCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        prefsEditor.putString("totalVehicles", totalVehicles +"");
        prefsEditor.commit();
    }

    private void saveDataToSharedPreferences()
    {
        Gson gson = new Gson();

        String json = gson.toJson(vList);
        prefsEditor.putString("vList", json);

        prefsEditor.putString("INVESTOR_ID",investorId);


        prefsEditor.commit();
    }





    @OnClick(R.id.vehicles_Activity)
    public void activity_Vehicles(View view)
    {
        Intent intent = new Intent(iCategory2.this, VehiclesActivity.class);
        startActivity(intent);
        //finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String imagePath = mPrefs.getString("imagePath"+investorId.trim(),"");
        if(!imagePath.equals("")) {
            loadImageFromStorage(imagePath);
        }

        String itv = mPrefs.getString("investorsTotalVehicles","").trim().toString();
        if(!itv.equals(""))
        {
            int investorsTotalVehicles = Integer.parseInt(itv);
            tVU_inv_TotalVehicles.setText(investorsTotalVehicles+"");
        }
    }
}
