package com.example.amphitryon;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AjouterTableActivity extends AppCompatActivity {

    private DatePicker datePicker;
    private Spinner spinnerService, spinnerServeur;
    private EditText editTextNombrePlaces;
    private Button buttonAjouterTable, buttonQuitterAjouterTable;
    private OkHttpClient client = new OkHttpClient();
    private List<Integer> idServeurs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter_table);

        // Initialisation des composants
        datePicker = findViewById(R.id.datePicker);
        spinnerService = findViewById(R.id.spinnerService);
        spinnerServeur = findViewById(R.id.spinnerServeur);
        editTextNombrePlaces = findViewById(R.id.editTextNombrePlaces);
        buttonAjouterTable = findViewById(R.id.buttonAjouterTable);
        buttonQuitterAjouterTable = findViewById(R.id.buttonQuitterAjouterTable);

        // Remplir le spinner du service
        ArrayAdapter<String> serviceAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new String[]{"Midi", "Soir"});
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerService.setAdapter(serviceAdapter);

        // Charger les serveurs depuis le serveur
        chargerServeurs();

        // Ajouter une table
        buttonAjouterTable.setOnClickListener(v -> ajouterTable());

        // Quitter l'activité
        buttonQuitterAjouterTable.setOnClickListener(v -> finish());
    }

    private void chargerServeurs() {
        Request request = new Request.Builder()
                .url("http://10.100.0.6/~tguyot/amphitryon/ServiceController/getServeurs.php")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ServeurSpinner", "Erreur", e);
                runOnUiThread(() ->
                        Toast.makeText(AjouterTableActivity.this, "Erreur de connexion serveur", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonStr = response.body().string();
                List<String> noms = new ArrayList<>();
                idServeurs.clear();

                try {
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        noms.add(obj.getString("nomUtilisateur"));
                        idServeurs.add(obj.getInt("idUtilisateur"));
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapterServeur = new ArrayAdapter<>(
                                AjouterTableActivity.this,
                                android.R.layout.simple_spinner_item,
                                noms
                        );
                        adapterServeur.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerServeur.setAdapter(adapterServeur);
                    });

                } catch (Exception e) {
                    Log.e("ServeurSpinner", "Erreur JSON", e);
                }
            }
        });
    }

    private void ajouterTable() {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();
        String date = year + "-" + month + "-" + day;

        int posServeur = spinnerServeur.getSelectedItemPosition();
        if (posServeur == -1 || posServeur >= idServeurs.size()) {
            Toast.makeText(this, "Sélectionnez un serveur", Toast.LENGTH_SHORT).show();
            return;
        }
        String idServeur = String.valueOf(idServeurs.get(posServeur));

        String service = spinnerService.getSelectedItem().toString();
        String idService = service.equals("Midi") ? "1" : "2";

        String nbPlaces = editTextNombrePlaces.getText().toString().trim();
        if (nbPlaces.isEmpty()) {
            Toast.makeText(this, "Entrez le nombre de places", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody formBody = new FormBody.Builder()
                .add("nombrePlace", nbPlaces)
                .add("idServeur", idServeur)
                .add("idService", idService)
                .add("dateCommande", date)
                .build();

        Request request = new Request.Builder()
                .url("http://10.100.0.6/~tguyot/amphitryon/ServiceController/ajouterTable.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(AjouterTableActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String rep = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(rep);
                        if (obj.getBoolean("success")) {
                            Toast.makeText(AjouterTableActivity.this, "Table ajoutée avec succès", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AjouterTableActivity.this, "Erreur : " + obj.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(AjouterTableActivity.this, "Erreur JSON : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("AJOUT TABLE", rep); // pour debug
                    }
                });
            }
        });
    }
}
