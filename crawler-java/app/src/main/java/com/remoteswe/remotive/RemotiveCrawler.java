package com.remoteswe.remotive;

import com.remoteswe.crawler.Crawler;
import com.remoteswe.crawler.Job;
import com.remoteswe.crawler.Database;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class RemotiveCrawler implements Crawler {

    private static final String URL = "https://remotive.com/remote-jobs/software-dev";

    @Override
    public List<Job> crawl() throws Exception {
        List<Job> jobs = new ArrayList<>();

        Document doc = Jsoup.connect(URL)
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .get();

        Elements jobCards = doc.select("li.job-tile, div.job-list-item");

        for (Element card : jobCards) {
            String title = card.select("a, h3").text();
            if (title.isEmpty())
                continue;

            Job job = new Job();
            job.jobTitle = title;
            job.companyName = card.select("span.company, .company-name").text();
            job.location = "Remote";

            Element link = card.selectFirst("a");
            if (link != null) {
                String href = link.attr("href");
                job.url = href.startsWith("http") ? href : "https://remotive.com" + href;
            }
            job.source = "Remotive";

            jobs.add(job);
        }

        System.out.println("Remotive: crawled " + jobs.size() + " jobs");
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