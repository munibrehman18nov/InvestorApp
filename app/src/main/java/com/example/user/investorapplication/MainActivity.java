package com.example.user.investorapplication;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;

import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;
import stanford.androidlib.SimpleActivity;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends SimpleActivity
{


    @BindView(R.id.et_username) MaterialEditText et_username;
    @BindView(R.id.et_Password) MaterialEditText et_password;
    @BindView(R.id.btn_sign_in) Button btn_sign_in;
    @BindView(R.id.progressBar) ProgressBar progressBar;

    private FirebaseDatabase db;
    private DatabaseReference users;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

    }

    @OnClick(R.id.btn_sign_in)
    public void signinBtnClicked(View view)
    {
        final String id = et_username.getText().toString().trim();
        final String password = et_password.getText().toString().trim();

        if(id.isEmpty() || password.isEmpty())
        {
            toast("Please enter username and password");
        }
        progressBar.setVisibility(View.VISIBLE);
        //InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                ArrayList<User> al = new ArrayList<User>();
                DataSnapshot investorsSS = dataSnapshot.child("Investors");
                Iterable<DataSnapshot> investorsChildren = investorsSS.getChildren();

                for(DataSnapshot temp : investorsChildren)
                {
                    User u = new User();
                    u.setID(temp.child("id").getValue().toString().trim());
                    u.setPassword(temp.child("password").getValue().toString().trim());
                    u.setCategory(temp.child("category").getValue().toString().trim());
                    u.setName(temp.child("name").getValue().toString().trim());
                    al.add(u);
                }

                boolean isUserExist = false;
                if(!isUserExist) {
                    for (int i = 0; i < al.size(); i++) {
                        if (al.get(i).getID().equals(id) && al.get(i).getPassword().equals(password))
                        {
                            if (al.get(i).getCategory().equals("1"))
                            {
                                Intent intent = new Intent(MainActivity.this, iCategory1.class);
                                intent.putExtra("INVESTOR_ID", id);
                                intent.putExtra("INVESTOR_NAME", al.get(i).getName());
                                startActivity(intent);
                                progressBar.setVisibility(View.GONE);
                                //finish();
                                isUserExist = true;
                            }
                            else if (al.get(i).getCategory().equals("2"))
                            {
                                Intent intent = new Intent(MainActivity.this, iCategory2.class);
                                intent.putExtra("INVESTOR_ID", id);
                                intent.putExtra("INVESTOR_NAME", al.get(i).getName());
                                startActivity(intent);
                                progressBar.setVisibility(View.GONE);
                                //finish();
                                isUserExist = true;
                            } else if (al.get(i).getCategory().equals("3"))
                            {
                                Intent intent = new Intent(MainActivity.this, iCategory3.class);
                                intent.putExtra("INVESTOR_ID", id);
                                intent.putExtra("INVESTOR_NAME", al.get(i).getName());
                                startActivity(intent);
                                progressBar.setVisibility(View.GONE);
                                //finish();
                                isUserExist = true;
                            }

                        }
                    }
                }
                else
                {
                    progressBar.setVisibility(View.GONE);
                    toast("User not exists");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    public void editBoxClicked(View view)
    {
        /*CircleImageView imgvu = findViewById(R.id.logo);
        RelativeLayout cont1 = findViewById(R.id.cont1);
        ViewGroup.LayoutParams params = cont1.getLayoutParams();
        int h = getResources().getDimensionPixelSize(R.dimen._200sdp);
        params.height = h;
        cont1.setLayoutParams(params);
        imgvu.setVisibility(View.GONE);*/
    }

    /*public void signinClicked(View view)
    {
        CircleImageView imgvu = findViewById(R.id.logo);
        RelativeLayout cont1 = findViewById(R.id.cont1);
        ViewGroup.LayoutParams params = cont1.getLayoutParams();
        int h = getResources().getDimensionPixelSize(R.dimen._350sdp);
        params.height = h;
        cont1.setLayoutParams(params);
        imgvu.setVisibility(View.VISIBLE);
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }*/
}
