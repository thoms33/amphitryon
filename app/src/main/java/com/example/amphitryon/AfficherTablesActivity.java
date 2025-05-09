package com.example.amphitryon;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.*;

public class AfficherTablesActivity extends AppCompatActivity {

    // Déclaration des composants d'interface
    private Button buttonChoisirDate, buttonAfficher, buttonRetour;
    private Spinner spinnerService;
    private ListView listViewTables;

    // Client HTTP pour les requêtes serveur
    private OkHttpClient client = new OkHttpClient();

    // Date sélectionnée dans le calendrier
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afficher_tables);

        // Initialisation des vues
        buttonChoisirDate = findViewById(R.id.buttonChoisirDate);
        buttonAfficher = findViewById(R.id.buttonAfficher);
        buttonRetour = findViewById(R.id.buttonRetour);
        spinnerService = findViewById(R.id.spinnerService);
        listViewTables = findViewById(R.id.listViewTables);

        // Spinner : Service
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Midi", "Soir"});
        spinnerService.setAdapter(adapter);

        // Date Picker
        buttonChoisirDate.setOnClickListener(v -> openDatePicker());

        // Bouton Afficher
        buttonAfficher.setOnClickListener(v -> {
            if (selectedDate == null) {
                Toast.makeText(this, "Choisissez une date", Toast.LENGTH_SHORT).show();
                return;
            }
            int idService = spinnerService.getSelectedItem().toString().equals("Midi") ? 1 : 2;
            chargerTables(selectedDate, idService);
        });

        // Bouton Retour
        buttonRetour.setOnClickListener(v -> finish());
    }
    // Fonction pour ouvrir le calendrier
    private void openDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Choisir une date")
                .build();

        picker.show(getSupportFragmentManager(), "DATE_PICKER");

        picker.addOnPositiveButtonClickListener(selection -> {
            Date date = new Date(selection);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = format.format(date);
            buttonChoisirDate.setText("Date : " + selectedDate);
        });
    }

    private void chargerTables(String date, int idService) {
        RequestBody formBody = new FormBody.Builder()
                .add("dateCommande", date)
                .add("idService", String.valueOf(idService))
                .build();

        Request request = new Request.Builder()
                .url("http://10.100.0.6/~tguyot/amphitryon/ServiceController/afficherTables.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(responseStr);
                    List<String> tables = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String serveurNom = obj.getString("prenomUtilisateur") + " " + obj.getString("nomUtilisateur");
                        String ligne = "Table " + obj.getInt("idTable") +
                                " - " + obj.getInt("nombrePlace") + " places" +
                                " - Serveur : " + serveurNom;
                        tables.add(ligne);
                    }

                    runOnUiThread(() -> {
                        if (tables.isEmpty()) {
                            Toast.makeText(AfficherTablesActivity.this, "Aucune table enregistrée ce jour-là.", Toast.LENGTH_SHORT).show();
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(AfficherTablesActivity.this,
                                android.R.layout.simple_list_item_1, tables);
                        listViewTables.setAdapter(adapter);
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(AfficherTablesActivity.this, "Erreur JSON", Toast.LENGTH_SHORT).show());
                }
            }

            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(AfficherTablesActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
