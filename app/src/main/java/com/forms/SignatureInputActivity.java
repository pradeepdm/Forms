package com.forms;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.github.gcacace.signaturepad.views.SignaturePad;

public class SignatureInputActivity extends AppCompatActivity {

    private SignaturePad signaturePad;
    private Button save, clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature_input);

        save = (Button) findViewById(R.id.saveButton);
        clear = (Button) findViewById(R.id.clearButton);

        save.setEnabled(false);

        signaturePad = (SignaturePad) findViewById(R.id.signaturePad);
        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {

            }

            @Override
            public void onSigned() {
                save.setEnabled(true);
            }

            @Override
            public void onClear() {
                save.setEnabled(false);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signaturePad.clear();
            }
        });

    }

    private void save() {
        Bitmap bitmap = signaturePad.getTransparentSignatureBitmap();
        String base64 = CreateFormActivity.encodeToBase64(bitmap);
        Intent intent = new Intent();
        intent.putExtras(getIntent().getExtras());
        intent.putExtra("base64", base64);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }
}
