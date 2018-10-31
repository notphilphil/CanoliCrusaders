package com.example.notphilphil.bob.controllers;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.example.notphilphil.bob.R;
import com.example.notphilphil.bob.models.Item;
import com.example.notphilphil.bob.models.Location;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class LocationDetailsActivity extends AppCompatActivity {
    private String key;
    private ArrayList<Item> items;
    private SearchView item_search;
    private Spinner category_spinner;
    private ArrayList<String> categories = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);
        key = this.getIntent().getStringExtra("key");
        items = new ArrayList<>();

        ListView item_list = findViewById(R.id.item_list);
        populateList(item_list, this);
        handleInfo(findViewById(R.id.info_btn), this);

        item_search = (SearchView) findViewById(R.id.item_search);
        category_spinner = findViewById(R.id.category_spinner);

        FloatingActionButton add_btn = findViewById(R.id.add_btn);
        add_btn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ModifyItemActivity.class);
            intent.putExtra("inventoryKey", key);
            intent.putExtra("edit", false);
            startActivityForResult(intent, 0);
        });
    }

    private void populateList(ListView item_list, Context context) {
        DatabaseReference ref = LoggedUser.getRef().child("inventories").child(key);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> names = new ArrayList<>();
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    Item newItem = new Item();
                    items.clear();
                    for (DataSnapshot val : item.getChildren()) {
                        newItem.addValue(val.getKey(), val.getValue().toString());
                    }
                    newItem.setKey(item.getKey());
                    items.add(newItem);
                    names.add(newItem.toString());
                    categories.add(newItem.getCategory().toLowerCase());
                }
                Set<String> categoriesWithoutDuplicates = new LinkedHashSet<String>(categories);
                categories.clear();
                categories.addAll(categoriesWithoutDuplicates);
                ArrayAdapter itemsAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, names);
                ArrayAdapter<String> categoryAdapter =
                        new ArrayAdapter<>(context, android.R.layout.simple_spinner_item,
                                categories);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                category_spinner.setAdapter(categoryAdapter);
                item_list.setAdapter(itemsAdapter);
                item_list.setOnItemClickListener(((parent, view, position, id) -> {
                    Intent intent = new Intent(context, ItemDetailsActivity.class);
                    intent.putExtra("itemKey", items.get(position).getKey());
                    intent.putExtra("inventoryKey", key);
                    startActivity(intent);
                }));



                item_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        //itemsAdapter.getFilter().filter(query);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        itemsAdapter.getFilter().filter(newText);

                        if (itemsAdapter.isEmpty()) {
                            Toast.makeText(LocationDetailsActivity.this, "No Match found", Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }

                });

                OnItemSelectedListener categorySelectedListener;
                category_spinner.setOnItemSelectedListener(categorySelectedListener = new Spinner.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> spinner, View container,
                                               int position, long id) {
                        itemsAdapter.getFilter().filter(category_spinner.getSelectedItem().toString());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {

                    }
                });
                category_spinner.setOnItemSelectedListener(categorySelectedListener);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Probably do nothing.
                Log.w("LocationDetailsLogs", databaseError.getDetails());
            }
        });
    }

    private void handleInfo(FloatingActionButton info_btn, Context context) {
        DatabaseReference ref = LoggedUser.getRef().child("locations").child(key);
        Log.d("LocationDetailsLogs", "Looking at "+key);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String body = "";
                String title = dataSnapshot.child("Name").getValue().toString();
                for (String token : Location.tokens) {
                    body += token + ": " + dataSnapshot.child(token).getValue().toString() + "\n";
                }
                String finalBody = body;
                info_btn.setOnClickListener(v -> {
                    new AlertDialog.Builder(context).setMessage(finalBody).setTitle(title).setCancelable(true).show();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
