package com.remoteswe.crawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class Database {
    private static final String URL = "jdbc:postgresql://localhost:5432/remoteswe_db";
    private static final String USER = "remoteswe_user";
    private static final String PASSWORD = "08080701";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void insertJob(Job job) throws SQLException {
        // Normalize URL
        String cleanUrl = job.url;
        if (cleanUrl != null) {
            int q = cleanUrl.indexOf('?');
            if (q != -1)
                cleanUrl = cleanUrl.substring(0, q);
        }

        String sql = "INSERT INTO jobs(" +
                "job_title, company_name, location, salary_min, salary_max, job_type, tags, url, source, " +
                "posted_date, crawled_at, job_description_raw, experience_level, employment_type_detail, " +
                "industry, experience_required" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (url) DO NOTHING";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, job.jobTitle);
            stmt.setString(2, job.companyName);
            stmt.setString(3, job.location);

            // Salary fields - NULL if not present
            if (job.salaryMin != null && job.salaryMin > 0) {
                stmt.setDouble(4, job.salaryMin);
            } else {
                stmt.setNull(4, Types.NUMERIC);
            }

            if (job.salaryMax != null && job.salaryMax > 0) {
                stmt.setDouble(5, job.salaryMax);
            } else {
                stmt.setNull(5, Types.NUMERIC);
            }

            stmt.setString(6, job.jobType);

            // Tags array - NULL if empty
            if (job.tags != null && !job.tags.isEmpty()) {
                stmt.setArray(7, conn.createArrayOf("text", job.tags.toArray()));
            } else {
                stmt.setNull(7, Types.ARRAY);
            }

            stmt.setString(8, cleanUrl);
            stmt.setString(9, job.source);

            if (job.postedDate != null) {
                stmt.setObject(10, job.postedDate);
            } else {
                stmt.setNull(10, Types.TIMESTAMP);
            }

            stmt.setObject(11, job.crawledAt != null ? job.crawledAt : java.time.LocalDateTime.now());

            // Optional string fields - NULL if empty or null (FIXED!)
            setStringOrNull(stmt, 12, job.jobDescriptionRaw);
            setStringOrNull(stmt, 13, job.experienceLevel);
            setStringOrNull(stmt, 14, job.employmentTypeDetail);
            setStringOrNull(stmt, 15, job.industry);
            setStringOrNull(stmt, 16, job.experienceRequired);

            int rowsAffected = stmt.executeUpdate();

            // Log what was inserted
            if (rowsAffected > 0) {
                System.out.println("✓ Inserted: " + job.jobTitle + " | " + job.source);
                System.out.println("  Experience: " + (job.experienceLevel != null ? job.experienceLevel : "NULL"));
                System.out.println("  Salary: " + job.salaryMin + " - " + job.salaryMax);
                System.out.println("  Tags: " + (job.tags != null ? job.tags.size() + " tags" : "NULL"));
            } else {
                System.out.println("⊗ Duplicate (skipped): " + job.jobTitle);
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to insert job: " + job.jobTitle);
            System.err.println("  Error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Helper method: Set string value or NULL if empty/null
     */
    private static void setStringOrNull(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value != null && !value.trim().isEmpty()) {
            stmt.setString(index, value.trim());
        } else {
            stmt.setNull(index, Types.VARCHAR);
        }
    }
}