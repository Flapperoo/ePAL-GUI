package com.example.epal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.Calendar;

public class WishlistActivity extends AppCompatActivity {

    private TextView savingsProgress;
    private RecyclerView recyclerView;
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
        savingsProgress = findViewById(R.id.savingsProgress);
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        wishlistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmt = 0;
                for (DataSnapshot snap: snapshot.getChildren()){
                    Data data = snap.getValue(Data.class);
                    totalAmt += data.getAmount();
                    String TotalW = String.valueOf("Total Savings: Php" + totalAmt);
                    savingsProgress.setText(TotalW);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


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

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>().setQuery(wishlistRef, Data.class).build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Data model) {
                holder.setItemAmount("Allocated Amount: Php" + model.getAmount());
                holder.setDate("On: "+model.getDate());
                holder.setItemName("Item is: "+model.getItem());


            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieve_layout, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        View myView;

        public ImageView imageView;
        public TextView itmProgress;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myView = itemView;
            imageView = itemView.findViewById(R.id.imageView);
            itmProgress = itemView.findViewById(R.id.itmProgress);

        }

        public void setItemName (String itemName){
            TextView item = myView.findViewById(R.id.itmName);
            item.setText(itemName);
        }

        public void setItemAmount (String itemAmount){
            TextView amount = myView.findViewById(R.id.itmPrice);
            amount.setText(itemAmount);
        }

        public void setDate (String itemDate){
            TextView date = myView.findViewById(R.id.itmDate);
        }


    }

}