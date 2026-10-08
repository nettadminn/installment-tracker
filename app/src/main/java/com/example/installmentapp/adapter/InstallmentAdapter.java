package com.example.installmentapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.installmentapp.R;
import com.example.installmentapp.model.Installment;
import com.example.installmentapp.util.DateConverter;

import java.util.List;

public class InstallmentAdapter extends RecyclerView.Adapter<InstallmentAdapter.InstallmentViewHolder> {

    private List<Installment> installmentList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Installment installment);
        void onDeleteClick(Installment installment);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class InstallmentViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewTitle;
        public TextView textViewAmount;
        public TextView textViewDueDate;
        public TextView textViewNotify;
        public TextView textViewDelete;

        public InstallmentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewDueDate = itemView.findViewById(R.id.textViewDueDate);
            textViewNotify = itemView.findViewById(R.id.textViewNotify);
            textViewDelete = itemView.findViewById(R.id.textViewDelete);
        }
    }

    public InstallmentAdapter(List<Installment> installmentList) {
        this.installmentList = installmentList;
    }

    @NonNull
    @Override
    public InstallmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_installment, parent, false);
        return new InstallmentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InstallmentViewHolder holder, int position) {
        Installment currentItem = installmentList.get(position);

        holder.textViewTitle.setText(currentItem.getTitle());
        holder.textViewAmount.setText(String.format("%,.0f", currentItem.getAmount()));
        holder.textViewDueDate.setText(DateConverter.gregorianToPersian(currentItem.getDueDateMillis()));

        int notifyDays = currentItem.getNotifyDaysBefore();
        String notifyText;
        if (notifyDays == 0) {
            notifyText = "همه‌ روز";
        } else if (notifyDays == 1) {
            notifyText = "1 روز قبل";
        } else {
            notifyText = notifyDays + " روز قبل";
        }
        holder.textViewNotify.setText(notifyText);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentItem);
            }
        });

        holder.textViewDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(currentItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return installmentList.size();
    }

    public void setInstallments(List<Installment> installments) {
        installmentList = installments;
        notifyDataSetChanged();
    }
}