package com.example.eventjoy.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventjoy.R;
import com.example.eventjoy.enums.ReportReason;
import com.example.eventjoy.enums.ReportStatus;
import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Report;
import com.example.eventjoy.services.ReportService;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CreateReportsActivity extends AppCompatActivity {

    private Toolbar toolbarActivity;
    private AutoCompleteTextView autoCompleteReportReason;
    private TextInputEditText textInputEditTextDescription;
    private Button btnCreateReport;
    private SharedPreferences sharedPreferences;
    private String idCurrentUser;
    private Bundle getData;
    private String memberId, groupId;
    private ReportService reportService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_reports);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadServices();
        loadComponents();

        btnCreateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createReport();
            }
        });

    }
    //TODO solo tengo que obtener el objeto grupo, añadirle la id del grupo al reporte y crearlo, luego haber si funciona
    private void createReport(){
        Report report = new Report();
        report.setReportDescription(textInputEditTextDescription.getText().toString());
        report.setReportReason(ReportReason.valueOf(autoCompleteReportReason.getText().toString().toUpperCase().replace(" ", "_")));
        report.setReportStatus(ReportStatus.PENDING);
        report.setReporterUserId(idCurrentUser);
        report.setReportedUserId(memberId);
        report.setGroupId(groupId);

        LocalDateTime localDateTime = LocalDateTime.now().withNano(0);
        ZonedDateTime utcDateTime = localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
        String formattedToday = utcDateTime.format(DateTimeFormatter.ISO_INSTANT);

        report.setReportedAt(formattedToday);
        reportService.insertReport(report);
        Toast.makeText(getApplicationContext(), "Report created successfully", Toast.LENGTH_SHORT).show();
        finish();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void loadComponents(){
        sharedPreferences = getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        idCurrentUser = sharedPreferences.getString("id", "");
        btnCreateReport = findViewById(R.id.btnCreateReport);
        textInputEditTextDescription = findViewById(R.id.textInputEditTextDescription);
        autoCompleteReportReason = findViewById(R.id.autoCompleteReportReason);
        toolbarActivity = findViewById(R.id.toolbarActivity);
        setSupportActionBar(toolbarActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] reportsReason = {"Offensive language", "Spam", "Harassment", "Violence", "Unfair play", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reportsReason);
        autoCompleteReportReason.setAdapter(adapter);

        getData = getIntent().getExtras();
        memberId = getData.getString("memberId");
        groupId = getData.getString("groupId");
    }

    private void loadServices(){
        reportService = new ReportService(getApplicationContext());
    }

}