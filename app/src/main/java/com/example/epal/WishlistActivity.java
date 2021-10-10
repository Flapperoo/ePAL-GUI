package com.example.epal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.Calendar;

public class WishlistActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private DatabaseReference wishlistRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        mAuth = FirebaseAuth.getInstance();
        wishlistRef = FirebaseDatabase.getInstance().getReference().child("wishlist").child(mAuth.getCurrentUser().getUid());
        loader = new ProgressDialog(this);

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                additem();
            }

        });

    }

    private void additem(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myview = inflater.inflate(R.layout.wishlist_layout, null);
        myDialog.setView(myview);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final EditText linkText = myview.findViewById(R.id.linkText);
        final EditText printTxt = myview.findViewById(R.id.linkText);
        final Button cancelBtn = myview.findViewById(R.id.cancelBtn);
        final Button saveBtn = myview.findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String wishlistLink = linkText.getText().toString();
                String itemAmount = printTxt.getText().toString();

                if(TextUtils.isEmpty(wishlistLink)){
                    linkText.setError("Please input link:");
                    return;
                }
                if(TextUtils.isEmpty(wishlistLink)){
                    printTxt.setError("Please input link:");
                    return;
                }

                else{
                    loader.setMessage("Adding Wishlist Item...");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    String id = wishlistRef.push().getKey();
                    DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
                    Calendar cal = Calendar.getInstance();
                    String date = dateFormat.format(cal.getTime());

                    MutableDateTime epoch = new MutableDateTime();
                    epoch.setDate(0);
                    DateTime now = new DateTime();
                    Months months = Months.monthsBetween(epoch, now);

                    Data data = new Data(itemAmount, date, id, null, wishlistLink,months.getMonths());
                    wishlistRef.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(WishlistActivity.this, "Item added successfully to wishlist", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(WishlistActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                            loader.dismiss();
                        }
                    });
                }
                dialog.dismiss();

            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

}