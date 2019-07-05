package com.example.user.investorapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import Model.Driver;
import Model.Vehicle;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomVehicleAdapter extends BaseAdapter
{
    private FirebaseDatabase db;
    private DatabaseReference users;
    private DatabaseReference vehicles;
    private Gson gson;
    private SharedPreferences.Editor prefsEditor;
    private String investorId;
    //private long totalVehicles = 0;

    private Activity act;
    private Context context;
    //private TextView v_idVU;
    private TextView v_nameVU;
    private TextView v_modelVU;
    private TextView model;
    private TextView name;
    private SharedPreferences mPrefs;
    private ArrayList<Vehicle>vList;
    private LinearLayout deleteVehicle;

    public CustomVehicleAdapter(Context context, Activity actCont) throws JSONException
    {
        //ButterKnife.bind(this);
        vList = new ArrayList<>();
        setSharedPreferences(context);
        setVehiclesList();
        this.context = context;
        act = actCont;
        gson = new Gson();
        //ButterKnife.bind(DriversActivity.this);
    }

    @Override
    public View getView(final int i, final View view, ViewGroup viewGroup)
    {
        View v = View.inflate(context, R.layout.layout_vehicle_custom, null);
        v_nameVU = v.findViewById(R.id.vNameVU);
        v_modelVU = v.findViewById(R.id.vModelVU);

        v_nameVU.setText(vList.get(i).getName().toString());
        v_modelVU.setText(vList.get(i).getModel().toString());
        deleteVehicle = v.findViewById(R.id.btn_deleteVehicle);
        //System.out.println("CustomVehicleAdapter::getView: "+i);

        deleteVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ImageView dImgVu = v.findViewById(R.id.delete_imgVu);
                dImgVu.setColorFilter(v.getResources().getColor(R.color.red));

                deleteVehicleBtnClicked(i);
                updateVList();

                Intent intent = new Intent(context, VehiclesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                act.finish();


            }
        });
        return v;
    }

    private void deleteVehicleBtnClicked(final int index)
    {
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        vehicles = users.child("Vehicles");
        vehicles.child(vList.get(index).getId()).child("owner").setValue("1000");
        ArrayList<Vehicle> vListNew = new ArrayList<>();
        for(int i=0; i<vList.size(); i++)
        {
            if(vList.get(i) != vList.get(index))
            {
                vListNew.add(vList.get(i));
            }
        }
        String json = gson.toJson(vListNew);
        prefsEditor.putString("vList", json);
        prefsEditor.commit();

    }

    private void updateVList()
    {
        gson = new Gson();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        vehicles = users.child("Vehicles");
        vList = new ArrayList<>();
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                setVList(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setVList(DataSnapshot dataSnapshot)
    {
        getDataFromSharedPreferences();
        int investorsTotalVehicles=0;
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
        //System.out.println("setInvestorsDriversList::DLIST: "+dList);
        String json = gson.toJson(vList);
        prefsEditor.putString("vList", json);
        prefsEditor.putString("investorsTotalVehicles", investorsTotalVehicles+"");
        prefsEditor.commit();
        System.out.println("CustomVehicleAdapter::setVList: "+investorsTotalVehicles);
    }


    private void setSharedPreferences(Context context)
    {
        mPrefs = context.getSharedPreferences("mPrefs", 0); // 0 - for private mode
        prefsEditor = mPrefs.edit();
    }
    private void getDataFromSharedPreferences()
    {
        investorId = mPrefs.getString("INVESTOR_ID", "").trim();
    }


    private void setVehiclesList() throws JSONException
    {
        Gson gson = new Gson();
        String json = mPrefs.getString("vList", "");
        JSONArray JArr = new JSONArray(json);
        for(int i=0; i<JArr.length(); i++)
        {
            JSONObject JObj = (JSONObject) JArr.get(i);
            String id = JObj.getString("Id").toString().trim();
            String name = JObj.getString("name").toString().trim();
            String owner = JObj.getString("owner").toString().trim();
            String model = JObj.getString("Model").toString().trim();
            //String vin = JObj.getString("vin").toString().trim();
            String vin = "";
            //System.out.println("CustomVehicleAdapter::setVehiclesList::JObj :"+JObj);
            Vehicle v = new Vehicle(id,owner,model,name,vin);
            vList.add(v);
        }
    }

    @Override
    public int getCount()
    {
        return vList.size();
    }

    @Override
    public Object getItem(int i) {
        return vList.get(i).getId().toString();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


}
