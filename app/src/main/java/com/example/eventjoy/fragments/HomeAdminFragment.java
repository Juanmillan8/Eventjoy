package com.example.eventjoy.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.eventjoy.R;
import com.example.eventjoy.adapters.GroupAdapter;
import com.example.eventjoy.adapters.InvitationAdapter;
import com.example.eventjoy.adapters.ReportAdapter;
import com.example.eventjoy.callbacks.GroupsCallback;
import com.example.eventjoy.callbacks.ReportsCallback;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Report;
import com.example.eventjoy.services.ReportService;
import com.example.eventjoy.services.UserGroupService;

import java.util.ArrayList;
import java.util.List;

public class HomeAdminFragment extends Fragment {

    private View rootView;
    private ListView lvReports;
    private ReportService reportService;
    private ReportAdapter reportAdapter;
    private List<Report> reportList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home_admin, container, false);

        loadServices();
        loadComponents();

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        reportService.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        startListeningReports();
    }

    private void startListeningReports() {
        reportService.stopListening();
        reportService.getPendingReports(new ReportsCallback() {
            @Override
            public void onSuccess(List<Report> reports) {
                reportList = new ArrayList<>();
                reportList = reports;
                reportAdapter = new ReportAdapter(getContext(), reports, true);
                lvReports.setAdapter(reportAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error querying database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComponents() {
        lvReports = rootView.findViewById(R.id.lvReports);
        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(getContext(), reportList, true);
    }

    private void loadServices() {
        reportService = new ReportService(getContext());
    }
}