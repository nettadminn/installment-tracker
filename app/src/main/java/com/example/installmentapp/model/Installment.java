package com.example.installmentapp.model;

public class Installment {
    private int id;
    private String title;
    private double amount;
    private long dueDateMillis; // Stored in milliseconds (Gregorian)
    private int notifyDaysBefore; // 0 for same day, positive for days before

    public Installment() {}

    public Installment(String title, double amount, long dueDateMillis, int notifyDaysBefore) {
        this.title = title;
        this.amount = amount;
        this.dueDateMillis = dueDateMillis;
        this.notifyDaysBefore = notifyDaysBefore;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getDueDateMillis() {
        return dueDateMillis;
    }

    public void setDueDateMillis(long dueDateMillis) {
        this.dueDateMillis = dueDateMillis;
    }

    public int getNotifyDaysBefore() {
        return notifyDaysBefore;
    }

    public void setNotifyDaysBefore(int notifyDaysBefore) {
        this.notifyDaysBefore = notifyDaysBefore;
    }
}