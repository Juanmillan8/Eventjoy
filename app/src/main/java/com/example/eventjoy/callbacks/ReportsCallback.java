package com.example.eventjoy.callbacks;

import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Report;
import com.google.firebase.database.core.Repo;

import java.util.List;

public interface ReportsCallback {
    void onSuccess(List<Report> reports);
    void onFailure(Exception e);
}
