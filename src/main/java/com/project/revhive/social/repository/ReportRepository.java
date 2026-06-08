package com.project.revhive.social.repository;

import com.project.revhive.social.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
