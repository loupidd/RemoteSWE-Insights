package com.remoteswe.workingnomads;

import com.remoteswe.crawler.Crawler;
import com.remoteswe.crawler.Job;
import com.remoteswe.crawler.Database;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class WorkingNomadsCrawler implements Crawler {

    @Override
    public List<Job> crawl() throws Exception {
        List<Job> allJobs = new ArrayList<>();

        String[] categories = {
                "https://www.workingnomads.com/jobs?category=development",
                "https://www.workingnomads.com/jobs?category=devops",
                "https://www.workingnomads.com/jobs?category=design"
        };

        for (String url : categories) {
            try {
                Thread.sleep(2000);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(20000)
                        .get();

                Elements jobs = doc.select("article.job, li.job-listing");

                for (Element jobElem : jobs) {
                    String title = jobElem.select("h3, .job-title").text();
                    if (title.isEmpty())
                        continue;

                    String company = jobElem.select(".company, .company-name").text();
                    String link = jobElem.select("a").attr("href");

                    Job job = new Job();
                    job.jobTitle = title;
                    job.companyName = company;
                    job.location = "Remote";
                    job.url = link.startsWith("http") ? link : "https://www.workingnomads.com" + link;
                    job.source = "WorkingNomads";

                    allJobs.add(job);
                }

                System.out.println("WorkingNomads " + url + ": " + jobs.size() + " jobs");

            } catch (Exception e) {
                System.err.println("WorkingNomads failed for " + url + ": " + e.getMessage());
            }
        }

        System.out.println("WorkingNomads Total: " + allJobs.size() + " jobs");
        return allJobs;
    }

    @Override
    public void insertToDatabase(List<Job> jobs) throws Exception {
        for (Job job : jobs) {
            try {
                Database.insertJob(job);
            } catch (Exception e) {
                // Silent
            }
        }
    }
}