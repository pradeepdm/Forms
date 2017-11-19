package com.forms;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView listView;
    private PersonAdapter adapter;

    private long formId;

    private Realm realm = Realm.getDefaultInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = (ListView) findViewById(R.id.listview);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if (!getIntent().hasExtra("id")) {
            finish();
        } else {
            formId = getIntent().getLongExtra("id", -1);

            RealmQuery<FormObject> formQuery = realm.where(FormObject.class)
                    .equalTo("id", formId);
            FormObject object = formQuery.findFirst();
            if (object != null) {
                setTitle(object.getName());
            }


            adapter = new PersonAdapter(this, android.R.layout.simple_list_item_1);
            listView.setAdapter(adapter);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ListActivity.this, FormActivity.class);
                    intent.putExtra("formId", formId);
                    startActivity(intent);
                }
            });
        }
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        RealmQuery<PersonObject> query = realm.where(PersonObject.class)
                .equalTo("formId", formId);
        RealmResults<PersonObject> results = query.findAll();

        adapter.clear();
        adapter.addAll(results);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(ListActivity.this, FormActivity.class);
        intent.putExtra("formId", formId);
        intent.putExtra("id", l);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Delete entry \"" + adapter.getItem(position).getTitle() + "\". Are you sure?")
                .setPositiveButton("No", null)
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.remove(adapter.getItem(position));
                        RealmQuery<PersonObject> query = realm.where(PersonObject.class)
                                .equalTo("id", id);
                        final RealmResults<PersonObject> results = query.findAll();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                results.deleteAllFromRealm();
                            }
                        });
                        //adapter.remove(adapter.getItem(position));
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();

        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
