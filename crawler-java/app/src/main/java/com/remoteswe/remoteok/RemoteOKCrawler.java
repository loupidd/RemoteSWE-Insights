package com.remoteswe.remoteok;

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
import java.util.Arrays;
import java.util.List;

public class RemoteOKCrawler implements Crawler {

    private static final List<String> EXCLUDE_KEYWORDS = Arrays.asList(
            "sales engineer", "customer success", "recruiter", "copywriter", "accountant");

    @Override
    public List<Job> crawl() throws Exception {
        List<Job> allJobs = new ArrayList<>();

        // RemoteOK API endpoints
        String[] categories = {
                "https://remoteok.io/api"
        };

        for (String apiUrl : categories) {
            List<Job> categoryJobs = crawlRemoteOKWithRetry(apiUrl);
            allJobs.addAll(categoryJobs);
        }

        System.out.println("RemoteOK Total: " + allJobs.size() + " jobs");
        return allJobs;
    }

    private List<Job> crawlRemoteOKWithRetry(String apiUrl) {
        List<Job> jobs = new ArrayList<>();
        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("RemoteOK attempt " + attempt + "...");

                // Delay
                if (attempt > 1) {
                    Thread.sleep(5000);
                }

                Connection.Response response = Jsoup.connect(apiUrl)
                        .ignoreContentType(true)
                        .userAgent(
                                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(30000)
                        .header("Accept", "application/json")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .execute();

                String json = response.body();
                JsonArray arr = JsonParser.parseString(json).getAsJsonArray();

                int validJobs = 0;
                for (JsonElement elem : arr) {
                    JsonObject obj = elem.getAsJsonObject();

                    // Skip Metadata
                    if (!obj.has("id"))
                        continue;

                    String position = obj.has("position") ? obj.get("position").getAsString() : "";
                    if (position.isEmpty())
                        continue;

                    // Exclude non-tech roles
                    if (shouldExclude(position))
                        continue;

                    Job job = new Job();
                    job.jobTitle = position;
                    job.companyName = obj.has("company") ? obj.get("company").getAsString() : "";
                    job.location = obj.has("location") ? obj.get("location").getAsString() : "Remote";
                    job.url = obj.has("url") ? "https://remoteok.io" + obj.get("url").getAsString() : "";
                    job.source = "RemoteOK";

                    if (obj.has("tags")) {
                        JsonArray tags = obj.getAsJsonArray("tags");
                        List<String> tagList = new ArrayList<>();
                        for (JsonElement tag : tags) {
                            tagList.add(tag.getAsString());
                        }
                        job.jobType = String.join(",", tagList);
                    }

                    jobs.add(job);
                    validJobs++;
                }

                System.out.println("✓ RemoteOK: Successfully got " + validJobs + " jobs");
                return jobs;

            } catch (java.net.SocketTimeoutException e) {
                System.err.println("✗ RemoteOK attempt " + attempt + " timed out");
                if (attempt == maxRetries) {
                    System.err.println("✗ RemoteOK: All " + maxRetries + " attempts failed due to timeout");
                }
            } catch (Exception e) {
                System.err.println("✗ RemoteOK attempt " + attempt + " failed: " + e.getMessage());
                if (attempt == maxRetries) {
                    System.err.println("✗ RemoteOK: All " + maxRetries + " attempts failed");
                }
            }
        }

        return jobs; // Return empty if failed
    }

    private boolean shouldExclude(String title) {
        String lower = title.toLowerCase();
        for (String keyword : EXCLUDE_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
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