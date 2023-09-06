package com.example.nfcconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
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

public class loginActivity extends AppCompatActivity {
    EditText mUsername, mPassword;
    Button mLoginBtn;
    ProgressBar progressBar;
    FirebaseDatabase rootNode;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    OkHttpClient okHttpClient = new OkHttpClient();
    String txtResponse, getKey;
    String encPassword, encNFCpass, getPassword, pass, uName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is already logged in using shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("session", MODE_PRIVATE);
        String username = sharedPreferences.getString("st", "");

        if (!username.isEmpty()) {
            // User is already logged in, redirect to Homepage
            Intent intent = new Intent(this, Homepage.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish(); // Finish loginActivity to prevent going back
        }

        setContentView(R.layout.activity_login);
        mUsername = findViewById(R.id.l_uName);
        mPassword = findViewById(R.id.l_password);
        mLoginBtn = findViewById(R.id.loginBtn);

        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called before the text is changed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called when the text is changed
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // This method is called after the text has changed
                String l_Name = mUsername.getText().toString().trim();
                uName = l_Name;
                reference.child("app").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(l_Name)) {
                            getPassword = snapshot.child(l_Name).child("password").getValue(String.class);
                            getKey = snapshot.child(l_Name).child("key").getValue(String.class);

                            // Build and send the network request for decryption
                            RequestBody formbody = new FormBody.Builder().add("password", getPassword).add("enckey", getKey).build();
                            Request request = new Request.Builder().url("https://karthik022.pythonanywhere.com/dcrypt").post(formbody).build();

                            okHttpClient.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(loginActivity.this, "Flask response error", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                txtResponse = response.body().string();
                                                Toast.makeText(loginActivity.this, txtResponse, Toast.LENGTH_SHORT).show();
                                                String in = txtResponse;
                                                JSONObject reader = new JSONObject(in);
                                                encPassword = reader.getString("password");
                                                pass = encPassword.toString();
                                            } catch (IOException | JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            });
                        } else {
                            Toast.makeText(loginActivity.this, "Wrong Username!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String l_password = mPassword.getText().toString();
                if (l_password.equals(pass)) {
                    // User is logged in successfully, save the session in shared preferences
                    SharedPreferences sharedPreferences = getSharedPreferences("session", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("st", uName);
                    editor.apply();

                    Toast.makeText(loginActivity.this, "Successfully logged in!", Toast.LENGTH_SHORT).show();

                    // Opening new Activity
                    Intent intent = new Intent(loginActivity.this, Homepage.class);
                    intent.putExtra("username", uName);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(loginActivity.this, "Wrong Password!", Toast.LENGTH_SHORT).show();
                }

                if (uName.isEmpty()) {
                    Toast.makeText(loginActivity.this, "Please enter username!", Toast.LENGTH_SHORT).show();
                }

                if (l_password.isEmpty()) {
                    Toast.makeText(loginActivity.this, "Please enter password!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
