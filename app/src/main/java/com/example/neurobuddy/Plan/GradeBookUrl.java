package com.example.neurobuddy.Plan;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.neurobuddy.R;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GradeBookUrl extends AppCompatActivity {
    private static ProgressDialog progressDialog;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private static TextView resultTextView;
    private static String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_book_login);

        usernameEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        resultTextView = findViewById(R.id.result_text);

        // Pobierz adres URL z intentu
        Intent intent = getIntent();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intent.hasExtra("URL")) {
                    url = intent.getStringExtra("URL");
                }

                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Wywołaj nowe zadanie, które zajmie się logowaniem
                new LoginTask(GradeBookUrl.this, username, password).execute();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private static class LoginTask extends AsyncTask<Void, Void, Void> {
        private final Context context;
        private final String username;
        private final String password;
        private String h1Content;

        LoginTask(Context context, String username, String password) {
            this.context = context;
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Logowanie...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Utwórz mapę parametrów dla danych logowania
                Map<String, String> loginData = new HashMap<>();
                loginData.put("LoginName", username);
                loginData.put("Password", password);

                // Połącz się z formularzem logowania
                Connection.Response loginForm = Jsoup.connect(url)
                        .method(Connection.Method.GET)
                        .execute();

                // Zaloguj się, przesyłając dane formularza
                Connection.Response loggedInPage = Jsoup.connect(url)
                        .cookies(loginForm.cookies())
                        .data(loginData)
                        .method(Connection.Method.POST)
                        .execute();

                // Pobierz zawartość strony po zalogowaniu
                Document document = loggedInPage.parse();
                Log.v("strona", String.valueOf(document));

                // Pobierz zawartość nagłówka <h1>
                Element h1 = document.select("p").first();
                // Zlokalizuj tekst w nagłówku <h1>
                if (h1 != null) {
                    h1Content = h1.text();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Zaktualizuj interfejs użytkownika wynikiem
            resultTextView.setText(h1Content);

            // Zamknij okno dialogowe postępu, jeśli jest widoczne
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }
}