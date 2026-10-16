package com.example.studymatch;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView tvResultado;
    private TextView tvSensor;
    private Button btnCarregar;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float currentAccel;
    private float lastAccel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResultado = findViewById(R.id.tvResultado);
        tvSensor = findViewById(R.id.tvSensor);
        btnCarregar = findViewById(R.id.btnCarregar);

        Button btnCriarVaga = findViewById(R.id.btnCriarVaga);
        btnCriarVaga.setOnClickListener(v -> mostrarDialogoCriacao());

        btnCarregar.setOnClickListener(v -> carregarGrupos());

        // Inicialização do acelerómetro nativo
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        currentAccel = SensorManager.GRAVITY_EARTH;
        lastAccel = SensorManager.GRAVITY_EARTH;
    }

    private void carregarGrupos() {
        tvResultado.setText("A ligar ao RestDB.io...");

        ApiClient.getService().getStudyGroups().enqueue(new Callback<List<StudyGroup>>() {
            @Override
            public void onResponse(Call<List<StudyGroup>> call, Response<List<StudyGroup>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<StudyGroup> grupos = response.body();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Total de grupos: ").append(grupos.size()).append("\n\n");
                    for (StudyGroup g : grupos) {
                        sb.append("• ").append(g.getTitle())
                                .append(" [").append(g.getSubject()).append("]")
                                .append(" - Vagas: ").append(g.getVacancies()).append("\n");
                    }
                    tvResultado.setText(sb.toString());
                    Toast.makeText(MainActivity.this, "Grupos carregados com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    tvResultado.setText("Erro na API: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<StudyGroup>> call, Throwable t) {
                tvResultado.setText("Erro de rede: " + t.getMessage());
            }
        });
    }

    private void mostrarDialogoCriacao() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Publicar Nova Vaga");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final android.widget.EditText inputTitle = new android.widget.EditText(this);
        inputTitle.setHint("Título (ex: Grupo DSSMV)");
        layout.addView(inputTitle);

        final android.widget.EditText inputSubject = new android.widget.EditText(this);
        inputSubject.setHint("Disciplina (ex: DSSMV)");
        layout.addView(inputSubject);

        final android.widget.EditText inputVacancies = new android.widget.EditText(this);
        inputVacancies.setHint("Vagas em falta (ex: 1)");
        inputVacancies.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputVacancies);

        builder.setView(layout);

        builder.setPositiveButton("Publicar", (dialog, which) -> {
            String title = inputTitle.getText().toString();
            String subject = inputSubject.getText().toString();
            int vacancies = 1;
            try {
                vacancies = Integer.parseInt(inputVacancies.getText().toString());
            } catch (NumberFormatException ignored) {}

            StudyGroup novoGrupo = new StudyGroup(title, subject, vacancies);

            ApiClient.getService().createStudyGroup(novoGrupo).enqueue(new Callback<StudyGroup>() {
                @Override
                public void onResponse(Call<StudyGroup> call, Response<StudyGroup> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Vaga criada com sucesso!", Toast.LENGTH_SHORT).show();
                        carregarGrupos(); // Atualiza a lista logo a seguir
                    } else {
                        Toast.makeText(MainActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<StudyGroup> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Falha de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        lastAccel = currentAccel;
        currentAccel = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = currentAccel - lastAccel;

        // Deteção do gesto físico de agitação (Shake)
        if (delta > 11) {
            tvSensor.setText("Shake detetado! A atualizar lista...");
            carregarGrupos();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
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