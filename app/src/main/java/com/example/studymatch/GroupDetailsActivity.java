package com.example.studymatch;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupDetailsActivity extends AppCompatActivity {
    private TextView titleView;
    private TextView statusView;
    private LinearLayout membersContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        titleView = findViewById(R.id.tvGroupDetailsTitle);
        statusView = findViewById(R.id.tvMembersStatus);
        membersContainer = findViewById(R.id.layoutGroupMembers);
        Button backButton = findViewById(R.id.btnCloseGroupDetails);
        backButton.setOnClickListener(view -> finish());

        String groupId = getIntent().getStringExtra("group_id");
        String groupTitle = getIntent().getStringExtra("group_title");
        titleView.setText(groupTitle == null || groupTitle.trim().isEmpty()
                ? "Membros do grupo" : groupTitle);
        if (groupId == null || groupId.trim().isEmpty()) {
            statusView.setText("Não foi possível identificar este grupo.");
            return;
        }

        carregarMembros(groupId);
    }

    private void carregarMembros(String groupId) {
        statusView.setText("A carregar membros...");
        ApiClient.getService().getStudyGroup(groupId).enqueue(new Callback<StudyGroup>() {
            @Override
            public void onResponse(Call<StudyGroup> call, Response<StudyGroup> response) {
                if (!response.isSuccessful()) {
                    statusView.setText("Erro ao carregar membros (HTTP " + response.code() + ").");
                    return;
                }
                StudyGroup group = response.body();
                if (group == null) {
                    statusView.setText("Grupo não encontrado.");
                    return;
                }

                titleView.setText(group.getTitle() == null ? "Membros do grupo" : group.getTitle());
                mostrarMembros(group.getMembers());
            }

            @Override
            public void onFailure(Call<StudyGroup> call, Throwable error) {
                String detail = error.getLocalizedMessage();
                statusView.setText("Falha de rede ao carregar membros"
                        + (detail == null || detail.trim().isEmpty() ? "." : ": " + detail));
                Toast.makeText(GroupDetailsActivity.this, "Não foi possível ligar ao servidor.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarMembros(List<StudyGroupMember> members) {
        membersContainer.removeAllViews();
        if (members.isEmpty()) {
            statusView.setText("Este grupo ainda não tem membros.");
            return;
        }

        statusView.setText("Membros: " + members.size());
        for (StudyGroupMember member : members) {
            if (member == null) {
                continue;
            }
            String name = member.getName();
            if (name == null || name.trim().isEmpty()) {
                name = "Estudante";
            }
            String studentNumber = member.getStudentNumber();

            TextView memberView = new TextView(this);
            memberView.setText(studentNumber == null || studentNumber.trim().isEmpty()
                    ? name : name + "\nN.º estudante: " + studentNumber);
            memberView.setTextSize(16);
            memberView.setPadding(16, 16, 16, 16);
            memberView.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = getResources().getDimensionPixelSize(
                    android.R.dimen.app_icon_size) / 4;
            membersContainer.addView(memberView, params);
        }
    }
}
