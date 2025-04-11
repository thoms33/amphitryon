package com.example.amphitryon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    String responseStr ;
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button buttonValiderAuthentification = findViewById(R.id.buttonValiderAuthentification);
        buttonValiderAuthentification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Appel de la fonction authentification
                try {
                    authentification();
                }catch (IOException e) {
                    Log.d("Test" , e.getMessage());
                }
            }
        });


        Button buttonQuitterAuthentification = findViewById(R.id.buttonQuitterAuthentification);
        buttonQuitterAuthentification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.finish();
            }
        });


    }


    public void authentification() throws IOException {

        EditText textLogin = findViewById(R.id.editTextLogin);
        EditText textMdp = findViewById(R.id.editTextMdp);

        RequestBody formBody = new FormBody.Builder()
                .add("login", textLogin.getText().toString())
                .add("mdp",  textMdp.getText().toString())
                .build();

        Request request = new Request.Builder()
                .url("http://10.100.0.6/~tguyot/amphitryon/ServiceController/authentification.php")
                .post(formBody)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {

            public  void onResponse(Call call, Response response) throws IOException {

                responseStr = response.body().string();

                Log.d("Test", responseStr);

                if (responseStr.compareTo("false")!=0){
                    try {
                        JSONObject utilisateur = new JSONObject(responseStr);
                        Log.d("Test",utilisateur.getString("nomUtilisateur") + " est  connecté");
                        if(utilisateur.getString("idRole").compareTo("1")==0) {
                            Intent intent = new Intent(MainActivity.this,GestionTableActivity.class);
                            //intent.putExtra("etudiant", etudiant.toString());
                            startActivity(intent);


                        }
                        else if(utilisateur.getString("idRole").compareTo("2")==0) {
                            //Intent intent = new Intent(MainActivity.this, MainChefSalleActivity.class);
                            //intent.putExtra("etudiant", etudiant.toString());
                           // startActivity(intent);


                        }
                    }
                    catch(JSONException e){
                        Log.d("Test", "respose error :" +Objects.requireNonNull(e.getMessage()));
                        // Toast.makeText(MainActivity.this, "Erreur de connexion !!!! !", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("Test","Login ou mot de  passe non valide !");
                }
            }

            public void onFailure(Call call, IOException e)
            {
                Log.d("Test","erreur!!! connexion impossible" + e.getMessage());
            }

        });
    }


}

