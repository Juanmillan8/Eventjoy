package com.example.eventjoy.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.eventjoy.R;
import com.example.eventjoy.activities.CreateReportsActivity;
import com.example.eventjoy.enums.ReportStatus;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Report;
import com.example.eventjoy.models.Valoration;
import com.example.eventjoy.services.MemberService;
import com.example.eventjoy.services.ReportService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

public class ReportAdapter extends ArrayAdapter<Report> {

    private List<Report> reports;
    private Context context;
    private MemberService memberService;
    private ReportService reportService;
    private Boolean isAdmin;

    //Constructor del adapter
    public ReportAdapter(Context context, List<Report> reports, Boolean isAdmin){
        super(context, 0, reports);
        this.reports = reports;
        this.context = context;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Report report = this.reports.get(position);
        memberService = new MemberService(context);
        reportService = new ReportService(context);

        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_report, parent, false);
        }

        TextView tvReportReason = convertView.findViewById(R.id.tvReportReason);
        CardView cardViewStatus = convertView.findViewById(R.id.cardViewStatus);
        TextView tvStatus = convertView.findViewById(R.id.tvStatus);
        TextView tvReportDescription = convertView.findViewById(R.id.tvReportDescription);
        TextView tvReportedBy = convertView.findViewById(R.id.tvReportedBy);
        TextView tvReportedUser = convertView.findViewById(R.id.tvReportedUser);
        LinearLayout linearLayoutApproveRejectReport = convertView.findViewById(R.id.linearLayoutApproveRejectReport);
        Button btnApproveReport = convertView.findViewById(R.id.btnApproveReport);
        Button btnRejectReport = convertView.findViewById(R.id.btnRejectReport);

        if(isAdmin){
            tvReportedUser.setVisibility(View.VISIBLE);
            linearLayoutApproveRejectReport.setVisibility(View.VISIBLE);
        }


        String reasonRaw = report.getReportReason().name().replace('_', ' ').toLowerCase();
        String reasonFormatted = reasonRaw.substring(0, 1).toUpperCase() + reasonRaw.substring(1);

        tvReportReason.setText("Reason: " + reasonFormatted);

        switch (report.getReportStatus().name()) {
            case "PENDING":
                cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.reportPending));
                tvStatus.setText("Pending");
                break;
            case "APPROVED":
                cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.reportApproved));
                tvStatus.setText("Approved");
                break;
            case "REJECTED":
                cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.reportRejected));
                tvStatus.setText("Rejected");
                break;
        }

        if (report.getReportDescription() != null && !report.getReportDescription().toString().isBlank()) {
            tvReportDescription.setText(report.getReportDescription());
        } else {
            tvReportDescription.setText("This report has no description");
        }

        memberService.getMemberById(report.getReporterUserId(), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Member member = dataSnapshot.getChildren().iterator().next().getValue(Member.class);
                tvReportedBy.setText("Reported by: " + member.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error - ReportAdapter - getMemberById", databaseError.getMessage());
            }
        });

        if(isAdmin){
            memberService.getMemberById(report.getReportedUserId(), new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Member member = dataSnapshot.getChildren().iterator().next().getValue(Member.class);
                    tvReportedUser.setText("Reported user: " + member.getUsername());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Error - ReportAdapter - getMemberById", databaseError.getMessage());
                }
            });
        }

        btnApproveReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                report.setReportStatus(ReportStatus.APPROVED);
                reportService.updateReport(report);
            }
        });

        btnRejectReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                report.setReportStatus(ReportStatus.REJECTED);
                reportService.updateReport(report);
            }
        });

        return convertView;
    }

}
