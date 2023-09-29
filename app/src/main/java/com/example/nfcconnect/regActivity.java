package com.example.nfcconnect;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Build;
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
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64; // Import Base64
import java.util.HashMap;
import java.util.Map;

public class regActivity extends AppCompatActivity implements TextWatcher {

    EditText reguName, regemail, regPass, regnPass;
    Button rButton;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    String encPassword, encNFCpass, name, emailId, aesKeyString; // Store the AES key as a string
    SecretKey aesKey;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        reguName = findViewById(R.id.uName);
        regemail = findViewById(R.id.eId);
        rButton = findViewById(R.id.regbtn2);
        regPass = findViewById(R.id.pId);
        regnPass = findViewById(R.id.npId);
        progressBar = findViewById(R.id.progressBar3);

        progressBar.setVisibility(View.GONE);

        // Generate the AES key once in the onCreate method
        try {
            aesKey = generateAESKey();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(regActivity.this, "AES Key generation error", Toast.LENGTH_SHORT).show();
        }

        rButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = reguName.getText().toString();
                emailId = regemail.getText().toString();
                String password = regPass.getText().toString();
                String NFC_Password = regnPass.getText().toString();
                if(!password.equals(NFC_Password)){
                    Toast.makeText(regActivity.this,"Passwords not matching",Toast.LENGTH_SHORT).show();
                }
                if(name.length()!=0 && emailId.length()!=0 && password.length()!=0 && NFC_Password.length()!=0){
                try {
                    // Use the same AES key for both password and NFC password encryption
                    encPassword = encryptAES(password, aesKey);
                    encNFCpass = encryptAES(NFC_Password, aesKey);

                    rootNode = FirebaseDatabase.getInstance();
                    reference = rootNode.getReference("app");

                    // Convert AES key to a Base64-encoded string
                    aesKeyString = encodeAESKeyToString(aesKey);

                    // Create a map to store the user data
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("name", name);
                    userData.put("emailId", emailId);
                    userData.put("encPassword", encPassword);
                    userData.put("encNFCpass", encNFCpass);
                    userData.put("aesKey", aesKeyString); // Store the AES key as a Base64 string

                    reference.child(name).setValue(userData);

                    Toast.makeText(regActivity.this, "Successfully Registered with Name " + name, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(regActivity.this, "Encryption error", Toast.LENGTH_SHORT).show();
                }
            }
                else{
                    Toast.makeText(regActivity.this,"Fill all the required fields",Toast.LENGHT_SHORT).show();
                }
            }
        );

        regPass.addTextChangedListener(this);
    }

    private SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128); // Use AES-128
        return keyGenerator.generateKey();
    }

    private String encryptAES(String plainText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder().encodeToString(encryptedBytes); // Convert the bytes to a Base64-encoded string
        }
        return null;
    }

    private String encodeAESKeyToString(SecretKey secretKey) {
        // Convert the SecretKey to a Base64-encoded string
        byte[] keyBytes = secretKey.getEncoded();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder().encodeToString(keyBytes);
        }
        return null;
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
    }
}
