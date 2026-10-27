package com.example.studymatch;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublishedGroupsActivity extends AppCompatActivity {

    // Coordenadas padrão de referência (Campus ISEP) caso o telemóvel não tenha GPS fixo no momento
    private double userLat = 41.1780;
    private double userLng = -8.6080;

    private RecyclerView rvPublishedGroups;
    private StudyGroupAdapter adapter;
    private TextView tvTotalVagas;
    private ProgressBar progressBar;
    private Spinner spTurmas;

    private List<StudyGroup> todosGrupos = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_published_groups);

        tvTotalVagas = findViewById(R.id.tvTotalVagas);
        progressBar = findViewById(R.id.progressBar);
        rvPublishedGroups = findViewById(R.id.rvPublishedGroups);
        spTurmas = findViewById(R.id.spTurmas);
        Button btnFecharJanela = findViewById(R.id.btnFecharJanela);
        Button btnRecarregarVagas = findViewById(R.id.btnRecarregarVagas);

        rvPublishedGroups.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudyGroupAdapter(new StudyGroupAdapter.OnActionClickListener() {
            @Override
            public void onJoinClick(StudyGroup group) {
                alterarParticipacaoGrupo(group);
            }

            @Override
            public void onDeleteClick(StudyGroup group) {
                confirmarRemocao(group);
            }
        });
        rvPublishedGroups.setAdapter(adapter);

        btnFecharJanela.setOnClickListener(v -> finish());
        btnRecarregarVagas.setOnClickListener(v -> carregarVagasDaApi());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        obterLocalizacaoAtual();
    }

    private void obterLocalizacaoAtual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();
                }
                carregarVagasDaApi();
            });
        } else {
            carregarVagasDaApi();
        }
    }

    private void carregarVagasDaApi() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getService().getStudyGroups().enqueue(new Callback<List<StudyGroup>>() {
            @Override
            public void onResponse(Call<List<StudyGroup>> call, Response<List<StudyGroup>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    todosGrupos = response.body();
                    calcularDistanciasEConfigurarFiltro();
                } else {
                    tvTotalVagas.setText("Erro ao obter grupos da API.");
                }
            }

            @Override
            public void onFailure(Call<List<StudyGroup>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                String detalhe = t.getLocalizedMessage();
                tvTotalVagas.setText("Falha de rede ao ligar ao RestDB"
                        + (detalhe == null || detalhe.trim().isEmpty() ? "." : ": " + detalhe));
            }
        });
    }

    private void calcularDistanciasEConfigurarFiltro() {
        List<String> opcoesTurmas = new ArrayList<>();
        opcoesTurmas.add("Todas as Turmas");

        // 1. Calcular distância de cada grupo em relação ao utilizador
        for (StudyGroup g : todosGrupos) {
            if (g.getLatitude() != 0.0 && g.getLongitude() != 0.0) {
                float[] resultado = new float[1];
                Location.distanceBetween(userLat, userLng, g.getLatitude(), g.getLongitude(), resultado);
                g.setDistanciaMetros(resultado[0]);
            } else {
                g.setDistanciaMetros(999999f); // Sem GPS vai para o fim
            }

            // Coletar turmas únicas para o menu
            String t = g.getTurma();
            if (!opcoesTurmas.contains(t)) {
                opcoesTurmas.add(t);
            }
        }

        // 2. Ordenar por proximidade crescente (os mais perto primeiro)
        Collections.sort(todosGrupos, Comparator.comparingDouble(StudyGroup::getDistanciaMetros));

        // 3. Preencher o seletor de turmas
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opcoesTurmas);
        spTurmas.setAdapter(spinnerAdapter);

        spTurmas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String turmaSelecionada = opcoesTurmas.get(position);
                aplicarFiltroTurma(turmaSelecionada);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        aplicarFiltroTurma("Todas as Turmas");
    }

    private void aplicarFiltroTurma(String turma) {
        List<StudyGroup> filtrados = new ArrayList<>();

        for (StudyGroup g : todosGrupos) {
            if (g.getTurma() == null || g.getTurma().trim().isEmpty()) {
                g.setTurma("Geral");
            }
            if (turma.equals("Todas as Turmas") || g.getTurma().equalsIgnoreCase(turma)) {
                filtrados.add(g);
            }
        }

        adapter.setGroups(filtrados);
        tvTotalVagas.setText("A apresentar: " + filtrados.size() + " vaga(s) ordenada(s) por proximidade");
    }

    private void alterarParticipacaoGrupo(StudyGroup group) {
        boolean novoEstado = !group.isJoined();
        group.setJoined(novoEstado);
        group.setVacancies(Math.max(0, group.getVacancies() + (novoEstado ? -1 : 1)));

        ApiClient.getService().updateStudyGroup(group.getId(), group).enqueue(new Callback<StudyGroup>() {
            @Override
            public void onResponse(Call<StudyGroup> call, Response<StudyGroup> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PublishedGroupsActivity.this, novoEstado ? "Entrou no grupo!" : "Saiu do grupo.", Toast.LENGTH_SHORT).show();
                    carregarVagasDaApi();
                } else {
                    Toast.makeText(PublishedGroupsActivity.this, "Erro ao atualizar participação.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StudyGroup> call, Throwable t) {
                String detalhe = t.getLocalizedMessage();
                Toast.makeText(PublishedGroupsActivity.this,
                        "Falha ao atualizar o grupo"
                                + (detalhe == null || detalhe.trim().isEmpty() ? "." : ": " + detalhe),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmarRemocao(StudyGroup group) {
        new AlertDialog.Builder(this)
                .setTitle("Encerrar Vaga")
                .setMessage("Deseja fechar a vaga '" + group.getTitle() + "'?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    ApiClient.getService().deleteStudyGroup(group.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(PublishedGroupsActivity.this, "Vaga encerrada!", Toast.LENGTH_SHORT).show();
                                carregarVagasDaApi();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(PublishedGroupsActivity.this, "Erro de rede.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Não", null)
                .show();
    }
}