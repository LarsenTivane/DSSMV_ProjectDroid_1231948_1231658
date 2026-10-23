package com.example.studymatch;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublishedGroupsActivity extends AppCompatActivity {

    private RecyclerView rvPublishedGroups;
    private StudyGroupAdapter adapter;
    private TextView tvTotalVagas;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_published_groups);

        tvTotalVagas = findViewById(R.id.tvTotalVagas);
        progressBar = findViewById(R.id.progressBar);
        rvPublishedGroups = findViewById(R.id.rvPublishedGroups);
        Button btnRecarregar = findViewById(R.id.btnRecarregarVagas);
        Button btnFechar = findViewById(R.id.btnFecharJanela);

        rvPublishedGroups.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudyGroupAdapter(this::confirmarRemocao);
        rvPublishedGroups.setAdapter(adapter);

        btnRecarregar.setOnClickListener(v -> carregarVagas());
        btnFechar.setOnClickListener(v -> finish());

        carregarVagas();
    }

    private void carregarVagas() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getService().getStudyGroups().enqueue(new Callback<List<StudyGroup>>() {
            @Override
            public void onResponse(Call<List<StudyGroup>> call, Response<List<StudyGroup>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<StudyGroup> lista = response.body();
                    adapter.setGroups(lista);
                    tvTotalVagas.setText("Total de vagas ativas no sistema: " + lista.size());
                } else {
                    tvTotalVagas.setText("Erro ao carregar dados da API.");
                    Toast.makeText(PublishedGroupsActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<StudyGroup>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvTotalVagas.setText("Erro de rede ao ligar ao RestDB.");
                Toast.makeText(PublishedGroupsActivity.this, "Falha: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarRemocao(StudyGroup group) {
        new AlertDialog.Builder(this)
                .setTitle("Encerrar Vaga")
                .setMessage("Deseja fechar permanentemente a vaga do grupo '" + group.getTitle() + "'?")
                .setPositiveButton("Sim, Encerrar", (dialog, which) -> {
                    ApiClient.getService().deleteStudyGroup(group.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(PublishedGroupsActivity.this, "Vaga encerrada!", Toast.LENGTH_SHORT).show();
                                carregarVagas();
                            } else {
                                Toast.makeText(PublishedGroupsActivity.this, "Erro ao remover: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(PublishedGroupsActivity.this, "Falha de comunicação: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}