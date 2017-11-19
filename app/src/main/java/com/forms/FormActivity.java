package com.forms;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.samples.vision.ocrreader.OcrCaptureActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;

/**
 * Created on 10/11/2016.
 */

public class FormActivity extends AppCompatActivity {

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


    private EditText fname, lname, address, hair, eyes;
    private AppCompatSpinner sex;
    private LinearLayout parent;
    private String checkedvaluesOfCheckbox = "";
    private List<FieldObjects> extras = new ArrayList<>();

    private FormObject formObject;
    private PersonObject personObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        parent = (LinearLayout) findViewById(R.id.parent);

        if (getIntent().hasExtra("formId")) {
            loadForm(getIntent().getLongExtra("formId", -1));
        }
        if (getIntent().hasExtra("id")) {
            load(getIntent().getLongExtra("id", -1));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Discard Entry")
                .setMessage("You have unsaved entries. Do you want to discard changes?")
                .setCancelable(true)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        validate();
                    }
                })
                .setPositiveButton("No", null)
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        FormActivity.super.onBackPressed();
                    }
                }).create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_ocr) {
            Intent ocr = new Intent(this, OcrCaptureActivity.class);
            ocr.putExtra(OcrCaptureActivity.AutoFocus, true);
            ocr.putExtra(OcrCaptureActivity.UseFlash, false);
            startActivityForResult(ocr, 2001);
        } else if (item.getItemId() == R.id.action_save) {
            validate();
        } else if(item.getItemId() == R.id.action_rename) {
            renameForm();
        }

        return true;
    }

    public static boolean isEmailValid(String email) {
        return !(email == null || TextUtils.isEmpty(email)) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPhoneValid(String phone) {
        return (Patterns.PHONE.matcher(phone).matches() && phone.length() > 9);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2001 && resultCode == 0) {
            String detected = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
            String text = detected.replace("#", "").toUpperCase();
            if (text.toUpperCase().startsWith("EXP") || text.toUpperCase().startsWith("LN")) {
                String[] lines = text.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    if (lines[i].startsWith("LN")) {
                        if (lname != null)
                            lname.setText(lines[i].substring(3));
                    } else if (lines[i].startsWith("FN")) {
                        if (fname != null)
                            fname.setText(lines[i].substring(3));
                        StringBuilder add = new StringBuilder();
                        for (int j = i + 1; j < lines.length; j++) {
                            if (!lines[j].startsWith("DOB")) {
                                add.append(lines[j]).append("\n");
                            } else {
                                break;
                            }
                        }
                        if (address != null)
                            address.setText(add.toString());
                    }
                }
            }
            String hair_exp = "HAIR (.*?) ";
            String eye_exp = "EYES (.*?)\n";
            Pattern hair_pat = Pattern.compile(hair_exp);
            Pattern eye_pat = Pattern.compile(eye_exp);
            Matcher matcher = hair_pat.matcher(text);
            if (matcher.find()) {
                if (hair != null)
                    hair.setText(matcher.group(1));
            }
            matcher = eye_pat.matcher(text);
            if (matcher.find()) {
                if (eyes != null)
                    eyes.setText(matcher.group(1));
            }
        }
// else if (requestCode == 2002 && resultCode == RESULT_OK) {
//            if (data != null) {
//                load(data.getLongExtra("id", 0));
//            }
//        }

        else if (requestCode == 2003 && resultCode == RESULT_OK) {
            if (data != null) {
                String tag = data.getStringExtra("tag");
                View view = parent.findViewWithTag(tag);
                if (view != null && view instanceof ImageView) {
                    FieldObjects objects = (FieldObjects) ((View) view.getParent()).getTag();
                    objects.setValue(data.getStringExtra("base64"));
                    ((ImageView) view).setImageBitmap(CreateFormActivity.decodeBase64(data.getStringExtra("base64")));
                }
            }
        }
    }

    private void addEmail(final FieldObjects objects) {
        final TextInputLayout layout = new TextInputLayout(this);
        final TextInputEditText editText = new TextInputEditText(this);
        editText.setHint(objects.getTitle());
        if (objects.isRequired()) {
            editText.setHint(objects.getTitle() + "*");
        }
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText.setText(objects.getValue());
        layout.addView(editText);


        editText.setTag(objects.getId());
        extras.add(objects);


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isEmailValid(editable.toString())) {
                    objects.setValue(editable.toString());
                } else {
                    editText.setError("enter a valid email address");
                }
            }
        });

        parent.addView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addPhone(final FieldObjects objects) {
        TextInputLayout layout = new TextInputLayout(this);
        final TextInputEditText editText = new TextInputEditText(this);
        editText.setHint(objects.getTitle());
        if (objects.isRequired()) {
            editText.setHint(objects.getTitle() + "*");
        }
        editText.setText(objects.getValue());
        editText.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(editText);
        int maxLength = 10;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        editText.setFilters(fArray);
//        FieldObjects objects = new FieldObjects(FIELD_PHONE, "Phone", "");
        editText.setTag(objects.getId());
        extras.add(objects);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isPhoneValid(editable.toString())) {
                    objects.setValue(editable.toString());
                } else {
                    editText.setError("enter a valid Phone number");
                }
            }
        });

        parent.addView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addDOB(final FieldObjects objects) {
        TextInputLayout layout = new TextInputLayout(this);
        TextInputEditText editText = new TextInputEditText(this);
        editText.setHint(objects.getTitle());
        editText.setText(objects.getValue());
        editText.setInputType(InputType.TYPE_CLASS_DATETIME);
        if (objects.isRequired()) {
            editText.setHint(objects.getTitle() + "*");
        }
        layout.addView(editText);

//        FieldObjects objects = new FieldObjects(FIELD_DOB, "Date", "");
        editText.setTag(objects.getId());
        extras.add(objects);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                objects.setValue(editable.toString());
            }
        });
//        editText.setOnLongClickListener(this);
        parent.addView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addInput(final FieldObjects objects) {
        TextInputLayout layout = new TextInputLayout(this);
        TextInputEditText editText = new TextInputEditText(this);
        editText.setHint(objects.getTitle());
        editText.setText(objects.getValue());
        if (objects.isRequired()) {
            editText.setHint(objects.getTitle() + "*");
        }
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setMaxLines(1);
        layout.addView(editText);
        editText.setTag(objects.getId());
        extras.add(objects);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                objects.setValue(editable.toString());
            }
        });
        if (objects.getKnown_type() != null) {
            switch (objects.getKnown_type()) {
                case "default_fn":
                    fname = editText;
                    break;
                case "default_ln":
                    lname = editText;
                    break;
                case "default_hair":
                    hair = editText;
                    break;
                case "default_eye":
                    eyes = editText;
                    break;
            }
        }
//        editText.setOnLongClickListener(this);
        parent.addView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addMultiInput(final FieldObjects objects) {
        TextInputLayout layout = new TextInputLayout(this);
        TextInputEditText editText = new TextInputEditText(this);
        editText.setHint(objects.getTitle());
        if (objects.isRequired()) {
            editText.setHint(objects.getTitle() + "*");
        }
        editText.setText(objects.getValue());
        layout.addView(editText);
        editText.setTag(objects.getId());
        extras.add(objects);

//        editText.setOnLongClickListener(this);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                objects.setValue(editable.toString());
            }
        });
        if (objects.getKnown_type() != null) {
            if (objects.getKnown_type().equals("default_address"))
                address = editText;
        }
        parent.addView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addToggle(final FieldObjects objects) {

        SwitchCompat switchCompat = new SwitchCompat(this);
        switchCompat.setText(objects.getTitle());
        switchCompat.setTextOff(objects.getTitle());
        switchCompat.setTextOn(objects.getTitle());
        switchCompat.setChecked(Boolean.valueOf(objects.getValue()));

        if (objects.isRequired()) {
            switchCompat.setTextOff(objects.getTitle() + "*");
            switchCompat.setTextOn(objects.getTitle() + "*");
        }

//        switchCompat.setOnLongClickListener(this);
        switchCompat.setTag(objects.getId());
        extras.add(objects);

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                objects.setValue(String.valueOf(isChecked));
            }
        });


        parent.addView(switchCompat, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addCheckbox(final FieldObjects objects) {
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        String valueString = objects.getTitle();
        String[] values = valueString.split(",");
        TextView textView = new TextView(this);
        textView.setText(values[0]);
        if (objects.isRequired()) {
            textView.append("*");
        }
        final String[] radioValues = Arrays.copyOfRange(values, 1, values.length);

        if(radioValues.length == 0) {
            return;
        }
        linearLayout.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        for(String box : radioValues) {
            final CheckBox checkBox = new CheckBox(this);
            checkBox.setText(box);


            String[] checkedValue = objects.getValue().split(",");

            for(String s : checkedValue) {
                if (box.equals(checkedValue)) {
                    checkBox.setChecked(true);
                }
            }

            checkBox.setTag(objects.getId());
            extras.add(objects);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    objects.setValue(String.valueOf(isChecked));
                    calculateValues(linearLayout, objects);
                }
            });
            linearLayout.addView(checkBox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        linearLayout.setTag(objects.getId());
        extras.add(objects);

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void calculateValues(View linearLayout, FieldObjects objects) {
        LinearLayout layout = (LinearLayout) linearLayout;
        checkedvaluesOfCheckbox = "";
        for(int i = 0; i < layout.getChildCount(); i++) {
            if (layout.getChildAt(i) instanceof CheckBox) {
                if(((CheckBox) layout.getChildAt(i)).isChecked())
                    checkedvaluesOfCheckbox += ((CheckBox) layout.getChildAt(i)).getText().toString()+ ",";
            }
        }
        objects.setValue(checkedvaluesOfCheckbox);
    }

    private void addCountry(final FieldObjects objects) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(this);
        textView.setText(objects.getTitle());
        if (objects.isRequired()) {
            textView.append("*");
        }

        AppCompatSpinner spinner = new AppCompatSpinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, COUNTRY);
        spinner.setAdapter(adapter);

        spinner.setSelection(adapter.getPosition(objects.getValue()));

        linearLayout.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(spinner, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        linearLayout.setTag(objects.getId());
//        linearLayout.setOnLongClickListener(this);
        extras.add(objects);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                objects.setValue(COUNTRY[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addCurrency(final FieldObjects objects) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(this);
        textView.setText(objects.getTitle());
        if (objects.isRequired()) {
            textView.append("*");
        }

        AppCompatSpinner spinner = new AppCompatSpinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CURRENCY);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(objects.getValue()));

        linearLayout.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(spinner, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        linearLayout.setTag(objects.getId());
//        linearLayout.setOnLongClickListener(this);
        extras.add(objects);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                objects.setValue(CURRENCY[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addSex(final FieldObjects objects) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(this);
        textView.setText(objects.getTitle());
        if (objects.isRequired()) {
            textView.append("*");
        }
        AppCompatSpinner spinner = new AppCompatSpinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SEX);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(objects.getValue()));

        linearLayout.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(spinner, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        linearLayout.setTag(objects.getId());
//        linearLayout.setOnLongClickListener(this);
        extras.add(objects);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                objects.setValue(SEX[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sex = spinner;

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }


    private void addTerms(final FieldObjects objects) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(objects.getTitle());
        textView.setLines(7);

        scrollView.addView(textView, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        CheckBox checkBox = new CheckBox(this);
        checkBox.setText("Accept");
        if (objects.isRequired()) {
            checkBox.append("*");
        }
        checkBox.setChecked(Boolean.valueOf(objects.getValue()));

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                objects.setValue(String.valueOf(isChecked));
            }
        });

        linearLayout.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(checkBox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        extras.add(objects);
        checkBox.setTag(objects.getId());
//        linearLayout.setOnLongClickListener(this);

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addSignature(final FieldObjects objects) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(this);
        textView.setText(objects.getTitle());
        if (objects.isRequired()) {
            textView.append("*");
        }

        ImageView imageView = new ImageView(this);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageBitmap(CreateFormActivity.decodeBase64(objects.getValue()));
        imageView.setTag(objects.getId());

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FormActivity.this, SignatureInputActivity.class);
                intent.putExtra("tag", objects.getId());
                startActivityForResult(intent, 2003);
            }
        });


        linearLayout.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(imageView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        extras.add(objects);
        linearLayout.setTag(objects);
//        linearLayout.setOnLongClickListener(this);

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    }

    private void addRadioButtons(final FieldObjects objects) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        RadioGroup radioGroup = new RadioGroup(this);
        String valueString = objects.getTitle();
        String[] values = valueString.split(",");
        TextView textView = new TextView(this);
        textView.setText(values[0]);
        if (objects.isRequired()) {
            textView.append("*");
        }
        final String[] radioValues = Arrays.copyOfRange(values, 1, values.length);
        if(radioValues.length == 0) {
            return;
        }
        radioGroup.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        for(String radio : radioValues) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(radio);

            String checkedValue = objects.getValue();

            if (radio.equals(checkedValue)) {
                radioButton.setChecked(true);
            }

            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                        objects.setValue(buttonView.getText().toString());
                }
            });

            radioGroup.addView(radioButton);
        }

        linearLayout.addView(radioGroup, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setTag(objects.getId());
        extras.add(objects);

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }


    private void addSpinner(final FieldObjects objects) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        String valueString = objects.getTitle();

        String[] values = valueString.split(",");
        TextView textView = new TextView(this);
        textView.setText(values[0]);
        if (objects.isRequired()) {
            textView.append("*");
        }

        final String[] dropDownValues = Arrays.copyOfRange(values, 1, values.length);
        if(dropDownValues.length == 0) {
            return;
        }
        AppCompatSpinner spinner = new AppCompatSpinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dropDownValues);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(objects.getValue()));

        linearLayout.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(spinner, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        linearLayout.setTag(objects.getId());
//        linearLayout.setOnLongClickListener(this);
        extras.add(objects);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                objects.setValue(dropDownValues[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sex = spinner;

        parent.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }


    private void addTimePicker(final FieldObjects objects) {
        TextInputLayout layout = new TextInputLayout(this);
        final TextInputEditText timePicker = new TextInputEditText(this);
        timePicker.setHint(objects.getTitle());
        if (objects.isRequired()) {
            timePicker.setHint(objects.getTitle() + "*");
        }
        timePicker.setText(objects.getValue());
        timePicker.setInputType(InputType.TYPE_NULL);
        Date value = new Date();
        final Calendar cal = Calendar.getInstance();
        cal.setTime(value);
        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // now show the time picker
                new TimePickerDialog(FormActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view,
                                                  int h, int min) {
                                cal.set(Calendar.HOUR_OF_DAY, h);
                                cal.set(Calendar.MINUTE, min);
                                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                                timePicker.setText(formatter.format(cal.getTime()));
                                objects.setValue(formatter.format(cal.getTime()));
                            }
                        }, cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE), true).show();
            }
        });

        timePicker.setTag(objects.getId());
        extras.add(objects);
        layout.addView(timePicker);
        parent.addView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addDatePicker(final FieldObjects objects) {
        TextInputLayout layout = new TextInputLayout(this);
        final TextInputEditText datePicker = new TextInputEditText(this);
        if (objects.isRequired()) {
            datePicker.setHint(objects.getTitle() + "*");
        }
        datePicker.setHint(objects.getTitle());
        datePicker.setText(objects.getValue());

        datePicker.setInputType(InputType.TYPE_NULL);
        Date value = new Date();
        final Calendar cal = Calendar.getInstance();
        cal.setTime(value);
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date value = new Date();
                final Calendar cal = Calendar.getInstance();
                cal.setTime(value);
                new DatePickerDialog(FormActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view,
                                                  int y, int m, int d) {
                                cal.set(Calendar.YEAR, y);
                                cal.set(Calendar.MONTH, m);
                                cal.set(Calendar.DAY_OF_MONTH, d);
                                // now show the time picker
                                DateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
                                datePicker.setText(formatter.format(cal.getTime()));
                                objects.setValue(formatter.format(cal.getTime()));
                            }
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        datePicker.setTag(objects.getId());
        extras.add(objects);
        layout.addView(datePicker);
        parent.addView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void save(final boolean draft) {
        final EditText editText = new EditText(this);
        editText.setHint("Form name");
        final AlertDialog saveDialog = new AlertDialog.Builder(this)
                .setTitle("Save As")
                .setView(editText)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        save(editText.getText().toString(), draft);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(true)
                .create();
        if (personObject != null)
            save(personObject.getTitle(), draft);
        else
            saveDialog.show();
    }


    private void renameForm() {
        if (personObject == null) {
            return;
        }
        final EditText editText = new EditText(this);
        editText.setHint("New name");
        final AlertDialog renameDialog = new AlertDialog.Builder(this)
                .setTitle("Edit Form")
                .setView(editText)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        personObject.setTitle(editText.getText().toString());
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(true)
                .create();
        renameDialog.show();
    }


    private void validate() {
        boolean empty = false;
        for (FieldObjects obj : extras) {
            if (obj.isRequired() && obj.getType() == FIELD_CHECKBOX && (TextUtils.isEmpty(obj.getValue()) || obj.getValue().equals("false"))) {
                View view = parent.findViewWithTag(obj.getId());
                LinearLayout v = (LinearLayout) view;
                if (v.getChildAt(0) instanceof TextView) {
                    TextView child = (TextView) v.getChildAt(0);
                    child.setError("Required");
                    return;
                }
            } else if (obj.isRequired() && obj.getType() == FIELD_RADIO && (TextUtils.isEmpty(obj.getValue()) || obj.getValue().equals("false"))) {
                View view = parent.findViewWithTag(obj.getId());
                LinearLayout v = (LinearLayout) view;
                RadioGroup rg = (RadioGroup) v.getChildAt(0);
                if (rg.getChildAt(0) instanceof TextView) {
                    TextView child = (TextView) rg.getChildAt(0);
                    child.setError("Required");
                    return;
                }
            } else if (obj.isRequired() && obj.getType() == FIELD_TERMS && (TextUtils.isEmpty(obj.getValue()) || obj.getValue().equals("false"))) {
                View view = parent.findViewWithTag(obj.getId());
                LinearLayout v = (LinearLayout) view.getParent();
                ScrollView rg = (ScrollView) v.getChildAt(0);
                if (rg.getChildAt(0) instanceof TextView) {
                    TextView child = (TextView) rg.getChildAt(0);
                    child.setError("Required");
                    return;
                }
            } else if (obj.isRequired() && TextUtils.isEmpty(obj.getValue())) {
                View view = parent.findViewWithTag(obj.getId());
                if (view != null && view instanceof TextInputEditText) {
                    TextInputEditText editText = (TextInputEditText) view;
                    editText.setError("Required");
                    view.requestFocus();
                    return;
                }
            } else if (TextUtils.isEmpty(obj.getValue())) {
                empty = true;
            } else if (obj.getType() == FIELD_CHECKBOX && (TextUtils.isEmpty(obj.getValue()) || obj.getValue().equals("false"))) {
                empty = true;
            } else if(obj.getType() == FIELD_SIGNATURE) {
                String base64 = CreateFormActivity.encodeToBase64(BitmapFactory.decodeResource(getResources(), R.drawable.signature_placeholder));
                if(obj.isRequired() && obj.getValue().equals(base64)) {
                    View view = parent.findViewWithTag(obj.getId());
                    LinearLayout v = (LinearLayout) view.getParent();
                    TextView tv = (TextView) v.getChildAt(0);
                    if (tv instanceof TextView) {
                        tv.setError("Required");
                        return;
                    }
                } else if(obj.getValue().equals(base64)){
                    empty = true;
                }
            }
        }
        if (empty) {
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Save Draft")
                    .setMessage("Save incomplete form?")
                    .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            save(true);
                        }
                    })
                    .setPositiveButton("No", null)
                    .create();
            dialog.show();
        } else {
            save(false);
        }
    }

    private void save(final String name, final boolean draft) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                PersonObject object = personObject;
                if (object == null) {
                    object = new PersonObject();
                    object.setFormId(getIntent().getLongExtra("formId", -1));
                    object.setTitle(name);
                }
                if (object.getExtras() == null) {
                    object.setExtras(new RealmList<FieldObjects>());
                } else {
                    object.getExtras().clear();
                }
                for (FieldObjects f : extras) {
                    object.getExtras().add(f);
                }

                object.setDraft(draft);

                realm.copyToRealmOrUpdate(object);
                Toast.makeText(FormActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


    private void load(long id) {
//        parent.removeAllViews();
//        fname = null;
//        lname = null;
//        hair = null;
//        eyes = null;
//        sex = null;
//        address = null;
//        extras.clear();

        Realm realm = Realm.getDefaultInstance();
        RealmQuery<PersonObject> _query = realm.where(PersonObject.class);
        _query.equalTo("id", id);

        personObject = realm.copyFromRealm(_query.findFirst());
        if (personObject != null) {
            for (FieldObjects f : personObject.getExtras()) {
                if (f.getType() == FIELD_LINE) {
                    if (f.getKnown_type() != null) {
                        if (f.getKnown_type().equals("default_fn") && fname != null)
                            fname.setText(f.getValue());
                        else if (f.getKnown_type().equals("default_ln") && lname != null)
                            lname.setText(f.getValue());
                        else if (f.getKnown_type().equals("default_hair") && hair != null)
                            hair.setText(f.getValue());
                        else if (f.getKnown_type().equals("default_eye") && eyes != null)
                            eyes.setText(f.getValue());
                    } else {
                        View view = parent.findViewWithTag(f.getId());
                        EditText e = (EditText) view;
                        e.setText(f.getValue());
                        updateObjectInExtras(f);
                    }
                } else if (f.getType() == FIELD_MULTILINE && address != null) {
                    if (f.getKnown_type() != null) {
                        if (f.getKnown_type().equals("default_address")) {
                            address.setText(f.getValue());
                        }
                    } else {
                        View view = parent.findViewWithTag(f.getId());
                        EditText e = (EditText) view;
                        e.setText(f.getValue());
                        updateObjectInExtras(f);
                    }
                } else if (f.getType() == FIELD_MULTILINE && address == null) {
                    View view = parent.findViewWithTag(f.getId());
                    EditText e = (EditText) view;
                    e.setText(f.getValue());
                    updateObjectInExtras(f);
                } else {
                    View view = parent.findViewWithTag(f.getId());
                    if (view instanceof EditText) {
//region editText
                        EditText e = (EditText) view;
                        e.setText(f.getValue());
                        updateObjectInExtras(f);
//endregion
                    } else if (view instanceof LinearLayout) {
//region spinner
                        if (f.getType() == FIELD_SEX || f.getType() == FIELD_COUNTRY || f.getType() == FIELD_CURRENCY || f.getType() == FIELD_SIGNATURE || f.getType() == FIELD_DROPDOWN) {
                            AppCompatSpinner spinner = (AppCompatSpinner) ((LinearLayout) view).getChildAt(1);
                            if (spinner != null) {
                                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
                                spinner.setSelection(adapter.getPosition(f.getValue()));
                            }
                        } else if (f.getType() == FIELD_RADIO) {
                            updateRadioButton(view, f.getValue());
                        } else if(f.getType() == FIELD_CHECKBOX) {
                            updateCheckboxes(view, f.getValue());
                        }
//endregion
                    } else if (view instanceof SwitchCompat) {
//region switch
                        SwitchCompat switchCompat = (SwitchCompat) view;
                        switchCompat.setChecked(Boolean.valueOf(f.getValue()));
//endregion
                    } else if (view instanceof CheckBox) {
//region checkbox
                        CheckBox checkBox = (CheckBox) view;
                        checkBox.setChecked(Boolean.valueOf(f.getValue()));
//endregion
                    } else if (view  instanceof ImageView) {
                        ((ImageView) view).setImageBitmap(CreateFormActivity.decodeBase64(f.getValue()));
                        updateObjectInExtras(f);
                    }
                }
            }
        }
    }

    private  void updateRadioButton(View view, String value) {
        RadioGroup r = (RadioGroup) ((LinearLayout) view).getChildAt(0);
        for(int i=1; i< r.getChildCount(); i++) {
            RadioButton rb = (RadioButton) r.getChildAt(i);
            if(rb.getText().equals(value))
                rb.setChecked(true);
        }
    }

    private void updateCheckboxes(View view, String value) {
        String[] checkedItems = value.split((","));
        LinearLayout ln = (LinearLayout) view;
        for(int i=1; i< ln.getChildCount(); i++) {
            if(ln.getChildAt(i) instanceof CheckBox) {
                for(String s : checkedItems) {
                    if(s.equals(((CheckBox) ln.getChildAt(i)).getText().toString())) {
                        ((CheckBox) ln.getChildAt(i)).setChecked(true);
                    }
                }
            }
        }
    }

    private void updateObjectInExtras(FieldObjects object) {
        for (FieldObjects f : extras) {
            if(f.getType() == object.getType() && f.getTitle().equals(object.getTitle()) && f.getKnown_type() == null) {
                f.setValue(object.getValue());
            }
        }
    }

    private void loadForm(long id) {
        parent.removeAllViews();
        fname = null;
        lname = null;
        hair = null;
        eyes = null;
        sex = null;
        address = null;
        extras.clear();

        personObject = null;
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
