package com.example.installmentapp.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.installmentapp.R;
import com.example.installmentapp.database.InstallmentDbHelper;
import com.example.installmentapp.model.Installment;
import com.example.installmentapp.util.DateConverter;

import java.util.Objects;

public class AddEditInstallmentFragment extends Fragment {

    private EditText editTextTitle;
    private EditText editTextAmount;
    private EditText editTextDueDate;
    private EditText editTextNotifyDays;
    private boolean isEditMode = false;
    private int installmentId = -1;

    private InstallmentDbHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_edit_installment, container, false);
        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextAmount = view.findViewById(R.id.editTextAmount);
        editTextDueDate = view.findViewById(R.id.editTextDueDate);
        editTextNotifyDays = view.findViewById(R.id.editTextNotifyDays);
        Button buttonSave = view.findViewById(R.id.buttonSave);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);

        dbHelper = new InstallmentDbHelper(requireContext());

        // If arguments are present, we are in edit mode
        if (getArguments() != null) {
            isEditMode = true;
            installmentId = getArguments().getInt("installmentId", -1);
            String title = getArguments().getString("title");
            double amount = getArguments().getDouble("amount");
            long dueDateMillis = getArguments().getLong("dueDateMillis");
            int notifyDays = getArguments().getInt("notifyDays");

            editTextTitle.setText(title);
            editTextAmount.setText(String.valueOf(amount));
            editTextDueDate.setText(DateConverter.gregorianToPersian(dueDateMillis));
            editTextNotifyDays.setText(String.valueOf(notifyDays));
        }

        buttonSave.setOnClickListener(v -> saveInstallment());
        buttonCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    private void saveInstallment() {
        String title = editTextTitle.getText().toString().trim();
        String amountStr = editTextAmount.getText().toString().trim();
        String dueDateStr = editTextDueDate.getText().toString().trim();
        String notifyDaysStr = editTextNotifyDays.getText().toString().trim();

        if (title.isEmpty()) {
            editTextTitle.setError("Title is required");
            return;
        }
        if (amountStr.isEmpty()) {
            editTextAmount.setError("Amount is required");
            return;
        }
        if (dueDateStr.isEmpty()) {
            editTextDueDate.setError("Due date is required");
            return;
        }
        if (notifyDaysStr.isEmpty()) {
            editTextNotifyDays.setError("Notify days is required");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            editTextAmount.setError("Invalid amount");
            return;
        }

        long dueDateMillis = DateConverter.persianToGregorianMillis(dueDateStr);
        if (dueDateMillis == -1) {
            editTextDueDate.setError("Invalid date format. Use yyyy/MM/dd");
            return;
        }

        int notifyDays;
        try {
            notifyDays = Integer.parseInt(notifyDaysStr);
            if (notifyDays < 0) {
                editTextNotifyDays.setError("Notify days cannot be negative");
                return;
            }
        } catch (NumberFormatException e) {
            editTextNotifyDays.setError("Invalid number");
            return;
        }

        Installment installment = new Installment();
        installment.setTitle(title);
        installment.setAmount(amount);
        installment.setDueDateMillis(dueDateMillis);
        installment.setNotifyDaysBefore(notifyDays);

        if (isEditMode && installmentId != -1) {
            installment.setId(installmentId);
            dbHelper.updateInstallment(installment);
            Toast.makeText(requireContext(), "Installment updated", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addInstallment(installment);
            Toast.makeText(requireContext(), "Installment added", Toast.LENGTH_SHORT).show();
        }

        requireActivity().getSupportFragmentManager().popBackStack();
    }
}