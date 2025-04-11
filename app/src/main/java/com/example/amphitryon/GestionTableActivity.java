package com.example.amphitryon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GestionTableActivity extends AppCompatActivity {

    private Button btnAjouterTable;
    private Button btnModifierTable;
    private Button btnAfficherTables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_table);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialise  les bouttons
        btnAjouterTable = findViewById(R.id.buttonSave);
        btnModifierTable = findViewById(R.id.buttonModify);
        btnAfficherTables = findViewById(R.id.buttonShow);


        // Ajouter une table
        btnAjouterTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GestionTableActivity.this, AjouterTableActivity.class);
                startActivity(intent);
            }
        });

        // Modifier une table
        btnModifierTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GestionTableActivity.this, ModifierTableActivity.class);
                startActivity(intent);
            }
        });


        // Afficher toutes les tables
        btnAfficherTables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GestionTableActivity.this, AfficherTablesActivity.class);
                startActivity(intent);
            }
        });
    }
}
