package com.example.epal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private ImageButton mainExitBtn;
    private DatabaseReference expensesRef;
    private int totalMonthSavings = 0;
    private TextView mainProgress;
    private RecyclerView recyclerViewSavings;
    private CardView wishlistCardView;
    private CardView expensesCardView;
    private FloatingActionButton fabMain;
    private DatabaseReference savingsRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;
    private String post_key = "";
    private String SaveItem = "";
    private int SaveAmt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        savingsRef = FirebaseDatabase.getInstance().getReference().child("savings").child(mAuth.getCurrentUser().getUid());
        expensesRef = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        loader = new ProgressDialog(this);
        mainProgress = findViewById(R.id.mainProgress);
        recyclerViewSavings = findViewById(R.id.recyclerViewSavings);

        mainExitBtn = (ImageButton)findViewById(R.id.mainExitBtn);
        mainExitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerViewSavings.setHasFixedSize(true);
        recyclerViewSavings.setLayoutManager(linearLayoutManager);

        savingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmt = 0;
                for (DataSnapshot snap: snapshot.getChildren()){
                    SavingsData data = snap.getValue(SavingsData.class);
                    totalAmt += data.getSavingsAmt();

                }
                totalMonthSavings = totalAmt;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        expensesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmtExp = 0;
                for (DataSnapshot snap: snapshot.getChildren()){
                    ExpenseData data = snap.getValue(ExpenseData.class);
                    totalAmtExp += data.getExpAmount();
                }
                totalMonthSavings = totalMonthSavings - totalAmtExp;
                String totalS = String.valueOf("Php "+totalMonthSavings);
                mainProgress.setText(totalS);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        wishlistCardView = findViewById(R.id.wishlistCardView);
        expensesCardView = findViewById(R.id.expensesCardView);
        fabMain = findViewById(R.id.fabMain);
        
        fabMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                additem();
            }
        });

        wishlistCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WishlistActivity.class);
                startActivity(intent);
            }
        });

        expensesCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ExpensesActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        savingsRef = FirebaseDatabase.getInstance().getReference().child("savings").child(mAuth.getCurrentUser().getUid());
        expensesRef = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        savingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmt = 0;
                for (DataSnapshot snap: snapshot.getChildren()){
                    SavingsData data = snap.getValue(SavingsData.class);
                    totalAmt += data.getSavingsAmt();
                }
                totalMonthSavings = totalAmt;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        expensesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmtExp = 0;
                for (DataSnapshot snap: snapshot.getChildren()){
                    ExpenseData data = snap.getValue(ExpenseData.class);
                    totalAmtExp += data.getExpAmount();
                }
                totalMonthSavings = totalMonthSavings - totalAmtExp;
                String totalS = String.valueOf("Php "+totalMonthSavings);
                mainProgress.setText(totalS);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void additem() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.main_input_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final EditText amtTxt = myView.findViewById(R.id.amtTxt);
        final Button mainCancel = myView.findViewById(R.id.mainCancel);
        final Button mainAdd = myView.findViewById(R.id.mainAdd);

        mainAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemAmount = amtTxt.getText().toString();

                if(TextUtils.isEmpty(itemAmount)){
                    amtTxt.setError("Please input amount:");
                    return;
                }else{
                    loader.setMessage("Adding Savings Amount...");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    String savId = savingsRef.push().getKey();
                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Calendar cal = Calendar.getInstance();
                    String savDate = dateFormat.format(cal.getTime());

                    MutableDateTime epoch = new MutableDateTime();
                    epoch.setDate(0);
                    DateTime now = new DateTime();
                    Months months = Months.monthsBetween(epoch, now);

                    SavingsData data = new SavingsData(savDate, savId, null, Integer.parseInt(itemAmount), months.getMonths());
                    savingsRef.child(savId).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Savings Amount Added Successfully", Toast.LENGTH_SHORT).show();
                                savingsRef = FirebaseDatabase.getInstance().getReference().child("savings").child(mAuth.getCurrentUser().getUid());
                                expensesRef = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
                                savingsRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int totalAmt = 0;
                                        for (DataSnapshot snap: snapshot.getChildren()){
                                            SavingsData data = snap.getValue(SavingsData.class);
                                            totalAmt += data.getSavingsAmt();
                                        }
                                        totalMonthSavings = totalAmt;
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                expensesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int totalAmtExp = 0;
                                        for (DataSnapshot snap: snapshot.getChildren()){
                                            ExpenseData data = snap.getValue(ExpenseData.class);
                                            totalAmtExp += data.getExpAmount();
                                        }
                                        totalMonthSavings = totalMonthSavings - totalAmtExp;
                                        String totalS = String.valueOf("Php "+totalMonthSavings);
                                        mainProgress.setText(totalS);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }else{
                                Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                            loader.dismiss();
                        }
                    });

                }
                dialog.dismiss();

            }
        });

        mainCancel.setOnClickListener(new View.OnClickListener() {
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

        FirebaseRecyclerOptions<SavingsData> options = new FirebaseRecyclerOptions.Builder<SavingsData>().setQuery(savingsRef, SavingsData.class).build();

        FirebaseRecyclerAdapter<SavingsData, MyViewHolder> adapter = new FirebaseRecyclerAdapter<SavingsData, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull SavingsData model) {

                holder.setItemAmount("Allocated Amount: Php "+model.getSavingsAmt());
                holder.setDate("Added On: "+model.getSavDate());

                holder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        post_key = getRef(position).getKey();
                        SaveAmt = model.getSavingsAmt();
                        updateData();
                    }
                });
            }



            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.savingsretrieve_layout, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerViewSavings.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        View myView;
        public TextView notes, date;



        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myView = itemView;
            notes = itemView.findViewById(R.id.saveNotes);
            date = itemView.findViewById(R.id.SaveItemDate);

        }

        public void setItemAmount (String itemAmount){
            TextView amount = myView.findViewById(R.id.SavingAmount);
            amount.setText(itemAmount);
        }
        public void setDate (String itemDate){
            TextView date = myView.findViewById(R.id.SaveItemDate);
            date.setText(itemDate);
        }

    }
    private void updateData(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View mView = inflater.inflate(R.layout.main_update_layout, null);

        myDialog.setView(mView);
        final AlertDialog dialog = myDialog.create();

        final EditText mAmount = mView.findViewById(R.id.SavAmount);
        final EditText mNotes = mView.findViewById(R.id.savNotes);

        mNotes.setVisibility(View.GONE);

        mAmount.setText(String.valueOf(SaveAmt));
        mAmount.setSelection(String.valueOf(SaveAmt).length());

        Button SavDelBtn = mView.findViewById(R.id.SavingsDelBtn);
        Button SaveUpBtn = mView.findViewById(R.id.SavingsUpBtn);

        SaveUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SaveAmt = Integer.parseInt(mAmount.getText().toString());

                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Calendar cal = Calendar.getInstance();
                String savDate = dateFormat.format(cal.getTime());

                MutableDateTime epoch = new MutableDateTime();
                epoch.setDate(0);
                DateTime now = new DateTime();
                Months months = Months.monthsBetween(epoch, now);

                SavingsData data = new SavingsData(savDate, post_key, null, SaveAmt, months.getMonths());
                savingsRef.child(post_key).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                            savingsRef = FirebaseDatabase.getInstance().getReference().child("savings").child(mAuth.getCurrentUser().getUid());
                            expensesRef = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
                            savingsRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int totalAmt = 0;
                                    for (DataSnapshot snap: snapshot.getChildren()){
                                        SavingsData data = snap.getValue(SavingsData.class);
                                        totalAmt += data.getSavingsAmt();
                                    }
                                    totalMonthSavings = totalAmt;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            expensesRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int totalAmtExp = 0;
                                    for (DataSnapshot snap: snapshot.getChildren()){
                                        ExpenseData data = snap.getValue(ExpenseData.class);
                                        totalAmtExp += data.getExpAmount();
                                    }
                                    totalMonthSavings = totalMonthSavings - totalAmtExp;
                                    String totalS = String.valueOf("Php "+totalMonthSavings);
                                    mainProgress.setText(totalS);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else{
                            Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                dialog.dismiss();
            }
        });
        SavDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savingsRef.child(post_key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            savingsRef = FirebaseDatabase.getInstance().getReference().child("savings").child(mAuth.getCurrentUser().getUid());
                            expensesRef = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
                            savingsRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int totalAmt = 0;
                                    for (DataSnapshot snap: snapshot.getChildren()){
                                        SavingsData data = snap.getValue(SavingsData.class);
                                        totalAmt += data.getSavingsAmt();
                                    }
                                    totalMonthSavings = totalAmt;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            expensesRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int totalAmtExp = 0;
                                    for (DataSnapshot snap: snapshot.getChildren()){
                                        ExpenseData data = snap.getValue(ExpenseData.class);
                                        totalAmtExp += data.getExpAmount();
                                    }
                                    totalMonthSavings = totalMonthSavings - totalAmtExp;
                                    String totalS = String.valueOf("Php "+totalMonthSavings);
                                    mainProgress.setText(totalS);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else{
                            Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                dialog.dismiss();
            }
        });

        dialog.show();

    }

}