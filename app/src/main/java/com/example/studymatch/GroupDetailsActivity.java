package com.example.studymatch;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupDetailsActivity extends AppCompatActivity {

    private String groupId;
    private TextView groupTitle;
    private TextView memberCount;
    private TextView status;
    private ProgressBar progressBar;
    private Button reloadButton;
    private StudyGroupMemberAdapter memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        groupId = getIntent().getStringExtra("group_id");
        groupTitle = findViewById(R.id.tvDetailsGroupTitle);
        memberCount = findViewById(R.id.tvMemberCount);
        status = findViewById(R.id.tvMemberStatus);
        progressBar = findViewById(R.id.progressGroupMembers);
        reloadButton = findViewById(R.id.btnReloadGroupMembers);
        RecyclerView membersList = findViewById(R.id.rvGroupMembers);

        memberAdapter = new StudyGroupMemberAdapter();
        membersList.setLayoutManager(new LinearLayoutManager(this));
        membersList.setAdapter(memberAdapter);

        findViewById(R.id.btnCloseGroupDetails).setOnClickListener(view -> finish());
        reloadButton.setOnClickListener(view -> loadGroupMembers());

        if (groupId == null || groupId.trim().isEmpty()) {
            status.setText("Não foi possível identificar este grupo.");
            reloadButton.setVisibility(View.GONE);
        } else {
            groupTitle.setText(getIntent().getStringExtra("group_title"));
            loadGroupMembers();
        }
    }

    private void loadGroupMembers() {
        progressBar.setVisibility(View.VISIBLE);
        reloadButton.setEnabled(false);
        status.setVisibility(View.GONE);

        ApiClient.getService().getStudyGroups().enqueue(new Callback<List<StudyGroup>>() {
            @Override
            public void onResponse(Call<List<StudyGroup>> call, Response<List<StudyGroup>> response) {
                progressBar.setVisibility(View.GONE);
                reloadButton.setEnabled(true);
                if (!response.isSuccessful() || response.body() == null) {
                    showError("Não foi possível carregar os membros (HTTP " + response.code() + ").");
                    return;
                }

                for (StudyGroup group : response.body()) {
                    if (groupId.equals(group.getId())) {
                        groupTitle.setText(group.getTitle() == null ? "Grupo de estudo" : group.getTitle());
                        List<StudyGroupMember> members = group.getMembers();
                        memberAdapter.setMembers(members);
                        int count = members == null ? 0 : members.size();
                        memberCount.setText("Membros: " + count);
                        if (count == 0) {
                            showStatus("Ainda não há membros registados neste grupo.");
                        }
                        return;
                    }
                }

                showError("Este grupo já não está disponível.");
            }

            @Override
            public void onFailure(Call<List<StudyGroup>> call, Throwable error) {
                progressBar.setVisibility(View.GONE);
                reloadButton.setEnabled(true);
                String detail = error.getLocalizedMessage();
                showError("Falha de rede ao carregar os membros"
                        + (detail == null || detail.trim().isEmpty() ? "." : ": " + detail));
            }
        });
    }

    private void showError(String message) {
        memberAdapter.setMembers(null);
        memberCount.setText("Membros: —");
        showStatus(message);
    }

    private void showStatus(String message) {
        status.setText(message);
        status.setVisibility(View.VISIBLE);
    }
}
