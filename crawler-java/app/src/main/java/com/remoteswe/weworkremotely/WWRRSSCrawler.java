package com.remoteswe.weworkremotely;

import com.remoteswe.crawler.Crawler;
import com.remoteswe.crawler.Job;
import com.remoteswe.crawler.Database;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WWRRSSCrawler implements Crawler {

    private static final String[] RSS_FEEDS = {
            // Programming
            "https://weworkremotely.com/categories/remote-programming-jobs.rss",
            "https://weworkremotely.com/categories/remote-full-stack-programming-jobs.rss",
            "https://weworkremotely.com/categories/remote-back-end-programming-jobs.rss",
            "https://weworkremotely.com/categories/remote-front-end-programming-jobs.rss",

            // DevOps
            "https://weworkremotely.com/categories/remote-devops-sysadmin-jobs.rss",

            // Design
            "https://weworkremotely.com/categories/remote-design-jobs.rss",

            // Product
            "https://weworkremotely.com/categories/remote-product-jobs.rss",

            // ALL JOBS
            "https://weworkremotely.com/remote-jobs.rss",

            //
            "https://weworkremotely.com/remote-full-time-jobs.rss",
            "https://weworkremotely.com/remote-contract-jobs.rss"
    };

    @Override
    public List<Job> crawl() throws Exception {
        List<Job> allJobs = new ArrayList<>();

        for (String feedUrl : RSS_FEEDS) {
            try {
                Thread.sleep(1000); // Be polite
                List<Job> feedJobs = crawlRSSFeed(feedUrl);
                allJobs.addAll(feedJobs);
                System.out.println("✓ WWR RSS: Got " + feedJobs.size() + " jobs from " + feedUrl);
            } catch (Exception e) {
                System.err.println("✗ Failed to crawl " + feedUrl + ": " + e.getMessage());
            }
        }

        System.out.println("WWR RSS: Total " + allJobs.size() + " jobs crawled");
        return allJobs;
    }

    private List<Job> crawlRSSFeed(String feedUrl) throws Exception {
        List<Job> jobs = new ArrayList<>();

        Document doc = Jsoup.connect(feedUrl)
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .get();

        Elements items = doc.select("item");

        for (Element item : items) {
            String title = item.select("title").text();
            String link = item.select("link").text();
            String description = item.select("description").text();

            if (title.isEmpty())
                continue;

            // Extract company and job title (format is usually "Company: Job Title")
            String company = "";
            String jobTitle = title;

            if (title.contains(":")) {
                String[] parts = title.split(":", 2);
                company = parts[0].trim();
                jobTitle = parts[1].trim();
            }

            Job job = new Job();
            job.jobTitle = jobTitle;
            job.companyName = company;
            job.location = "Remote";
            job.url = link;
            job.source = "WeWorkRemotely";

            jobs.add(job);
        }

        return jobs;
    }

    @Override
    public void insertToDatabase(List<Job> jobs) throws Exception {
        for (Job job : jobs) {
            try {
                Database.insertJob(job);
            } catch (Exception e) {
                System.err.println("Failed to insert: " + e.getMessage());
            }
        }
    }
}