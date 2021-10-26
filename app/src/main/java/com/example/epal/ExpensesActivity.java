package com.example.epal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

public class ExpensesActivity extends AppCompatActivity {

    private ImageButton backBtnExp;
    private TextView expensesProgress;
    private RecyclerView recyclerViewExp;
    private FloatingActionButton fabExp;
    private DatabaseReference expensesRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;
    private String post_key = "";
    private String expenseitem = "";
    private int expamount = 0;
    private String expNotes = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);


        mAuth = FirebaseAuth.getInstance();
        expensesRef = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        loader = new ProgressDialog(this);
        expensesProgress = findViewById(R.id.expensesProgress);
        recyclerViewExp = findViewById(R.id.recyclerViewExp);

        backBtnExp = (ImageButton)findViewById(R.id.backBtnExp);
        backBtnExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpensesActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerViewExp.setHasFixedSize(true);
        recyclerViewExp.setLayoutManager(linearLayoutManager);

        expensesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmtExp = 0;
                for (DataSnapshot snap: snapshot.getChildren()){
                    ExpenseData data = snap.getValue(ExpenseData.class);
                    totalAmtExp += data.getExpAmount();
                    String TotalE = String.valueOf("Total Expenses: Php " + totalAmtExp);
                    expensesProgress.setText(TotalE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        fabExp = findViewById(R.id.fabExp);

        fabExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                additem();
            }

        });

    }

    private void additem() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myview = inflater.inflate(R.layout.expense_layout, null);
        myDialog.setView(myview);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final EditText linkTxtExp = myview.findViewById(R.id.linkTxtExp);
        final EditText priceTxtExp = myview.findViewById(R.id.priceTxtExp);
        final EditText notesTxtExp = myview.findViewById(R.id.notesTxtExp);
        final Button cancelExpBtn = myview.findViewById(R.id.cancelExpBtn);
        final Button saveExpBtn = myview.findViewById(R.id.saveExpBtn);

        saveExpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String expItem = linkTxtExp.getText().toString();
                String itemAmount = priceTxtExp.getText().toString();
                String expNotes = notesTxtExp.getText().toString();

                if(TextUtils.isEmpty(expItem)){
                    linkTxtExp.setError("Please input Item:");
                    return;
                }
                if(TextUtils.isEmpty(itemAmount)){
                    priceTxtExp.setError("Please input amount:");
                    return;
                }

                else{
                    loader.setMessage("Expenses Item Added Successfully");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    String expId = expensesRef.push().getKey();
                    DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
                    Calendar cal = Calendar.getInstance();
                    String expDate = dateFormat.format(cal.getTime());

                    MutableDateTime epoch = new MutableDateTime();
                    epoch.setDate(0);
                    DateTime now = new DateTime();
                    Months months = Months.monthsBetween(epoch, now);


                    ExpenseData data = new ExpenseData(expItem, expDate, expId, expNotes, Integer.parseInt(itemAmount), months.getMonths());
                    expensesRef.child(expId).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ExpensesActivity.this, "Spent Item is Added Successfully", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(ExpensesActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }

                            loader.dismiss();
                        }
                    });

                }
                dialog.dismiss();

            }
        });
        cancelExpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ExpenseData> options = new FirebaseRecyclerOptions.Builder<ExpenseData>().setQuery(expensesRef, ExpenseData.class).build();

        FirebaseRecyclerAdapter<ExpenseData, MyViewHolder> adapter = new FirebaseRecyclerAdapter<ExpenseData, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull ExpenseData model) {
                holder.setItemAmount("Item Price: Php "+ model.getExpAmount());
                holder.setDate("Date Added: "+model.getExpDate());
                holder.setItemName("Item Name: "+model.getExpItem());
                holder.setNotes("Notes: "+model.getExpNotes());

                holder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        post_key = getRef(position).getKey();
                        expenseitem = model.getExpItem();
                        expamount = model.getExpAmount();
                        expNotes = model.getExpNotes();
                        updateData();
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expretrieve_layout, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerViewExp.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    private void updateData() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View mView = inflater.inflate(R.layout.expupdate_layout, null);

        myDialog.setView(mView);
        final AlertDialog dialog = myDialog.create();
        final TextView mItem = mView.findViewById(R.id.expitemName);
        final EditText mAmount = mView.findViewById(R.id.expamount);
        final EditText mNotes = mView.findViewById(R.id.expnote);

        mNotes.setVisibility(View.VISIBLE);

        mItem.setText(expenseitem);
        mAmount.setText(String.valueOf(expamount));
        mAmount.setSelection(String.valueOf(expamount).length());

        Button delBtn = mView.findViewById(R.id.deleteBtnExp);
        Button upBtn = mView.findViewById(R.id.updateBtnExp);

        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expamount = Integer.parseInt(mAmount.getText().toString());
                expNotes = mNotes.getText().toString();

                DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
                Calendar cal = Calendar.getInstance();
                String expDate = dateFormat.format(cal.getTime());

                MutableDateTime epoch = new MutableDateTime();
                epoch.setDate(0);
                DateTime now = new DateTime();
                Months months = Months.monthsBetween(epoch, now);

                ExpenseData data = new ExpenseData(expenseitem, expDate, post_key, expNotes, expamount, months.getMonths());
                expensesRef.child(post_key).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(ExpensesActivity.this, "Item was Updated Successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(ExpensesActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.dismiss();

            }
        });

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expensesRef.child(post_key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(ExpensesActivity.this, "Wishlist Item was Deleted Successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(ExpensesActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();

            }
        });



        dialog.show();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        View myView;

        public ImageView imageView;
        public TextView itmProgress, expitmDate, expitmNotes;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myView = itemView;
            imageView = itemView.findViewById(R.id.imageView);
            itmProgress = itemView.findViewById(R.id.itmProgress);
            expitmDate = itemView.findViewById(R.id.expitmDate);
            expitmNotes = itemView.findViewById(R.id.expitmNotes);

        }

        public void setItemName (String itemName){
            TextView item = myView.findViewById(R.id.expitmName);
            item.setText(itemName);
        }

        public void setItemAmount (String itemAmount){
            TextView amount = myView.findViewById(R.id.expitmPrice);
            amount.setText(itemAmount);
        }

        public void setDate (String itemDate){
            TextView date = myView.findViewById(R.id.expitmDate);
            date.setText(itemDate);
        }

        public void setNotes (String SavNotes){
            TextView notes = myView.findViewById(R.id.expitmNotes);
            notes.setText(SavNotes);
        }



    }
}