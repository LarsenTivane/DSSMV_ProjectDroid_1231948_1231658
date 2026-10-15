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