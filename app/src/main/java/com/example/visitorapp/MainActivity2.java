package com.example.visitorapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {
    private String data = "", unit = "", contact = "", ic = "", username = "";
    private Date expire = new Date();
    private Date currTime = new Date();
    private Map<String, Object> visitor = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        TextView mTextView = findViewById(R.id.text);
        TextView text = findViewById(R.id.carplateText);
        EditText carplateno = findViewById(R.id.carplateData);
        Button submit = findViewById(R.id.carplateSubmit);
        mTextView.setVisibility(View.INVISIBLE);
        text.setVisibility(View.INVISIBLE);
        carplateno.setVisibility(View.INVISIBLE);
        submit.setVisibility(View.INVISIBLE);
        Intent dataintent = getIntent();
        if(dataintent != null)
            data = dataintent.getStringExtra("data");
        if(data.contains("Owner Contact: ") || data.contains("Unit: ") || data.contains("Owner IC: ") || data.contains("Owner Name: ") || data.contains("Expire time: ")) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            data = data.replaceAll("[a-zA-Z ]+[:][ ]", "");
            String[] datas = data.split("\n");
            unit = datas[0];
            List<String> units = Arrays.asList(unit.split(","));
            contact = datas[1];
            ic = datas[2];
            username = datas[3];
            expire.setTime(Long.parseLong(datas[4].trim()));
            if(currTime.before(expire)) {
                db.collection("visitor").whereEqualTo("username", username).whereEqualTo("expire_time", expire).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                visitor.put("unit", units);
                                visitor.put("contact", contact);
                                visitor.put("ic", ic);
                                visitor.put("username", username);
                                visitor.put("expire_time", expire);
                                visitor.put("checkin_time", new Date());
                                text.setVisibility(View.VISIBLE);
                                carplateno.setVisibility(View.VISIBLE);
                                submit.setVisibility(View.VISIBLE);
                                submit.setOnClickListener(view -> {
                                    if (carplateno.getText().toString().matches(""))
                                        visitor.put("carplate", null);
                                    else
                                        visitor.put("carplate", carplateno.getText().toString());
                                    visitor.put("status", "Check-In");
                                    db.collection("visitor").add(visitor).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task1) {
                                            success();
                                        }
                                    });
                                });
                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.get("status").toString().equals("Check-In")) {
                                        Map<String, Object> updateData = new HashMap<>();
                                        updateData.put("checkout_time", new Date());
                                        updateData.put("status", "Check-Out");
                                        db.collection("visitor").document(document.getId()).update(updateData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                success();
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                });
            }
            else
            {
                mTextView.setVisibility(View.VISIBLE);
                mTextView.setText("QR Code Expired");
            }
        }
        else {
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText("Invalid QR Code");
        }
    }
    void success(){
        Intent intent = new Intent(MainActivity2.this,MainActivity3.class);
        startActivity(intent);
    }
}