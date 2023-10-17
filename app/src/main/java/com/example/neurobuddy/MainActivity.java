package com.example.neurobuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText urlEditText;
    private Button urlButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlEditText = findViewById(R.id.url);
        urlButton = findViewById(R.id.url_button);

        urlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pobierz adres URL z EditText
                String url = urlEditText.getText().toString();

                // Przejdź do drugiej aktywności, przekazując adres URL jako dane dodatkowe
                Intent intent = new Intent(MainActivity.this, GradeBookLoginActivity.class);
                intent.putExtra("URL", url);
                startActivity(intent);
            }
        });
    }
}
