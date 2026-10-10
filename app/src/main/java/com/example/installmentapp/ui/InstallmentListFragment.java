package com.example.installmentapp.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.installmentapp.adapter.InstallmentAdapter;
import com.example.installmentapp.R;
import com.example.installmentapp.database.InstallmentDbHelper;
import com.example.installmentapp.model.Installment;
import com.example.installmentapp.receiver.NotificationReceiver;

import java.util.List;

public class InstallmentListFragment extends Fragment implements InstallmentAdapter.OnItemClickListener {

    private static final String TAG = "InstallmentListFragment";

    private RecyclerView recyclerView;
    private InstallmentAdapter adapter;
    private InstallmentDbHelper dbHelper;
    private View emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_installment_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.textViewEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new InstallmentAdapter(null);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        dbHelper = new InstallmentDbHelper(requireContext());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadInstallments();
        scheduleNotifications();
    }

    private void loadInstallments() {
        List<Installment> installments = dbHelper.getAllInstallments();
        if (installments.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.setInstallments(installments);
        }
    }

    @Override
    public void onItemClick(Installment installment) {
        // Show edit dialog/fragment
        Bundle bundle = new Bundle();
        bundle.putInt("installmentId", installment.getId());
        bundle.putString("title", installment.getTitle());
        bundle.putDouble("amount", installment.getAmount());
        bundle.putLong("dueDateMillis", installment.getDueDateMillis());
        bundle.putInt("notifyDays", installment.getNotifyDaysBefore());

        AddEditInstallmentFragment fragment = new AddEditInstallmentFragment();
        fragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDeleteClick(Installment installment) {
        dbHelper.deleteInstallment(installment.getId());
        loadInstallments();
        scheduleNotifications(); // Reschedule after deletion
        Toast.makeText(requireContext(), "قسط حذف شد", Toast.LENGTH_SHORT).show();
    }

    private void scheduleNotifications() {
        try {
            // Check exact alarm permission (Android 14+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Cannot schedule exact alarms - permission not granted");
                    return;
                }
            }

            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(requireContext(), NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            // Cancel existing alarms
            alarmManager.cancel(pendingIntent);

            // Get all installments and set the earliest notification
            List<Installment> installments = dbHelper.getAllInstallments();
            long earliestTrigger = Long.MAX_VALUE;
            for (Installment inst : installments) {
                long notifyTime = inst.getDueDateMillis() - (inst.getNotifyDaysBefore() * 24 * 60 * 60 * 1000L);
                if (notifyTime < earliestTrigger) {
                    earliestTrigger = notifyTime;
                }
            }

            if (earliestTrigger != Long.MAX_VALUE) {
                // Set alarm for the earliest notification
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, earliestTrigger, pendingIntent);
                Log.d(TAG, "Alarm scheduled for: " + earliestTrigger);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException scheduling alarm - exact alarm permission may be missing", e);
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notifications", e);
        }
    }
}