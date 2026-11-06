package com.example.studymatch;

import android.app.Activity;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public final class MemberDetailsDialog {

    public interface OnMemberConfirmedListener {
        void onMemberConfirmed(String name, String studentNumber);
    }

    private MemberDetailsDialog() {
    }

    public static void show(Activity activity, OnMemberConfirmedListener listener) {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * activity.getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, 0);

        TextView notice = new TextView(activity);
        notice.setText("O teu nome e número de estudante ficarão visíveis aos membros deste grupo.");
        layout.addView(notice);

        EditText nameInput = new EditText(activity);
        nameInput.setHint("Nome");
        nameInput.setSingleLine(true);
        layout.addView(nameInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText studentNumberInput = new EditText(activity);
        studentNumberInput.setHint("Número de estudante do ISEP");
        studentNumberInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        studentNumberInput.setSingleLine(true);
        layout.addView(studentNumberInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        CheckBox consentCheckbox = new CheckBox(activity);
        consentCheckbox.setText("Concordo em partilhar estes dados com os membros do grupo.");
        layout.addView(consentCheckbox, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Entrar no grupo")
                .setView(layout)
                .setPositiveButton("Confirmar e entrar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    String name = nameInput.getText().toString().trim();
                    String studentNumber = studentNumberInput.getText().toString().trim();
                    if (name.isEmpty() || studentNumber.isEmpty()) {
                        Toast.makeText(activity, "Preenche o nome e o número de estudante.", Toast.LENGTH_SHORT).show();
                    } else if (!consentCheckbox.isChecked()) {
                        Toast.makeText(activity, "Confirma a partilha dos dados para entrar no grupo.", Toast.LENGTH_SHORT).show();
                    } else {
                        dialog.dismiss();
                        listener.onMemberConfirmed(name, studentNumber);
                    }
                }));

        dialog.show();
    }
}
