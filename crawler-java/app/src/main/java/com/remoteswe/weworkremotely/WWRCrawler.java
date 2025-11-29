package com.remoteswe.weworkremotely;

import com.remoteswe.crawler.Crawler;
import com.remoteswe.crawler.Job;
import com.remoteswe.crawler.Database;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class WWRCrawler implements Crawler {

    private static final String URL = "https://weworkremotely.com/categories/remote-programming-jobs";

    @Override
    public List<Job> crawl() throws Exception {
        List<Job> jobs = new ArrayList<>();
        Document doc = Jsoup.connect(URL).userAgent("Mozilla/5.0").get();
        Elements jobSections = doc.select("section.jobs article li a");
        for (Element jobElem : jobSections) {
            Job job = new Job();
            job.jobTitle = jobElem.select("span.title").text();
            job.companyName = jobElem.select("span.company").text();
            job.location = jobElem.select("span.region").text();
            job.url = "https://weworkremotely.com" + jobElem.attr("href");
            job.source = "WeWorkRemotely";
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
