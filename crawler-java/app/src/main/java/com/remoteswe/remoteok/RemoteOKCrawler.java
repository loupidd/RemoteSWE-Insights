package com.remoteswe.remoteok;

import com.remoteswe.crawler.Crawler;
import com.remoteswe.crawler.Job;
import com.remoteswe.crawler.Database;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class RemoteOKCrawler implements Crawler {

    private static final String URL = "https://remoteok.io/remote-dev-jobs";

    @Override
    public List<Job> crawl() throws Exception {
        List<Job> jobs = new ArrayList<>();
        Document doc = Jsoup.connect(URL)
                .userAgent("Mozilla/5.0")
                .get();

        Elements rows = doc.select("tr.job"); // each job row
        for (Element row : rows) {
            Job job = new Job();
            job.jobTitle = row.select("h2").text();
            job.companyName = row.select("h3").text();
            job.location = row.select(".location").text();
            job.url = "https://remoteok.io" + row.attr("data-href");
            job.source = "RemoteOK";
            job.jobType = row.select(".time").text();
            job.tags = row.select(".tags li").eachText();
            // Salary rarely available
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
