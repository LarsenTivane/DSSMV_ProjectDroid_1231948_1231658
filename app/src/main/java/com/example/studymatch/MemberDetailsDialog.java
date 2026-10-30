package com.example.studymatch;

import android.content.Context;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public final class MemberDetailsDialog {
    public interface OnMemberDetailsEnteredListener {
        void onMemberDetailsEntered(String name, String studentNumber);
    }

    private MemberDetailsDialog() {
    }

    public static void show(Context context, OnMemberDetailsEnteredListener listener) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * context.getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding / 2, padding, 0);

        EditText nameInput = new EditText(context);
        nameInput.setHint("Nome");
        nameInput.setSingleLine(true);
        layout.addView(nameInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText studentNumberInput = new EditText(context);
        studentNumberInput.setHint("Número de estudante");
        studentNumberInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        studentNumberInput.setSingleLine(true);
        layout.addView(studentNumberInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Entrar no grupo")
                .setView(layout)
                .setPositiveButton("Confirmar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    String name = nameInput.getText().toString().trim();
                    String studentNumber = studentNumberInput.getText().toString().trim();
                    if (name.isEmpty() || studentNumber.isEmpty()) {
                        Toast.makeText(context, "Preencha o nome e o número de estudante.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listener.onMemberDetailsEntered(name, studentNumber);
                    dialog.dismiss();
                }));
        dialog.show();
    }
}
