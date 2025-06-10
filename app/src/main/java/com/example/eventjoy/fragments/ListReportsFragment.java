package com.example.eventjoy.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.eventjoy.R;
import com.example.eventjoy.activities.CreateReportsActivity;
import com.example.eventjoy.activities.CreateValorationsActivity;
import com.example.eventjoy.activities.EventDetailsActivity;
import com.example.eventjoy.adapters.ReportAdapter;
import com.example.eventjoy.adapters.ValorationAdapter;
import com.example.eventjoy.callbacks.ReportsCallback;
import com.example.eventjoy.callbacks.ValorationsCallback;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Report;
import com.example.eventjoy.models.Valoration;
import com.example.eventjoy.services.MemberService;
import com.example.eventjoy.services.ReportService;
import com.example.eventjoy.services.ValorationService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListReportsFragment extends Fragment {

    private View rootView;
    private ListView lvReports;
    private ReportService reportService;
    private List<Report> reportList;
    private ReportAdapter reportAdapter;
    private SharedPreferences sharedPreferences;
    private String reportedUserId;
    private Member m;
    private Group g;
    private FloatingActionButton btnAddReport;
    private Boolean reportPending;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list_reports, container, false);

        loadServices();
        loadComponents();

        btnAddReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportPending=false;
                for (Report report : reportList) {
                    if(report.getReporterUserId().equals(sharedPreferences.getString("id", "")) && report.getReportStatus().name().equals("PENDING")){
                        Toast.makeText(getContext(), "You already have a pending report for this user", Toast.LENGTH_SHORT).show();
                        reportPending=true;
                        return;
                    }
                }
                if(!reportPending){
                    Intent createReportIntent = new Intent(getContext(), CreateReportsActivity.class);
                    createReportIntent.putExtra("memberId", m.getId());
                    createReportIntent.putExtra("groupId", g.getId());
                    startActivity(createReportIntent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        startListeningReports();
    }

    @Override
    public void onStop() {
        super.onStop();
        reportService.stopListening();
    }

    private void startListeningReports() {
        reportService.stopListening();
        reportService.getByUserId(reportedUserId, new ReportsCallback() {
            @Override
            public void onSuccess(List<Report> reports) {
                reportList = reports;
                reportAdapter = new ReportAdapter(getContext(), reports, false);
                lvReports.setAdapter(reportAdapter);
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity().getApplication(), "Error querying database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComponents(){
        reportList = new ArrayList<>();
        lvReports = rootView.findViewById(R.id.lvReports);
        sharedPreferences = getActivity().getApplication().getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        btnAddReport = rootView.findViewById(R.id.btnAddReport);

        if (getArguments() != null) {
            m = (Member) getArguments().getSerializable("member");
            g = (Group) getArguments().getSerializable("group");
            reportedUserId=m.getId();
            btnAddReport.setVisibility(View.VISIBLE);
        }else{
            reportedUserId = sharedPreferences.getString("id", "");
        }

    }

    private void loadServices(){
        reportService = new ReportService(getContext());
    }

}