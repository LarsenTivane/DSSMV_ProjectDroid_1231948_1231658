package com.example.studymatch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    // Coordenadas aproximadas do ISEP (Porto)
    private static final double ISEP_LATITUDE = 41.1780;
    private static final double ISEP_LONGITUDE = -8.6080;
    // Intervalo mínimo entre shakes (evita rajadas de pedidos ao RestDB)
    private static final long SHAKE_COOLDOWN_MS = 3000;

    private RecyclerView rvStudyGroups;
    private StudyGroupAdapter adapter;
    private TextView tvSensor;
    private TextView tvFocusMode;
    private TextView tvLocation;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor proximitySensor;
    private FusedLocationProviderClient fusedLocationClient;

    private float currentAccel;
    private float lastAccel;
    private long ultimoShake = 0;
    private boolean aCarregar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Mapeamento das Views
        tvSensor = findViewById(R.id.tvSensor);
        tvFocusMode = findViewById(R.id.tvFocusMode);
        tvLocation = findViewById(R.id.tvLocation);
        Button btnCarregar = findViewById(R.id.btnCarregar);
        Button btnCriarVaga = findViewById(R.id.btnCriarVaga);
        rvStudyGroups = findViewById(R.id.rvStudyGroups);

        // 2. Botão para abrir o ecrã de vagas publicadas
        Button btnAbrirVagasPublicadas = findViewById(R.id.btnAbrirVagasPublicadas);
        btnAbrirVagasPublicadas.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PublishedGroupsActivity.class);
            startActivity(intent);
        });

        // 3. Configurar RecyclerView
        rvStudyGroups.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudyGroupAdapter(new StudyGroupAdapter.OnActionClickListener() {
            @Override
            public void onJoinClick(StudyGroup group) {
                alterarParticipacaoGrupo(group);
            }

            @Override
            public void onDeleteClick(StudyGroup group) {
                apagarGrupo(group);
            }
        });
        rvStudyGroups.setAdapter(adapter);

        btnCarregar.setOnClickListener(v -> carregarGrupos());
        btnCriarVaga.setOnClickListener(v -> mostrarDialogoCriacao());

        // 4. Configurar Sensores
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
        currentAccel = SensorManager.GRAVITY_EARTH;
        lastAccel = SensorManager.GRAVITY_EARTH;

        // 5. Inicializar GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        obterLocalizacao();

        // 6. Carga inicial dos registos
        carregarGrupos();
    }

    private void obterLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        ISEP_LATITUDE, ISEP_LONGITUDE, results);
                float distanciaMetros = results[0];

                if (distanciaMetros < 1000) {
                    tvLocation.setText(String.format(Locale.getDefault(), "📍 Campus ISEP: a %.0f m de distância", distanciaMetros));
                } else {
                    tvLocation.setText(String.format(Locale.getDefault(), "📍 Fora do Campus: a %.1f km do ISEP", distanciaMetros / 1000.0f));
                }
            } else {
                tvLocation.setText("📍 Localização: A aguardar sinal GPS...");
            }
        });
    }

    private void carregarGrupos() {
        // Evita pedidos sobrepostos (o plano Development só permite 1 pedido/segundo)
        if (aCarregar) {
            return;
        }
        aCarregar = true;

        ApiClient.getService().getStudyGroups().enqueue(new Callback<List<StudyGroup>>() {
            @Override
            public void onResponse(Call<List<StudyGroup>> call, Response<List<StudyGroup>> response) {
                aCarregar = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<StudyGroup> grupos = response.body();
                    for (StudyGroup grupo : grupos) {
                        if (grupo.getTitle() == null || grupo.getTitle().trim().isEmpty()) {
                            grupo.setTitle("Grupo de estudo");
                        }
                        if (grupo.getSubject() == null || grupo.getSubject().trim().isEmpty()) {
                            grupo.setSubject("Geral");
                        }
                        if (grupo.getTurma() == null || grupo.getTurma().trim().isEmpty()) {
                            grupo.setTurma("Geral");
                        }
                    }
                    adapter.setGroups(grupos);
                } else {
                    Toast.makeText(MainActivity.this, "Erro API: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<StudyGroup>> call, Throwable t) {
                aCarregar = false;
                Toast.makeText(MainActivity.this, "Falha de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void alterarParticipacaoGrupo(StudyGroup group) {
        boolean novoEstado = !group.isJoined();
        group.setJoined(novoEstado);

        int novasVagas = Math.max(0, group.getVacancies() + (novoEstado ? -1 : 1));
        group.setVacancies(novasVagas);

        ApiClient.getService().updateStudyGroup(group.getId(), group).enqueue(new Callback<StudyGroup>() {
            @Override
            public void onResponse(Call<StudyGroup> call, Response<StudyGroup> response) {
                if (response.isSuccessful()) {
                    String mensagem = novoEstado ? "Entrou no grupo com sucesso!" : "Saiu do grupo.";
                    Toast.makeText(MainActivity.this, mensagem, Toast.LENGTH_SHORT).show();
                    carregarGrupos();
                } else {
                    Toast.makeText(MainActivity.this, "Não foi possível atualizar a participação: " + response.code(), Toast.LENGTH_SHORT).show();
                    group.setJoined(!novoEstado);
                    group.setVacancies(group.getVacancies() + (novoEstado ? 1 : -1));
                }
            }

            @Override
            public void onFailure(Call<StudyGroup> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Falha ao atualizar participação: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void apagarGrupo(StudyGroup group) {
        new AlertDialog.Builder(this)
                .setTitle("Fechar Vaga")
                .setMessage("Deseja encerrar a vaga '" + group.getTitle() + "'?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    ApiClient.getService().deleteStudyGroup(group.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Vaga encerrada!", Toast.LENGTH_SHORT).show();
                                carregarGrupos();
                            } else {
                                Toast.makeText(MainActivity.this, "Erro ao remover: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void mostrarDialogoCriacao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Publicar Nova Vaga no RestDB");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputTitle = new EditText(this);
        inputTitle.setHint("Título (ex: Grupo Lab DSSMV)");
        layout.addView(inputTitle);

        final EditText inputSubject = new EditText(this);
        inputSubject.setHint("Disciplina (ex: DSSMV)");
        layout.addView(inputSubject);

        final EditText inputVacancies = new EditText(this);
        inputVacancies.setHint("Vagas (ex: 2)");
        inputVacancies.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputVacancies);

        final EditText inputTurma = new EditText(this);
        inputTurma.setHint("Turma (ex: 2DA, 1NA, 2NB)");
        layout.addView(inputTurma);

        builder.setView(layout);

        builder.setPositiveButton("Publicar na BD", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            String subject = inputSubject.getText().toString().trim();
            String turma = inputTurma.getText().toString().trim();
            if (turma.isEmpty()) turma = "Geral";

            int vacancies = 1;
            try {
                vacancies = Integer.parseInt(inputVacancies.getText().toString().trim());
            } catch (NumberFormatException ignored) {}

            // Envia as coordenadas reais do campus/utilizador para calcular a distância
            StudyGroup novoGrupo = new StudyGroup(title, subject, vacancies, turma, ISEP_LATITUDE, ISEP_LONGITUDE);
            novoGrupo.setJoined(true);
            novoGrupo.setVacancies(Math.max(1, vacancies));

            ApiClient.getService().createStudyGroup(novoGrupo).enqueue(new Callback<StudyGroup>() {
                @Override
                public void onResponse(Call<StudyGroup> call, Response<StudyGroup> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Vaga gravada com sucesso no RestDB!", Toast.LENGTH_SHORT).show();
                        carregarGrupos();
                    } else {
                        Toast.makeText(MainActivity.this, "Erro API: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<StudyGroup> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Falha de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            lastAccel = currentAccel;
            currentAccel = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = Math.abs(currentAccel - lastAccel);

            long agora = SystemClock.elapsedRealtime();
            if (delta > 11 && agora - ultimoShake > SHAKE_COOLDOWN_MS) {
                ultimoShake = agora;
                tvSensor.setText("Shake detetado! A atualizar...");
                carregarGrupos();
                obterLocalizacao();
            }
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] < proximitySensor.getMaximumRange()) {
                tvFocusMode.setText("Sessão de Foco: Telemóvel pousado na mesa");
                tvFocusMode.setBackgroundColor(0xFFC8E6C9);
            } else {
                tvFocusMode.setText("Mesa de Estudo: Telemóvel ativo");
                tvFocusMode.setBackgroundColor(0xFFE0F2F1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obterLocalizacao();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            }
            if (proximitySensor != null) {
                sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}