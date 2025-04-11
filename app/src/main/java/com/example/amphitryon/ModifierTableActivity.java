package com.example.amphitryon;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.*;

public class ModifierTableActivity extends AppCompatActivity {

    private Button buttonChoisirDate;
    private Spinner spinnerTables, spinnerServeurs;
    private EditText editTextNombrePlaces;
    private TextView textViewService;
    private Button buttonModifier, buttonSupprimer;

    private OkHttpClient client = new OkHttpClient();

    private List<Integer> idTables = new ArrayList<>();
    private List<Integer> idServeurs = new ArrayList<>();
    private int selectedTableId = -1;
    private int idService = -1;
    private String selectedDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_table);

        // Initialisation des vues
        buttonChoisirDate = findViewById(R.id.buttonChoisirDate);
        spinnerTables = findViewById(R.id.spinnerTables);
        spinnerServeurs = findViewById(R.id.spinnerServeurs);
        editTextNombrePlaces = findViewById(R.id.editTextNombrePlaces);
        textViewService = findViewById(R.id.textViewService);
        buttonModifier = findViewById(R.id.buttonModifier);
        buttonSupprimer = findViewById(R.id.buttonSupprimer);

        // Choix de la date via MaterialDatePicker
        buttonChoisirDate.setOnClickListener(v -> openDatePicker());

        // Action après sélection d'une table
        spinnerTables.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int pos, long id) {
                if (pos >= 0 && pos < idTables.size()) {
                    selectedTableId = idTables.get(pos);
                    chargerDetailsTable(selectedTableId);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Actions des boutons
        buttonModifier.setOnClickListener(v -> modifierTable());
        buttonSupprimer.setOnClickListener(v -> supprimerTable());
        Button buttonRetour = findViewById(R.id.buttonRetour);
        buttonRetour.setOnClickListener(v -> finish());


        chargerServeurs(); // on charge les serveurs dès le début
    }

    // Affiche le calendrier MaterialDatePicker
    private void openDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Choisir une date")
                .build();

        picker.show(getSupportFragmentManager(), "DATE_PICKER");

        picker.addOnPositiveButtonClickListener(selection -> {
            Date date = new Date(selection);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = format.format(date);

            buttonChoisirDate.setText("Date sélectionnée : " + selectedDate);
            chargerTablesPourDate(selectedDate);
        });
    }

    // Appel au serveur pour récupérer les tables d'une date
    private void chargerTablesPourDate(String date) {
        RequestBody formBody = new FormBody.Builder()
                .add("dateCommande", date)
                .build();

        Request request = new Request.Builder()
                .url("http://10.100.0.6/~tguyot/amphitryon/ServiceController/tableController.php?action=getTablesParDate")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                try {
                    JSONArray array = new JSONArray(responseStr);
                    List<String> nomsTables = new ArrayList<>();
                    idTables.clear();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        idTables.add(obj.getInt("idTable"));
                        nomsTables.add("Table ID: " + obj.getInt("idTable"));
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ModifierTableActivity.this,
                                android.R.layout.simple_spinner_item, nomsTables);
                        spinnerTables.setAdapter(adapter);
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(ModifierTableActivity.this, "Erreur JSON : " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }

            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ModifierTableActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // Appel au serveur pour charger les infos d'une table
    private void chargerDetailsTable(int idTable) {
        RequestBody formBody = new FormBody.Builder()
                .add("idTable", String.valueOf(idTable))
                .build();

        Request request = new Request.Builder()
                .url("http://10.100.0.6/~tguyot/amphitryon/ServiceController/tableController.php?action=getTableById")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                try {
                    JSONObject obj = new JSONObject(res);
                    String nbPlaces = obj.getString("nombrePlace");
                    idService = obj.getInt("idService");
                    int idServeur = obj.getInt("idServeur");

                    runOnUiThread(() -> {
                        editTextNombrePlaces.setText(nbPlaces);
                        textViewService.setText("Service : " + (idService == 1 ? "Midi" : "Soir"));
                        int pos = idServeurs.indexOf(idServeur);
                        if (pos != -1) spinnerServeurs.setSelection(pos);
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(ModifierTableActivity.this, "Erreur JSON : " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }

            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ModifierTableActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // Charge tous les serveurs pour remplir le spinner
    private void chargerServeurs() {
        Request request = new Request.Builder()
                .url("http://10.100.0.6/~tguyot/amphitryon/ServiceController/getServeurs.php")
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                String jsonStr = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    List<String> nomsServeurs = new ArrayList<>();
                    idServeurs.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        nomsServeurs.add(obj.getString("nomUtilisateur"));
                        idServeurs.add(obj.getInt("idUtilisateur"));
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ModifierTableActivity.this,
                                android.R.layout.simple_spinner_item, nomsServeurs);
                        spinnerServeurs.setAdapter(adapter);
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(ModifierTableActivity.this, "Erreur JSON", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ModifierTableActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // Modifier une table
    private void modifierTable() {
        String nbPlaces = editTextNombrePlaces.getText().toString().trim();
        if (nbPlaces.isEmpty()) {
            Toast.makeText(this, "Entrez le nombre de places", Toast.LENGTH_SHORT).show();
            return;
        }

        int idServeur = idServeurs.get(spinnerServeurs.getSelectedItemPosition());

        RequestBody formBody = new FormBody.Builder()
                .add("idTable", String.valueOf(selectedTableId))
                .add("nombrePlace", nbPlaces)
                .add("idServeur", String.valueOf(idServeur))
                .build();

        Request request = new Request.Builder()
                .url("http://10.100.0.6/~tguyot/amphitryon/ServiceController/tableController.php?action=modifierTable")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onResponse(Call call, Response response) {
                runOnUiThread(() ->
                        Toast.makeText(ModifierTableActivity.this, "Table modifiée", Toast.LENGTH_SHORT).show()
                );
            }

            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ModifierTableActivity.this, "Erreur de modification", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // Supprimer une table
    private void supprimerTable() {
        RequestBody formBody = new FormBody.Builder()
                .add("idTable", String.valueOf(selectedTableId))
                .build();

        Request request = new Request.Builder()
                .url("http://10.100.0.6/~tguyot/amphitryon/ServiceController/tableController.php?action=supprimerTable")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onResponse(Call call, Response response) {
                runOnUiThread(() ->
                        Toast.makeText(ModifierTableActivity.this, "Table supprimée", Toast.LENGTH_SHORT).show()
                );
            }

            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ModifierTableActivity.this, "Erreur de suppression", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
