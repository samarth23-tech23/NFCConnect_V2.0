package com.example.nfcconnect;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

public class regActivity extends AppCompatActivity implements TextWatcher {
    EditText reguName, regemail, regPass, regnPass;
    Button rButton;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    OkHttpClient okHttpClient = new OkHttpClient();
    String encPassword, encNFCpass, Key, name, emailId;
    String password;
    String NFC_Password;
    String txtResponse;
    ProgressBar progressBar; // Declare ProgressBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        reguName = findViewById(R.id.uName);
        regemail = findViewById(R.id.eId);
        rButton = findViewById(R.id.regbtn2);
        regPass = findViewById(R.id.pId);
        regnPass = findViewById(R.id.npId);
        progressBar = findViewById(R.id.progressBar3); // Initialize ProgressBar

        progressBar.setVisibility(View.GONE); // Hide the progress bar initially

        rButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = reguName.getText().toString();
                emailId = regemail.getText().toString();

                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference("app");

                UserHelperClass helperClass = new UserHelperClass(name, emailId, encPassword, encNFCpass, Key);
                reference.child(name).setValue(helperClass);
                Toast.makeText(regActivity.this, "Successfully Registered with Name " + name, Toast.LENGTH_SHORT).show();
                reference.child(name).setValue(helperClass);

                // Show the progress spinner when the Register button is clicked
                progressBar.setVisibility(View.VISIBLE);

                regnPass.clearFocus();
            }
        });

        regPass.addTextChangedListener(this); // Set TextWatcher on the password EditText
    }

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
        // Perform the operation when the password text is modified
        password = regPass.getText().toString();
        NFC_Password = regnPass.getText().toString();

        // Show the progress spinner
        progressBar.setVisibility(View.VISIBLE);

        RequestBody formbody = new FormBody.Builder().add("password", password).add("nfcpassword", NFC_Password).add("activity", "register").build();
        Request request = new Request.Builder().url("https://karthik022.pythonanywhere.com/encrypt").post(formbody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Hide the spinner on failure
                       //    progressBar.setVisibility(View.GONE);
                        Toast.makeText(regActivity.this, "Flask response error", Toast.LENGTH_LONG).show();
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
                            JSONObject reader = new JSONObject(txtResponse);
                            encPassword = reader.getString("password");
                            encNFCpass = reader.getString("nfcpassword");
                            Key = reader.getString("key");

                            // Hide the spinner on successful response
                            progressBar.setVisibility(View.GONE);

                            // Continue with your code for a successful response
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
