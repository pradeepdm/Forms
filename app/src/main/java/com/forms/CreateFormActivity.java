package com.forms;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;

public class CreateFormActivity extends AppCompatActivity implements View.OnLongClickListener {

    public static final int FIELD_EMAIL = 0;
    public static final int FIELD_DOB = 1;
    public static final int FIELD_PHONE = 2;
    public static final int FIELD_LINE = 3;
    public static final int FIELD_MULTILINE = 4;
    public static final int FIELD_CHECKBOX = 5;
    public static final int FIELD_TOGGLE = 6;
    public static final int FIELD_CURRENCY = 7;
    public static final int FIELD_COUNTRY = 8;
    public static final int FIELD_SEX = 9;
    public static final int FIELD_TERMS = 10;
    public static final int FIELD_SIGNATURE = 11;
    public static final int FIELD_RADIO = 12;
    public static final int FIELD_DROPDOWN = 13;
    public static final int FIELD_DATE = 14;
    public static final int FIELD_TIME = 15;

    private static final String[] SEX = {"M", "F"};
    private static final String[] CURRENCY = {"$", "¢", "£", "¤", "¥", "ƒ", "", "৲", "₹"};
    private static final String[] COUNTRY = {"Malaysia", "United States", "Indonesia", "France", "Italy", "Singapore", "New Zealand", "India", "Portugal"};

    private FormObject formObject;
    private LinearLayout parent;


    private List<FieldObjects> extras = new ArrayList<>();
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        parent = (LinearLayout) findViewById(R.id.parent);

        if (getIntent().hasExtra("id")) {
            long existingFormId = getIntent().getLongExtra("id", 0);
            loadForm(existingFormId);
        } else {
            init();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        AlertDialog confirmation = new AlertDialog.Builder(this)
                .setTitle("Discard Form")
                .setMessage("Discard Form. Are you sure?")
                .setPositiveButton("No", null)
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        CreateFormActivity.super.onBackPressed();
                    }
                }).create();
        confirmation.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        boolean firstTimeUser = preferences.getBoolean("first_time", true);
        if (firstTimeUser) {
            startTour();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("first_time", false);
            editor.apply();
        }
    }

    @Override
    public boolean onLongClick(final View view) {
        final EditText editText = new EditText(this);
        editText.setHint("Set new name to field");
        final AlertDialog editDialog = new AlertDialog.Builder(this)
                .setTitle("Edit Field Name")
                .setView(editText)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        FieldObjects objects = (FieldObjects) view.getTag();
                        if (view instanceof EditText) {
//region editText
                            EditText e = (EditText) view;
                            ((TextInputLayout) e.getParent().getParent()).setHint(editText.getText());
                            if (objects != null) {
                                objects.setTitle(editText.getText().toString());
                            }
//endregion
                        } else if (view instanceof LinearLayout) {
//region spinner
                            if (objects != null && (objects.getType() == FIELD_SEX || objects.getType() == FIELD_COUNTRY || objects.getType() == FIELD_CURRENCY || objects.getType() == FIELD_SIGNATURE || objects.getType() == FIELD_DROPDOWN)) {
                                if (objects.getType() != FIELD_DROPDOWN) {
                                    TextView textView = (TextView) ((LinearLayout) view).getChildAt(0);
                                    textView.setText(editText.getText());
                                    objects.setTitle(editText.getText().toString());
                                }
                            }
//endregion
                        } else if (view instanceof SwitchCompat) {
//region switch
                            SwitchCompat switchCompat = (SwitchCompat) view;
                            switchCompat.setTextOn(editText.getText());
                            switchCompat.setText(editText.getText());
                            switchCompat.setTextOff(editText.getText());
                            if (objects != null) {
                                objects.setTitle(editText.getText().toString());
                            }
//endregion
                        } else if (view instanceof CheckBox) {
//region checkbox
                            CheckBox checkBox = (CheckBox) view;
                            checkBox.setText(editText.getText());
                            if (objects != null) {
                                objects.setTitle(editText.getText().toString());
                            }
//endregion
                        } else if (view instanceof RadioButton) {
//region checkbox
                            RadioButton radioButton = (RadioButton) view;
                            radioButton.setText(editText.getText());
                            if (objects != null) {
                                objects.setTitle(editText.getText().toString());
                            }
//endregion
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();

        final FieldObjects objects = (FieldObjects) view.getTag();
        CharSequence[] items = null;
        if (objects.getType() == FIELD_LINE || objects.getType() == FIELD_MULTILINE) {
            items = new CharSequence[]{"Edit Name", "Toggle Required", "Move Up", "Move Down", "Delete", "Map To OCR Field"};
        } else {
            items = new CharSequence[]{"Edit Name", "Toggle Required", "Move Up", "Move Down", "Delete"};
        }

        CharSequence[] ocr = {"LN", "FN", "Address", "Hair", "Eyes"};

//        final AlertDialog mapDialog = new AlertDialog.Builder(this)
//                .setTitle("Map to OCR Field")
//                .setItems(ocr, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        FieldObjects objects = (FieldObjects) view.getTag();
//                        String[] default_types = {"default_ln", "default_fn", "default_address", "default_hair", "default_eye"};
//                        if (objects.getType() == FIELD_LINE && which == 2) {
//                            Toast.makeText(CreateFormActivity.this, "Invalid mapping.", Toast.LENGTH_SHORT).show();
//                        } else if (objects.getType() == FIELD_MULTILINE && which != 2) {
//                            Toast.makeText(CreateFormActivity.this, "Invalid mapping.", Toast.LENGTH_SHORT).show();
//                        } else {
//                            objects.setKnown_type(default_types[which]);
//                            dialog.dismiss();
//                        }
//                    }
//                }).setNegativeButton("Cancel", null)
//                .create();

        AlertDialog optionsDialog = new AlertDialog.Builder(this)
                .setTitle("Edit Field")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            editDialog.show();
                        } else if (which == 1) {
                            FieldObjects objects = (FieldObjects) view.getTag();
                            objects.setRequired(!objects.isRequired());
                        } else if (which == 2) {
                            FieldObjects objects = (FieldObjects) view.getTag();
                            int pos = extras.indexOf(objects);
                            if (pos > 0) {
                                View top = parent.getChildAt(pos - 1);
                                FieldObjects topObjects = extras.get(pos - 1);

                                View current = parent.getChildAt(pos);
                                FieldObjects currentObjects = extras.get(pos);

                                parent.removeView(top);
                                parent.removeView(current);

                                extras.remove(topObjects);
                                extras.remove(currentObjects);

                                extras.add(pos - 1, currentObjects);
                                extras.add(pos, topObjects);


                                parent.addView(current, pos - 1);
                                parent.addView(top, pos);

                            }
                        } else if (which == 3) {
                            FieldObjects objects = (FieldObjects) view.getTag();
                            int pos = extras.indexOf(objects);
                            if (pos < extras.size() - 1) {
                                View bottom = parent.getChildAt(pos + 1);
                                FieldObjects bottomObjects = extras.get(pos + 1);

                                View current = parent.getChildAt(pos);
                                FieldObjects currentObjects = extras.get(pos);

                                extras.remove(bottomObjects);
                                extras.remove(currentObjects);

                                parent.removeView(bottom);
                                parent.removeView(current);

                                extras.add(pos, bottomObjects);
                                extras.add(pos + 1, currentObjects);


                                parent.addView(bottom, pos);
                                parent.addView(current, pos + 1);

                            }
                        } else if (which == 4) {
                            FieldObjects objects = (FieldObjects) view.getTag();
                            extras.remove(objects);
                            if (view instanceof EditText) {
                                parent.removeView(((TextInputLayout) view.getParent().getParent()));
                            } else if (view instanceof RadioButton) {
                                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
                                LinearLayout radioLayout = (LinearLayout) findViewById(R.id.linearLayoutForRadio);
                                if (radioGroup != null) {
                                    radioGroup.removeView(view);
                                    if (radioLayout != null && radioGroup.getChildCount() == 0) {
                                        radioLayout.removeView(radioGroup);
                                        parent.removeView(radioLayout);
                                    }
                                }
                            } else {
                                parent.removeView(view);
                            }
                        } else if (which == 5) {
                            showMapDialog(view, objects);
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton("Cancel", null)
                .setCancelable(true).create();
        optionsDialog.show();
        return true;
    }

    private void showMapDialog(final View view, final FieldObjects objects) {
        CharSequence[] ocr = {};
        String[] default_types = {};
        if(objects.getType() == FIELD_LINE) {
            ocr = new CharSequence[]{"LN", "FN", "Hair", "Eyes"};
            default_types = new String[]{"default_ln", "default_fn", "default_hair", "default_eye"};

        } else if(objects.getType() == FIELD_MULTILINE) {
            ocr = new CharSequence[]{"address"};
            default_types = new String[]{"default_address"};
        }
        final String[] finalDefault_types = default_types;
        final AlertDialog mapDialog = new AlertDialog.Builder(this)
                .setTitle("Map to OCR Field")
                .setItems(ocr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean exists = isExisting(finalDefault_types[which]);
                        if (exists) {
                            Toast.makeText(CreateFormActivity.this, "Mapping already exists.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }
                        FieldObjects objects = (FieldObjects) view.getTag();
                        objects.setKnown_type(finalDefault_types[which]);
                        dialog.dismiss();
                    }
                }).setNegativeButton("Cancel", null)
                .create();
        mapDialog.show();
    }

    private boolean isExisting(String s) {
        for(FieldObjects o : extras) {
            if(o.getKnown_type() != null && o.getKnown_type().equals(s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveForm();
        } else if (item.getItemId() == R.id.action_tour) {
            startTour();
        } else if (item.getItemId() == R.id.action_save_as) {
            if (formObject != null)
                saveAs();
            else
                Toast.makeText(this, "Invalid operation.", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.action_edit) {
            if (formObject != null)
                renameForm();
            else
                Toast.makeText(this, "Invalid operation.", Toast.LENGTH_SHORT).show();
        }
        switch (item.getItemId()) {
            case R.id.action_email:
                FieldObjects email = new FieldObjects(FIELD_EMAIL, "Email", null);
                addEmail(email);
                break;
            case R.id.action_phone:
                FieldObjects phone = new FieldObjects(FIELD_PHONE, "Phone", null);
                addPhone(phone);
                break;
            case R.id.action_dob:
                DateFormat format = SimpleDateFormat.getDateTimeInstance();
                FieldObjects dob = new FieldObjects(FIELD_DOB, "Date", format.format(new Date()));
                addDOB(dob);
                break;
            case R.id.action_single:
                FieldObjects line = new FieldObjects(FIELD_LINE, "Input", null);
                addInput(line);
                break;
            case R.id.action_multiline:
                FieldObjects multi = new FieldObjects(FIELD_MULTILINE, "Multiline Input", null);
                addMultiInput(multi);
                break;
            case R.id.action_check:
                FieldObjects check = new FieldObjects(FIELD_CHECKBOX, "Checkbox", String.valueOf(false));
                addCheckbox(check);
                break;
            case R.id.action_toggle:
                FieldObjects toggle = new FieldObjects(FIELD_TOGGLE, "Switch", String.valueOf(false));
                addToggle(toggle);
                break;
            case R.id.action_country:
                FieldObjects country = new FieldObjects(FIELD_COUNTRY, "Country", COUNTRY[0]);
                addCountry(country);
                break;
            case R.id.action_currency:
                FieldObjects currency = new FieldObjects(FIELD_CURRENCY, "Currency", CURRENCY[0]);
                addCurrency(currency);
                break;

            case R.id.action_terms:
                FieldObjects terms = new FieldObjects(FIELD_TERMS, "Terms and Conditions", String.valueOf(false));
                addTerms(terms);
                break;
            case R.id.action_signature:
                String base64 = encodeToBase64(BitmapFactory.decodeResource(getResources(), R.drawable.signature_placeholder));
                FieldObjects signature = new FieldObjects(FIELD_SIGNATURE, "Signature", base64);
                addSignature(signature);
                break;
            case R.id.action_radios:
                FieldObjects radio = new FieldObjects(FIELD_RADIO, "Radio", null);
                addRadioButtons(radio);
                break;
            case R.id.action_dropdown:
                FieldObjects dropDown = new FieldObjects(FIELD_DROPDOWN, "dropdown,", null);
                addSpinner(dropDown);
                break;
            case R.id.action_date:
                FieldObjects date = new FieldObjects(FIELD_DATE, "Click here to set the date", null);
                addDatePicker(date);
                break;
            case R.id.action_time:
                FieldObjects time = new FieldObjects(FIELD_TIME, "Click here to set the time", null);
                addTimePicker(time);
                break;
        }
        return true;
    }


    private void init() {

        FieldObjects ln = new FieldObjects(FIELD_LINE, "LN", null);
        ln.setKnown_type("default_ln");
        addInput(ln);

        FieldObjects fn = new FieldObjects(FIELD_LINE, "FN", null);
        fn.setKnown_type("default_fn");
        addInput(fn);

        FieldObjects sex = new FieldObjects(FIELD_SEX, "SEX", SEX[0]);
        addSex(sex);

        FieldObjects hair = new FieldObjects(FIELD_LINE, "HAIR", null);
        hair.setKnown_type("default_hair");
        addInput(hair);

        FieldObjects eye = new FieldObjects(FIELD_LINE, "EYES", null);
        eye.setKnown_type("default_eye");
        addInput(eye);

        FieldObjects address = new FieldObjects(FIELD_MULTILINE, "ADDRESS", null);
        address.setKnown_type("default_address");
        addMultiInput(address);
    }

    private void addEmail(final FieldObjects objects) {
        TextInputLayout layout = new TextInputLayout(this);
        TextInputEditText editText = new TextInputEditText(this);
        editText.setHint(objects.getTitle());
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText.setText(objects.getValue());
        layout.addView(editText);


        editText.setTag(objects);
        extras.add(objects);

//        editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                objects.setValue(editable.toString());
//            }
//        });
        editText.setOnLongClickListener(this);
        parent.addView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addPhone(final FieldObjects objects) {
        TextInputLayout layout = new TextInputLayout(this);
        TextInputEditText editText = new TextInputEditText(this);
        editText.setHint(objects.getTitle());
        editText.setText(objects.getValue());
        editText.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(editText);

//        FieldObjects objects = new FieldObjects(FIELD_PHONE, "Phone", "");
        editText.setTag(objects);
        extras.add(objects);

//        editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                objects.setValue(editable.toString());
//            }
//        });
        editText.setOnLongClickListener(this);
        parent.addView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addDOB(final FieldObjects objects) {
        TextInputLayout layout = new TextInputLayout(this);
        TextInputEditText editText = new TextInputEditText(this);
        editText.setHint(objects.getTitle());
        editText.setText(objects.getValue());
        editText.setInputType(InputType.TYPE_CLASS_DATETIME);
        layout.addView(editText);

//        FieldObjects objects = new FieldObjects(FIELD_DOB, "Date", "");
        editText.setTag(objects);
        extras.add(objects);

//        editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                objects.setValue(editable.toString());
//            }
//        });
        editText.setOnLongClickListener(this);
        parent.addView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addInput(final FieldObjects objects) {
        TextInputLayout layout = new TextInputLayout(this);
        TextInputEditText editText = new TextInputEditText(this);
        editText.setHint(objects.getTitle());
        editText.setText(objects.getValue());
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setMaxLines(1);
        layout.addView(editText);
        editText.setTag(objects);
        extras.add(objects);

//        editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                objects.setValue(editable.toString());
//            }
//        });
        editText.setOnLongClickListener(this);
        parent.addView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addMultiInput(final FieldObjects objects) {
        TextInputLayout layout = new TextInputLayout(this);
        TextInputEditText editText = new TextInputEditText(this);
        editText.setHint(objects.getTitle());
        editText.setText(objects.getValue());
        layout.addView(editText);
        editText.setTag(objects);
        extras.add(objects);

        editText.setOnLongClickListener(this);
//        editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                objects.setValue(editable.toString());
//            }
//        });
        parent.addView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addToggle(final FieldObjects objects) {

        SwitchCompat switchCompat = new SwitchCompat(this);
        switchCompat.setText(objects.getTitle());
        switchCompat.setTextOff(objects.getTitle());
        switchCompat.setTextOn(objects.getTitle());
        switchCompat.setChecked(Boolean.valueOf(objects.getValue()));

        switchCompat.setOnLongClickListener(this);
        switchCompat.setTag(objects);
        extras.add(objects);

//        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                objects.setValue(String.valueOf(isChecked));
//            }
//        });


        parent.addView(switchCompat, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addCheckbox(final FieldObjects objects) {
//        CheckBox checkBox = new CheckBox(this);
//        checkBox.setText(objects.getTitle());
//        checkBox.setChecked(Boolean.valueOf(objects.getValue()));
//
//        checkBox.setOnLongClickListener(this);
//        checkBox.setTag(objects);
//        extras.add(objects);
//
////        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
////            @Override
////            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
////                objects.setValue(String.valueOf(isChecked));
////            }
////        });
//
//        parent.addView(checkBox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextInputLayout title = new TextInputLayout(this);

        String[] all = objects.getTitle().split(",");

        final TextInputEditText titleEditText = new TextInputEditText(this);
        titleEditText.setMaxLines(1);
        titleEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        titleEditText.setHint("Title");
        if (all.length > 0) {
            titleEditText.setText(all[0]);
        }
        title.addView(titleEditText);

        final TextInputLayout value = new TextInputLayout(this);
        final TextInputEditText editText = new TextInputEditText(this);
        editText.setMaxLines(1);
        titleEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setHint("Comma separated values");
        String[] values = Arrays.copyOfRange(all, 1, all.length);
        editText.setText(TextUtils.join(",", values));
        value.addView(editText);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                objects.setTitle(titleEditText.getText().toString() + "," + editText.getText().toString());
            }
        });

        linearLayout.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(value, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setOnLongClickListener(this);
        linearLayout.setTag(objects);
        extras.add(objects);

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addCountry(final FieldObjects objects) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(this);
        textView.setText(objects.getTitle());

        AppCompatSpinner spinner = new AppCompatSpinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, COUNTRY);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(objects.getValue()));

        linearLayout.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(spinner, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        linearLayout.setTag(objects);
        linearLayout.setOnLongClickListener(this);
        extras.add(objects);

//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                objects.setValue(COUNTRY[position]);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addCurrency(final FieldObjects objects) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(this);
        textView.setText(objects.getTitle());

        AppCompatSpinner spinner = new AppCompatSpinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CURRENCY);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(objects.getValue()));

        linearLayout.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(spinner, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        linearLayout.setTag(objects);
        linearLayout.setOnLongClickListener(this);
        extras.add(objects);

//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                objects.setValue(CURRENCY[position]);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addSex(final FieldObjects objects) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(this);
        textView.setText(objects.getTitle());

        AppCompatSpinner spinner = new AppCompatSpinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SEX);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(objects.getValue()));

        linearLayout.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(spinner, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        linearLayout.setTag(objects);
        linearLayout.setOnLongClickListener(this);
        extras.add(objects);

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }


    private void addTerms(final FieldObjects objects) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        final EditText editText = new EditText(this);
        editText.setHint("Input terms and conditions");
        editText.setText(objects.getTitle());
        editText.setLines(4);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                objects.setTitle(editText.getText().toString());
            }
        });

        CheckBox checkBox = new CheckBox(this);
        checkBox.setText("Accept");
        checkBox.setChecked(Boolean.valueOf(objects.getValue()));

//        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                objects.setValue(String.valueOf(isChecked));
//            }
//        });

        linearLayout.addView(editText, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(checkBox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        extras.add(objects);

        linearLayout.setTag(objects);
        linearLayout.setOnLongClickListener(this);

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addSignature(final FieldObjects objects) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(this);
        textView.setText(objects.getTitle());

        ImageView imageView = new ImageView(this);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageBitmap(decodeBase64(objects.getValue()));

//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(CreateFormActivity.this, SignatureInputActivity.class);
//                startActivityForResult(intent, 300);
//            }
//        });
        linearLayout.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(imageView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        extras.add(objects);

        linearLayout.setTag(objects);
        linearLayout.setOnLongClickListener(this);

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    }


//    private void addRadioButtons(final FieldObjects objects) {
//        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutForRadio);
//        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
//        boolean addlayout = false;
//        if (linearLayout == null) {
//            addlayout = true;
//            linearLayout = new LinearLayout(this);
//            linearLayout.setId(R.id.linearLayoutForRadio);
//            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
//
//            radioGroup = new RadioGroup(this);
//            radioGroup.setId(R.id.radioGroup);
//            linearLayout.addView(radioGroup, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        }
//
//        RadioButton radioButton = new RadioButton(this);
//        radioButton.setText(objects.getTitle());
//        radioButton.setChecked(Boolean.valueOf(objects.getValue()));
//
//        radioButton.setOnLongClickListener(this);
//        radioButton.setTag(objects);
//
//        radioGroup.addView(radioButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//        extras.add(objects);
//        if (addlayout) {
//            parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        }
//    }

    private void addRadioButtons(final FieldObjects objects) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextInputLayout title = new TextInputLayout(this);

        String[] all = objects.getTitle().split(",");

        final TextInputEditText titleEditText = new TextInputEditText(this);
        titleEditText.setMaxLines(1);
        titleEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        titleEditText.setHint("Title");
        if (all.length > 0) {
            titleEditText.setText(all[0]);
        }
        title.addView(titleEditText);

        final TextInputLayout value = new TextInputLayout(this);
        final TextInputEditText editText = new TextInputEditText(this);
        editText.setMaxLines(1);
        titleEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setHint("Comma separated values");
        String[] values = Arrays.copyOfRange(all, 1, all.length);
        editText.setText(TextUtils.join(",", values));
        value.addView(editText);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                objects.setTitle(titleEditText.getText().toString() + "," + editText.getText().toString());
            }
        });

        linearLayout.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(value, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setOnLongClickListener(this);
        linearLayout.setTag(objects);
        extras.add(objects);

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public static String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    private void addSpinner(final FieldObjects objects) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextInputLayout title = new TextInputLayout(this);

        String[] all = objects.getTitle().split(",");

        final TextInputEditText titleEditText = new TextInputEditText(this);
        titleEditText.setMaxLines(1);
        titleEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        titleEditText.setHint("Title");
        if (all.length > 0) {
            titleEditText.setText(all[0]);
        }
        title.addView(titleEditText);

        final TextInputLayout value = new TextInputLayout(this);
        final TextInputEditText editText = new TextInputEditText(this);
        editText.setMaxLines(1);
        titleEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setHint("Comma separated values");
        String[] values = Arrays.copyOfRange(all, 1, all.length);
        editText.setText(TextUtils.join(",", values));
        value.addView(editText);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String[] values = editText.getText().toString().split(",");
                if (values.length > 0)
                    objects.setValue(values[0]);
                objects.setTitle(titleEditText.getText().toString() + "," + editText.getText().toString());
            }
        });

        linearLayout.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(value, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setOnLongClickListener(this);
        linearLayout.setTag(objects);
        extras.add(objects);

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addTimePicker(FieldObjects objects) {
        TextInputLayout layout = new TextInputLayout(this);
        final TextInputEditText timePicker = new TextInputEditText(this);
        timePicker.setHint(objects.getTitle());

        layout.addView(timePicker, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        timePicker.setTag(objects);
        timePicker.setOnLongClickListener(this);
        extras.add(objects);

        parent.addView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addDatePicker(FieldObjects objects) {
        TextInputLayout layout = new TextInputLayout(this);
        final TextInputEditText datePicker = new TextInputEditText(this);
        datePicker.setHint(objects.getTitle());

        layout.addView(datePicker, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        datePicker.setTag(objects);
        datePicker.setOnLongClickListener(this);
        extras.add(objects);

        parent.addView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void startTour() {
        View view = parent;
        if (parent.getChildCount() > 0)
            view = parent.getChildAt(0);

        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(14);
        paint.setColor(Color.BLACK);

        final ShowcaseView slide1 = new ShowcaseView.Builder(this)
                .withHoloShowcase()
                .blockAllTouches()
                .setContentTextPaint(paint)
                .setContentTitle("Field Templates")
                .setContentText("Add new field from the pre-defined templates.\nEmail, Phone, Line, Multiline input fields, Checkbox, Switch, Multiple choice, Signature pad and more to choose from.")
                .build();

        final ShowcaseView slide3 = new ShowcaseView.Builder(this)
                .blockAllTouches()
                .withHoloShowcase()
                .setContentTextPaint(paint)
                .setContentTitle("Take Tour")
                .setContentText("Lost? Take tour again.")
                .build();

        final ShowcaseView slide2 = new ShowcaseView.Builder(this)
                .blockAllTouches()
                .withHoloShowcase()
                .setContentTextPaint(paint)
                .setContentTitle("Field Properties")
                .setContentText("Tap and hold on a field to modify its properties. Edit name. Reorder in position. Delete field.")
                .setTarget(new ViewTarget(view))
                .build();

        slide2.overrideButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slide3.show();
                slide1.hide();
                slide2.hide();

            }
        });


        slide1.overrideButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slide2.show();
                slide1.hide();
                slide3.hide();

            }
        });
        slide1.show();
        slide2.hide();
        slide3.hide();

    }

    private void saveForm() {
        int objCount = 0;
        for(FieldObjects o : extras) {
            objCount++;
        }
        if(objCount == 0) {
            showEmptyFormDialog();
            return;
        }
        final EditText editText = new EditText(this);
        editText.setHint("Form name");
        final AlertDialog saveDialog = new AlertDialog.Builder(this)
                .setTitle("Save As")
                .setView(editText)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        save(editText.getText().toString());
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(true)
                .create();

        if (formObject != null) {
            save(formObject.getName());
        } else {
            saveDialog.show();
        }

    }

    private void save(final String name) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (formObject == null)
                    formObject = new FormObject();
                formObject.setName(name);
                if (formObject.getFields() == null) {
                    formObject.setFields(new RealmList<FieldObjects>());
                }
                formObject.getFields().clear();
                for (FieldObjects f : extras) {
                    formObject.getFields().add(f);
                }

                realm.copyToRealmOrUpdate(formObject);
                Toast.makeText(CreateFormActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void renameForm() {
        final EditText editText = new EditText(this);
        editText.setHint("New name");
        editText.setText(formObject.getName());
        final AlertDialog saveDialog = new AlertDialog.Builder(this)
                .setTitle("Edit Form")
                .setView(editText)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        formObject.setName(editText.getText().toString());
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(true)
                .create();
        saveDialog.show();
    }

    private void saveAs() {
        final EditText editText = new EditText(this);
        editText.setHint("Form name");
        editText.setText("Copy of " + formObject.getName());
        final AlertDialog saveDialog = new AlertDialog.Builder(this)
                .setTitle("Save As")
                .setView(editText)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        Realm realm = Realm.getDefaultInstance();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                FormObject object = new FormObject();
                                if (object.getFields() == null) {
                                    object.setFields(new RealmList<FieldObjects>());
                                }
                                object.setName(editText.getText().toString());
                                object.getFields().clear();

                                for (FieldObjects f : extras) {
                                    object.getFields().add(f);
                                }
                                realm.copyToRealmOrUpdate(object);
                                Toast.makeText(CreateFormActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(true)
                .create();
        saveDialog.show();
    }

    private void showEmptyFormDialog(){
        final AlertDialog emptyFormDialog = new AlertDialog.Builder(this)
                .setTitle("No Fields added")
                .setPositiveButton("exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("add Fields", null)
                .setCancelable(true)
                .create();
        emptyFormDialog.show();
    }

    private void loadForm(long id) {
        parent.removeAllViews();
        extras.clear();

        Realm realm = Realm.getDefaultInstance();
        RealmQuery<FormObject> _query = realm.where(FormObject.class);
        _query.equalTo("id", id);

        formObject = realm.copyFromRealm(_query.findFirst());
        if (formObject != null) {
            for (FieldObjects f : formObject.getFields()) {
                switch (f.getType()) {
                    case FIELD_EMAIL:
                        addEmail(f);
                        break;
                    case FIELD_DOB:
                        DateFormat format = SimpleDateFormat.getDateTimeInstance();
                        f.setValue(format.format(new Date()));
                        addDOB(f);
                        break;
                    case FIELD_LINE:
                        addInput(f);
                        break;
                    case FIELD_MULTILINE:
                        addMultiInput(f);
                        break;
                    case FIELD_PHONE:
                        addPhone(f);
                        break;
                    case FIELD_CHECKBOX:
                        addCheckbox(f);
                        break;
                    case FIELD_COUNTRY:
                        addCountry(f);
                        break;
                    case FIELD_CURRENCY:
                        addCurrency(f);
                        break;
                    case FIELD_TOGGLE:
                        addToggle(f);
                        break;
                    case FIELD_SEX:
                        addSex(f);
                        break;
                    case FIELD_TERMS:
                        addTerms(f);
                        break;
                    case FIELD_SIGNATURE:
                        addSignature(f);
                        break;
                    case FIELD_RADIO:
                        addRadioButtons(f);
                        break;
                    case FIELD_DROPDOWN:
                        addSpinner(f);
                        break;
                    case FIELD_DATE:
                        addDatePicker(f);
                        break;
                    case FIELD_TIME:
                        addTimePicker(f);
                        break;
                }
            }
        }
    }

}


