package com.project.revhive.social.service;

import com.project.revhive.social.dto.UserProfileDto;
import com.project.revhive.social.model.Report;
import com.project.revhive.social.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public ReportService(
            ReportRepository reportRepository,
            RestTemplate restTemplate,
            @Value("${app.services.user-service}") String userServiceUrl
    ) {
        this.reportRepository = reportRepository;
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    public Report createReport(Report report) {
        report.setStatus("PENDING");
        Report saved = reportRepository.save(report);
        enrichReport(saved);
        return saved;
    }

    public List<Report> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        for (Report r : reports) {
            enrichReport(r);
        }
        return reports;
    }

    public Report getReportById(Long id) {
        Report r = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        enrichReport(r);
        return r;
    }

    public Report updateStatus(Long id, String status) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setStatus(status);

        Report saved = reportRepository.save(report);
        enrichReport(saved);
        return saved;
    }

    private void enrichReport(Report r) {
        r.setTargetType("POST");
        if (r.getReporterId() != null && r.getReporterId() != 0) {
            try {
                String url = userServiceUrl + "/api/users/" + r.getReporterId();
                
                String authHeader = getAuthHeader();
                UserProfileDto profile = null;
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    try {
                        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                        headers.set("Authorization", authHeader);
                        org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
                        
                        profile = restTemplate.exchange(
                            url,
                            org.springframework.http.HttpMethod.GET,
                            entity,
                            UserProfileDto.class
                        ).getBody();
                    } catch (Exception ex) {
                        // fallback to unauthenticated call if exchange fails
                    }
                }
                
                if (profile == null) {
                    profile = restTemplate.getForObject(url, UserProfileDto.class);
                }
                
                if (profile != null && profile.getUsername() != null) {
                    r.setReporterUsername(profile.getUsername());
                } else {
                    r.setReporterUsername("User_" + r.getReporterId());
                }
            } catch (Exception e) {
                r.setReporterUsername("User_" + r.getReporterId());
            }
        } else {
            r.setReporterUsername("Anonymous");
        }
    }

    private String getAuthHeader() {
        try {
            org.springframework.web.context.request.ServletRequestAttributes attributes = 
                (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getHeader("Authorization");
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}
