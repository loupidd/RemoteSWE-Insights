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

    private static final String URL = "https://wellfound.com/remote-jobs"; // adjust URL if needed

    @Override
    public List<Job> crawl() throws Exception {
        List<Job> jobs = new ArrayList<>();
        Document doc = Jsoup.connect(URL).userAgent("Mozilla/5.0").get();
        Elements listings = doc.select(".job"); // adjust selector based on page

        for (Element elem : listings) {
            Job job = new Job();
            job.jobTitle = elem.select(".job-title").text();
            job.companyName = elem.select(".company-name").text();
            job.location = elem.select(".location").text();
            job.url = elem.select("a").attr("href");
            job.source = "Wellfound";
            jobs.add(job);
        }
        return jobs;
    }

    @Override
    public void insertToDatabase(List<Job> jobs) throws Exception {
        for (Job job : jobs) {
            Database.insertJob(job);
        }
    }
}
