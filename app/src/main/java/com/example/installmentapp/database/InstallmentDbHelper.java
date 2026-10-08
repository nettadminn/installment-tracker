package com.example.installmentapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.installmentapp.model.Installment;

import java.util.ArrayList;
import java.util.List;

public class InstallmentDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "installments.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_INSTALLMENTS = "installments";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_DUE_DATE = "due_date";
    private static final String COLUMN_NOTIFY_DAYS = "notify_days";

    public InstallmentDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_INSTALLMENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_AMOUNT + " REAL,"
                + COLUMN_DUE_DATE + " INTEGER,"
                + COLUMN_NOTIFY_DAYS + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INSTALLMENTS);
        onCreate(db);
    }

    // CRUD operations
    public long addInstallment(Installment installment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, installment.getTitle());
        values.put(COLUMN_AMOUNT, installment.getAmount());
        values.put(COLUMN_DUE_DATE, installment.getDueDateMillis());
        values.put(COLUMN_NOTIFY_DAYS, installment.getNotifyDaysBefore());
        long id = db.insert(TABLE_INSTALLMENTS, null, values);
        db.close();
        return id;
    }

    public Installment getInstallment(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_INSTALLMENTS,
                new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_AMOUNT, COLUMN_DUE_DATE, COLUMN_NOTIFY_DAYS},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Installment installment = new Installment();
            installment.setId(cursor.getInt(0));
            installment.setTitle(cursor.getString(1));
            installment.setAmount(cursor.getDouble(2));
            installment.setDueDateMillis(cursor.getLong(3));
            installment.setNotifyDaysBefore(cursor.getInt(4));
            cursor.close();
            return installment;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<Installment> getAllInstallments() {
        List<Installment> installmentList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String selectAll = "SELECT * FROM " + TABLE_INSTALLMENTS;
        Cursor cursor = db.rawQuery(selectAll, null);
        if (cursor.moveToFirst()) {
            do {
                Installment installment = new Installment();
                installment.setId(cursor.getInt(0));
                installment.setTitle(cursor.getString(1));
                installment.setAmount(cursor.getDouble(2));
                installment.setDueDateMillis(cursor.getLong(3));
                installment.setNotifyDaysBefore(cursor.getInt(4));
                installmentList.add(installment);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return installmentList;
    }

    public int updateInstallment(Installment installment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, installment.getTitle());
        values.put(COLUMN_AMOUNT, installment.getAmount());
        values.put(COLUMN_DUE_DATE, installment.getDueDateMillis());
        values.put(COLUMN_NOTIFY_DAYS, installment.getNotifyDaysBefore());
        int count = db.update(TABLE_INSTALLMENTS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(installment.getId())});
        db.close();
        return count;
    }

    public void deleteInstallment(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_INSTALLMENTS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }
}