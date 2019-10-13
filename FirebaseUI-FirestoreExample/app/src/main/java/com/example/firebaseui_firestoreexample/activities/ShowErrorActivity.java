package com.example.firebaseui_firestoreexample.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.firebaseui_firestoreexample.R;

public class ShowErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_error);


        showErrors();

    }

    private void showErrors() {

        LinearLayout linearLayout = findViewById(R.id.linearLayout);

        Button button = new Button(this);
        button.setText("go to main activity");
        button.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        linearLayout.addView(button);

        TextView textViewMessage = new TextView(this);
        textViewMessage.setText(getIntent().getStringExtra("message")+ "\n\n\n\n" + "stack trace: \n");
        linearLayout.addView(textViewMessage);

        String[] stackTrace = getIntent().getStringArrayExtra("stackTrace");
        for (String s : stackTrace) {
            TextView textView = new TextView(this);
            textView.setText(s + "\n\n");
            linearLayout.addView(textView);
        }

    }
}
