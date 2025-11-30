package com.remoteswe.arbeitnow;

import com.remoteswe.crawler.Crawler;
import com.remoteswe.crawler.Job;
import com.remoteswe.crawler.Database;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

public class ArbeitNowCrawler implements Crawler {

    @Override
    public List<Job> crawl() throws Exception {
        List<Job> jobs = new ArrayList<>();

        // ArbeitNow has free API with lots of remote jobs
        String url = "https://www.arbeitnow.com/api/job-board-api";

        Connection.Response response = Jsoup.connect(url)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .execute();

        String json = response.body();
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

        if (!obj.has("data")) {
            return jobs;
        }

        JsonArray jobsArray = obj.getAsJsonArray("data");

        for (JsonElement elem : jobsArray) {
            JsonObject jobObj = elem.getAsJsonObject();

            // Filter for remote only
            String remote = jobObj.has("remote") ? jobObj.get("remote").getAsString() : "false";
            if (!remote.equals("true"))
                continue;

            String title = jobObj.has("title") ? jobObj.get("title").getAsString() : "";
            if (title.isEmpty())
                continue;

            Job job = new Job();
            job.jobTitle = title;
            job.companyName = jobObj.has("company_name") ? jobObj.get("company_name").getAsString() : "";
            job.location = "Remote";
            job.url = jobObj.has("url") ? jobObj.get("url").getAsString() : "";
            job.source = "ArbeitNow";

            jobs.add(job);
        }

        System.out.println("ArbeitNow API: Got " + jobs.size() + " jobs");
        return jobs;
    }

    @Override
    public void insertToDatabase(List<Job> jobs) throws Exception {
        for (Job job : jobs) {
            try {
                Database.insertJob(job);
            } catch (Exception e) {
                // Silent fail
            }
        }
    }
}