package com.remoteswe.rss;

import com.remoteswe.crawler.Crawler;
import com.remoteswe.crawler.Job;
import com.remoteswe.crawler.Database;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RSSCrawler implements Crawler {

    private static final Map<String, String> RSS_SOURCES = new HashMap<String, String>() {
        {
            put("https://www.python.org/jobs/feed/rss/", "Python.org");
            put("https://jobs.drupal.org/all-jobs/feed", "Drupal Jobs");
            put("https://himalayas.app/jobs/rss", "Himalayas RSS");
            put("https://remotive.com/feed", "Remotive RSS");
            put("https://www.flexjobs.com/rss/remote-software-developer-jobs", "FlexJobs Dev");
            put("https://www.flexjobs.com/rss/remote-web-developer-jobs", "FlexJobs Web");
            put("https://www.flexjobs.com/rss/remote-devops-engineer-jobs", "FlexJobs DevOps");
        }
    };

    @Override
    public List<Job> crawl() throws Exception {
        List<Job> allJobs = new ArrayList<>();

        for (Map.Entry<String, String> entry : RSS_SOURCES.entrySet()) {
            String feedUrl = entry.getKey();
            String sourceName = entry.getValue();

            try {
                Thread.sleep(1000);
                List<Job> feedJobs = crawlGenericRSS(feedUrl, sourceName);
                allJobs.addAll(feedJobs);
                System.out.println("✓ " + sourceName + ": Got " + feedJobs.size() + " jobs");
            } catch (Exception e) {
                System.err.println("✗ " + sourceName + " failed: " + e.getMessage());
            }
        }

        System.out.println("RSS Feeds Total: " + allJobs.size() + " jobs");
        return allJobs;
    }

    private List<Job> crawlGenericRSS(String feedUrl, String source) throws Exception {
        List<Job> jobs = new ArrayList<>();

        Document doc = Jsoup.connect(feedUrl)
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .ignoreContentType(true)
                .get();

        Elements items = doc.select("item, entry");

        for (Element item : items) {
            String title = item.select("title").text();
            String link = item.select("link").text();

            // Handle Atom format
            if (link.isEmpty()) {
                Element linkElem = item.selectFirst("link");
                if (linkElem != null) {
                    link = linkElem.attr("href");
                }
            }

            if (title.isEmpty())
                continue;

            // Extract company and job title
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
            job.source = source;

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

            }
        }
    }
}