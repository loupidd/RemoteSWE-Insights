package com.remoteswe.wellfound;

import com.remoteswe.crawler.Crawler;
import com.remoteswe.crawler.Job;
import com.remoteswe.crawler.Database;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class WellfoundCrawler implements Crawler {

    private static final String URL = "https://wellfound.com/remote-jobs";

    @Override
    public List<Job> crawl() throws Exception {
        List<Job> jobs = new ArrayList<>();

        Document doc = Jsoup.connect(URL)
                .userAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .referrer("https://wellfound.com")
                .header("Accept-Language", "en-US,en;q=0.9")
                .get();

        // Adjust selector for remote job listings
        Elements listings = doc.select("div[data-test-job-card]"); // inspect the site for actual selector

        for (Element elem : listings) {
            String title = elem.select("h3").text();
            if (title.isEmpty())
                continue; // skip invalid entries

            Job job = new Job();
            job.jobTitle = title;
            job.companyName = elem.select("h4").text();
            job.location = elem.select("p[data-test-job-location]").text();
            job.url = "https://wellfound.com" + elem.select("a").attr("href");
            job.source = "Wellfound";

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
                System.err.println("Failed to insert job: " + job.jobTitle + " | " + e.getMessage());
            }
        }
    }
}
