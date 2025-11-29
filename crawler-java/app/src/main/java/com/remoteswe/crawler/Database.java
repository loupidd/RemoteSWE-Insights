
package com.remoteswe.crawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class Database {

    private static final String URL = "jdbc:postgresql://localhost:5432/remoteswe";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void insertJob(Job job) throws Exception {
        String sql = "INSERT INTO jobs(job_title, company_name, location, salary_min, salary_max, job_type, tags, url, source, posted_date, crawled_at, job_description_raw, experience_level) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, job.jobTitle);
            stmt.setString(2, job.companyName);
            stmt.setString(3, job.location);
            if (job.salaryMin != null)
                stmt.setDouble(4, job.salaryMin);
            else
                stmt.setNull(4, java.sql.Types.NUMERIC);
            if (job.salaryMax != null)
                stmt.setDouble(5, job.salaryMax);
            else
                stmt.setNull(5, java.sql.Types.NUMERIC);
            stmt.setString(6, job.jobType);
            stmt.setArray(7, conn.createArrayOf("text", job.tags != null ? job.tags.toArray() : new String[] {}));
            stmt.setString(8, job.url);
            stmt.setString(9, job.source);
            if (job.postedDate != null)
                stmt.setObject(10, job.postedDate);
            stmt.setObject(11, job.crawledAt);
            stmt.setString(12, job.jobDescriptionRaw);
            stmt.setString(13, job.experienceLevel);

            stmt.executeUpdate();
        }
    }
}
