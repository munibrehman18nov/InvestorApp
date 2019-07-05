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
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.io.InputStream;
import java.util.ArrayList;

import Model.Driver;
import Model.ImageData;
import Model.User;
import Model.Vehicle;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import stanford.androidlib.SimpleActivity;


public class iCategory1 extends SimpleActivity
{
    private boolean menu;
    private FirebaseDatabase db;
    private DatabaseReference users;
    private DatabaseReference drivers;
    private DatabaseReference vehicles;

    private ArrayList<Driver> dList;
    private ArrayList<Vehicle> vList;
    private long totalDrivers;
    private long totalVehicles;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor prefsEditor;
    private Gson gson;
    private String investorId;
    private String investorName;
    private TextView tVU_inv_Total_Drivers;
    private TextView tVU_inv_TotalVehicles;
    private TextView invNameTVu;
    private ImageView changeProfile;
    private ImageView profileImageView;
    final static int Gallery_Pick = 1;
    private Bitmap mBitMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i_category1);

        ButterKnife.bind(this);
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        drivers = users.child("Drivers");
        vehicles = users.child("Vehicles");

        dList = new ArrayList<>();
        vList = new ArrayList<>();
        totalDrivers = 0;
        totalVehicles = 0;

        changeProfile = findViewById(R.id.changeProfile);
        tVU_inv_Total_Drivers = (TextView) findViewById(R.id.totalDrivers);
        tVU_inv_TotalVehicles = (TextView) findViewById(R.id.totalVehicles);
        investorId = getIntent().getStringExtra("INVESTOR_ID").toString().trim();
        investorName = getIntent().getStringExtra("INVESTOR_NAME").toString().trim();

        invNameTVu = findViewById(R.id.invNameTVu);
        invNameTVu.setText(investorName);


        setSharedPreferences();
        prefsEditor.putString("INVESTOR_ID",investorId);
        prefsEditor.commit();
        gson = new Gson();
        setInvestorsVehiclesAndDrivers();
        setTotalDrivers();
        setTotalVehicles();

        menu = false;

        changeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();

            }
        });
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

    private boolean setAndSaveImgToInternalStorage()
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

            /*fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri)
                {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(iCategory1.this.getContentResolver(), uri);
                        System.out.println("ON SUCCESS::BITMAP: "+bitmap);
                        String imagePath = saveToInternalStorage(bitmap);
                        setSharedPreferences();
                        prefsEditor.putString("imagePath"+investorId.trim(),imagePath.trim());
                        prefsEditor.commit();
                        System.out.println("setAndSaveImgToInternalStorage:: "+imagePath);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });*/
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





    @Override
    protected void onResume() {
        super.onResume();

        String imagePath = mPrefs.getString("imagePath"+investorId.trim(),"");
        if(!imagePath.isEmpty())
        {
            //System.out.println("-Image found in Internal Storage-");
            loadImageFromStorage(imagePath);
        }
        else
        {
            //System.out.println("setAndSaveImgToInternalStorage::");
            setAndSaveImgToInternalStorage();
        }

        String itd = mPrefs.getString("investorsTotalDrivers","").trim().toString();
        String itv = mPrefs.getString("investorsTotalVehicles","").trim().toString();
        //System.out.println("iCategory1::onResume: "+itd+":"+itv);
        if(!itd.equals(""))
        {
            int investorsTotalDrivers = Integer.parseInt(itd);
            tVU_inv_Total_Drivers.setText(investorsTotalDrivers+"");
        }
        if(!itv.equals(""))
        {
            int investorsTotalVehicles = Integer.parseInt(itv);
            tVU_inv_TotalVehicles.setText(investorsTotalVehicles+"");
        }

    }

    @OnClick(R.id.drivers_Activity)
    public void activity_Drivers(View view)
    {
        Intent intent = new Intent(iCategory1.this, DriversActivity.class);
        startActivity(intent);
        //finish();
    }
    @OnClick(R.id.vehicles_Activity)
    public void activity_Vehicles(View view)
    {
        Intent intent = new Intent(iCategory1.this, VehiclesActivity.class);
        startActivity(intent);
        //finish();
    }









    private void setSharedPreferences()
    {
        mPrefs = getApplicationContext().getSharedPreferences("mPrefs", 0); // 0 - for private mode
        prefsEditor = mPrefs.edit();
    }

    private void setInvestorsDriversList(DataSnapshot dataSnapshot)
    {
        int investorsTotalDrivers = 0;
        DataSnapshot driversSS = dataSnapshot.child("Drivers");
        Iterable<DataSnapshot> driversChildren = driversSS.getChildren();
        for(DataSnapshot temp : driversChildren)
        {
            if(temp!=null)
            {
                if (temp.child("mgr").getValue().toString().trim().equals(investorId)) {
                    Driver d = new Driver(temp.child("id").getValue().toString().trim(), temp.child("name").getValue().toString().trim(), temp.child("mgr").getValue().toString().trim(), temp.child("cnic").getValue().toString().trim(), temp.child("mobile").getValue().toString().trim(), temp.child("address").getValue().toString().trim(), temp.child("email").getValue().toString().trim(),temp.child("online").getValue().toString().trim());
                    dList.add(d);
                    investorsTotalDrivers++;
                }
            }
        }
        //System.out.println("setInvestorsDriversList::DLIST: "+dList);
        tVU_inv_Total_Drivers.setText(investorsTotalDrivers+"");
        String json = gson.toJson(dList);
        prefsEditor.putString("dList", json);
        prefsEditor.putString("investorsTotalDrivers",investorsTotalDrivers+"");
        prefsEditor.commit();

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
        String json = gson.toJson(vList);
        prefsEditor.putString("vList", json);
        prefsEditor.putString("investorsTotalVehicles",investorsTotalVehicles+"");
        prefsEditor.commit();
        //System.out.println("iCategory1::setInvestorsVehiclesList::vList: "+vList.size());
        //for(int i=0;i<vList.size();i++)
        //{
        //   System.out.println("id:"+vList.get(i).getId()+", "+"name:"+vList.get(i).getName()+", "+"owner:"+vList.get(i).getOwner()+", "+"model:"+vList.get(i).getModel()+", "+"vin:"+vList.get(i).getVIN());
        //}

    }

    private void setInvestorsVehiclesAndDrivers()
    {
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                setInvestorsDriversList(dataSnapshot);
                setInvestorsVehiclesList(dataSnapshot);


                //System.out.println("iCategory1::setInvestorsVehiclesAndDrivers::Investors Total Drivers: "+ investorsTotalDrivers);
                //System.out.println("iCategory1::setInvestorsVehiclesAndDrivers::Total Drivers: "+ totalDrivers);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setTotalDrivers()
    {
        drivers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    totalDrivers = (dataSnapshot.getChildrenCount());
                    prefsEditor.putString("totalDrivers",totalDrivers+"");
                    prefsEditor.commit();
                }
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
                    prefsEditor.putString("totalVehicles",totalVehicles+"");
                    prefsEditor.commit();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



/*    @OnClick(R.id.menu_imgvu)
    public void menuClicked(View view)
    {
        if(menu==false) {
            NavigationView nav_vu = findViewById(R.id.nav_vu);
            nav_vu.setVisibility(View.VISIBLE);
            menu=true;
        }
        else
        {
            NavigationView nav_vu = findViewById(R.id.nav_vu);
            nav_vu.setVisibility(View.GONE);
            menu=false;
        }
    }*/
}
