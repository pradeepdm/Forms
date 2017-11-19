package com.forms;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created on 10/11/2016.
 */

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView listView;
    private FormsAdapter adapter;

    private Realm mRealm = Realm.getDefaultInstance();
    private ShowcaseView showcaseView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listview);

        adapter = new FormsAdapter(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateFormActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit or delete Form")
                .setMessage("Edit Form \"" + adapter.getItem(position).getName() + "\" including entries. Are you sure?")
                .setPositiveButton("Cancel", null)
                .setNeutralButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(MainActivity.this, CreateFormActivity.class);
                        intent.putExtra("id", id);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.remove(adapter.getItem(position));
//                        Clear form entries
                        RealmQuery<PersonObject> query = mRealm.where(PersonObject.class)
                                .equalTo("formId", id);
                        final RealmResults<PersonObject> results = query.findAll();
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                results.deleteAllFromRealm();
                            }
                        });
//                       Delete form
                        RealmQuery<FormObject> formQuery = mRealm.where(FormObject.class)
                                .equalTo("id", id);
                        final RealmResults<FormObject> formResults = formQuery.findAll();
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                formResults.deleteAllFromRealm();
                            }
                        });

                        dialog.dismiss();
                    }
                }).create();
        dialog.show();

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.clear();
        RealmQuery<FormObject> query = mRealm.where(FormObject.class);
        RealmResults<FormObject> results = query.findAll();
        adapter.addAll(results);
        if (adapter.getCount() < 1) {
            if (showcaseView == null) {
                showcaseView = new ShowcaseView.Builder(MainActivity.this)
                        .withHoloShowcase()
                        .doNotBlockTouches()
                        .hideOnTouchOutside()
                        .setContentText("Your Forms folder is empty.\nStart by creating a new form.")
                        .setContentTitle("Welcome!")
                        .setTarget(new ViewTarget(R.id.fab, MainActivity.this))
                        .build();
                RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lps.setMargins(50, 0, 0, 50);
                lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                showcaseView.setButtonPosition(lps);
            }
            showcaseView.show();
        }
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.global, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.action_new) {
//            Intent intent = new Intent(this, CreateFormActivity.class);
//            startActivity(intent);
//        }
//        return true;
//    }
}
