package com.example.eventjoy.models;

import com.example.eventjoy.enums.ReportReason;
import com.example.eventjoy.enums.ReportStatus;

import java.time.LocalDateTime;

public class Report extends DomainEntity{

    private ReportReason reportReason;
    private String reportDescription;
    private String reportedUserId;
    private String reporterUserId;
    private String groupId;
    private String reportedAt;
    private ReportStatus reportStatus;

    public Report() {
        super();
    }

    public ReportReason getReportReason() {
        return reportReason;
    }

    public void setReportReason(ReportReason reportReason) {
        this.reportReason = reportReason;
    }

    public String getReportDescription() {
        return reportDescription;
    }

    public void setReportDescription(String reportDescription) {
        this.reportDescription = reportDescription;
    }

    public String getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(String reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReporterUserId() {
        return reporterUserId;
    }

    public void setReporterUserId(String reporterUserId) {
        this.reporterUserId = reporterUserId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(String reportedAt) {
        this.reportedAt = reportedAt;
    }

    public ReportStatus getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(ReportStatus reportStatus) {
        this.reportStatus = reportStatus;
    }

    @Override
    public String toString() {
        return "Report{" +
                "reportReason=" + reportReason +
                ", reportDescription='" + reportDescription + '\'' +
                ", reportedUserId='" + reportedUserId + '\'' +
                ", reporterUserId='" + reporterUserId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", reportedAt='" + reportedAt + '\'' +
                ", reportStatus=" + reportStatus +
                '}';
    }
}
