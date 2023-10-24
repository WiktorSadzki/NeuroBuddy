package com.example.neurobuddy.Plan;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.example.neurobuddy.R;

import net.maciekmm.uonet.UONETClient;
import net.maciekmm.uonet.UONETException;
import net.maciekmm.uonet.models.Certyfikat;
import net.maciekmm.uonet.models.CertyfikatRequest;
import net.maciekmm.uonet.models.PlanLekcji;
import net.maciekmm.uonet.models.PlanLekcjiRequest;
import net.maciekmm.uonet.models.Slowniki;
import net.maciekmm.uonet.models.SlownikiRequest;
import net.maciekmm.uonet.models.Uczniowie;
import net.maciekmm.uonet.models.UczniowieRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

public class GradeBookLogin extends AppCompatActivity {

    EditText tokenEditText;
    EditText symbolEditText;
    EditText pinEditText;
    Button loginButton;
    TextView resultTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_book_login);

        tokenEditText = findViewById(R.id.token);
        symbolEditText = findViewById(R.id.symbol);
        pinEditText = findViewById(R.id.pin);
        loginButton = findViewById(R.id.login_button);
        resultTextView = findViewById(R.id.result_text);



        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String symbol = symbolEditText.getText().toString();
//                String pin = pinEditText.getText().toString();
//                String token = tokenEditText.getText().toString();

                String symbol = "https://uonetplus.vulcan.net.pl/powiatgostynski";
                String pin = "803916";
                String token = "3S1RGP9";

                Python py = Python.getInstance();
                PyObject pyObject = py.getModule("main");
                PyObject result = pyObject.callAttr("login_vulcan", "3S13JA9", "https://uonetplus.vulcan.net.pl/powiatgostynski", "963158");

                Log.v("python", String.valueOf(result));

                try {
                    UONETClient uonetClient = UONETClient.fromCredentials(symbol, pin, token);
                } catch (UONETException | MalformedURLException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
