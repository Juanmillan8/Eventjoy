package com.example.eventjoy.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eventjoy.callbacks.MembersCallback;
import com.example.eventjoy.callbacks.ReportsCallback;
import com.example.eventjoy.models.Admin;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Report;
import com.example.eventjoy.models.UserEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Repo;

import java.util.ArrayList;
import java.util.List;

public class ReportService {

    private DatabaseReference databaseReferenceReports;
    private ValueEventListener reportsListener;

    public ReportService(Context context) {
        databaseReferenceReports = FirebaseDatabase.getInstance().getReference().child("reports");
    }

    public ReportService(FirebaseDatabase firebaseDatabase) {
        databaseReferenceReports = firebaseDatabase.getReference().child("reports");
    }

    public void updateReport(Report r) {
        databaseReferenceReports.child(r.getId()).setValue(r);
    }

    public String insertReport(Report r) {
        DatabaseReference newReference = databaseReferenceReports.push();
        r.setId(newReference.getKey());

        newReference.setValue(r);
        return r.getId();
    }

    public void getPendingReports(ReportsCallback callback) {
        if (reportsListener != null) {
            databaseReferenceReports.removeEventListener(reportsListener);
        }

        reportsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Report> reports = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Report report = snapshot.getValue(Report.class);
                    reports.add(report);
                }
                if (reports.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                }else{
                    callback.onSuccess(reports);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - ReportService - getPendingReports", error.getMessage());
                callback.onFailure(error.toException());
            }
        };
        databaseReferenceReports.orderByChild("reportStatus").equalTo("PENDING").addValueEventListener(reportsListener);
    }

    public void getByUserId(String userId, ReportsCallback callback) {
        if (reportsListener != null) {
            databaseReferenceReports.removeEventListener(reportsListener);
        }

        reportsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Report> reports = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Report report = snapshot.getValue(Report.class);
                    reports.add(report);
                }
                if (reports.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                }else{
                    callback.onSuccess(reports);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - ReportService - getByUserId", error.getMessage());
                callback.onFailure(error.toException());
            }
        };
        databaseReferenceReports.orderByChild("reportedUserId").equalTo(userId).addValueEventListener(reportsListener);
    }

    public void stopListening() {
        if (reportsListener != null) {
            databaseReferenceReports.removeEventListener(reportsListener);
            reportsListener = null;
        }
    }

}
