package com.example.nfcconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class reset extends AppCompatActivity {
    String name,oldP,newP,getPassword,getKey,txtResponse,txtResponse2,txtResponse3,encPassword,pass,username,encNFCpass,Key,combinedUP;
    TextView oldpass,newpass,uname;
    Button chkBtn,resetBtn;

    FirebaseDatabase rootNode;
    DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
    OkHttpClient okHttpClient=new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        Intent intent=getIntent();
        name=intent.getStringExtra("username");
        oldpass=findViewById(R.id.oldPass);
        newpass=findViewById(R.id.newPass);
        chkBtn=findViewById(R.id.chkBtn);
        resetBtn=findViewById(R.id.resetBtn);
        uname=findViewById(R.id.uName2);



        newpass.setVisibility(View.INVISIBLE);
        oldpass.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press

                  /*  if (oldP.isEmpty())
                        Toast.makeText(reset.this, "Please enter old password!", Toast.LENGTH_SHORT).show();
                    else {
                    }*/

                    username=uname.getText().toString();
                    oldP=oldpass.getText().toString();

                    //database fetching
                    reference.child("app").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(username)) {
                                getPassword = snapshot.child(username).child("password").getValue(String.class);
                                getKey = snapshot.child(username).child("key").getValue(String.class);


                                //flask request
                                RequestBody formbody = new FormBody.Builder().add("password", getPassword).add("enckey", getKey).build();
                                Request request = new Request.Builder().url("https://karthik022.pythonanywhere.com/dcrypt").post(formbody).build();

                                okHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(reset.this, "Flask response error", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {

                                                    txtResponse = (response.body().string());
                                                    Toast.makeText(reset.this, txtResponse, Toast.LENGTH_SHORT).show();
                                                    String in = txtResponse;
                                                    JSONObject reader = new JSONObject(in);
                                                    encPassword = reader.getString("password");
                                                    pass = encPassword.toString();
                                                    Toast.makeText(reset.this, pass, Toast.LENGTH_SHORT).show();
                                                } catch (IOException | JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                    }
                                });

                            } else {
                                Toast.makeText(reset.this, "Wrong Password !", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                    return true;
                }
                return false;
            }
        });



        newpass.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    newP=newpass.getText().toString();

                    RequestBody formbody2=new FormBody.Builder().add("activity","nfcreset").add("newnfcpassword",newP).build();
                    Request request2=new Request.Builder().url("https://karthik022.pythonanywhere.com/encrypt").post(formbody2).build();
                    Toast.makeText(getApplicationContext(), "Encrypting...", Toast.LENGTH_SHORT).show();
                    okHttpClient.newCall(request2).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Flask response error", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {


                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    try {
                                        txtResponse= (response.body().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    String in=txtResponse;
                                    try {
                                        JSONObject reader = new JSONObject(in);

                                        encNFCpass=reader.getString("newnfcpassword");
                                        Key=reader.getString("key");

                                        combinedUP=username+":"+encNFCpass;

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                    });

                    return true;
                }
                return false;
            }
        });





        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //For resetting
                    RequestBody formbody3 = new FormBody.Builder().add("resetpassword",combinedUP).add("activity", "reset").build();
                    Request request3 = new Request.Builder().url("https://raspi-nfcapi.socketxp.com/reset").post(formbody3).build();
                    Toast.makeText(reset.this, "Sending to Raspberry PI", Toast.LENGTH_SHORT).show();

                    okHttpClient.newCall(request3).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Socket response error", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    try {
                                        txtResponse2= (response.body().string());
                                        Toast.makeText(reset.this, txtResponse2, Toast.LENGTH_LONG).show();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                        }
                    });
                rootNode=FirebaseDatabase.getInstance();
                reference=rootNode.getReference("app");

                //Firebase
                UserHelperClass2 helperClass =new UserHelperClass2(username,encNFCpass,Key);
                reference.child(username).setValue(helperClass);

            }
        });


        chkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (oldP.equals(pass)) {
                    Toast.makeText(reset.this, "Valid password!", Toast.LENGTH_SHORT).show();
                    newpass.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(reset.this, "Not a valid password!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

}