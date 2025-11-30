package com.remoteswe.jsonapi;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JSONAPICrawler implements Crawler {

    @Override
    public List<Job> crawl() throws Exception {
        List<Job> allJobs = new ArrayList<>();

        // Jobicy API with multiple tags
        String[] jobicyTags = { "dev", "software", "engineer", "frontend", "backend", "fullstack", "devops", "python",
                "java", "react" };
        for (String tag : jobicyTags) {
            try {
                Thread.sleep(2000);
                allJobs.addAll(crawlJobicy(tag));
            } catch (Exception e) {
                System.err.println("Jobicy '" + tag + "' failed: " + e.getMessage());
            }
        }

        // Remotive API - call it 3 times with different categories
        String[] remotiveCategories = { "", "software-dev", "devops" }; // Empty string = ALL jobs
        for (String category : remotiveCategories) {
            try {
                Thread.sleep(2000);
                allJobs.addAll(crawlRemotive(category));
            } catch (Exception e) {
                System.err.println("Remotive '" + category + "' failed: " + e.getMessage());
            }
        }

        // Himalayas API with pagination
        try {
            allJobs.addAll(crawlHimalayas());
        } catch (Exception e) {
            System.err.println("Himalayas API failed: " + e.getMessage());
        }

        // Remove duplicates
        allJobs = removeDuplicates(allJobs);

        System.out.println("JSON APIs Total (after dedup): " + allJobs.size() + " jobs");
        return allJobs;
    }

    private List<Job> crawlJobicy(String tag) throws Exception {
        List<Job> jobs = new ArrayList<>();

        String url = "https://jobicy.com/api/v2/remote-jobs?count=50&tag=" + tag;

        Connection.Response response = Jsoup.connect(url)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .execute();

        String json = response.body();
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

        if (!obj.has("jobs")) {
            System.out.println("Jobicy '" + tag + "': No jobs field found");
            return jobs;
        }

        JsonArray jobsArray = obj.getAsJsonArray("jobs");

        for (JsonElement elem : jobsArray) {
            JsonObject jobObj = elem.getAsJsonObject();

            Job job = new Job();
            job.jobTitle = jobObj.has("jobTitle") ? jobObj.get("jobTitle").getAsString() : "";
            job.companyName = jobObj.has("companyName") ? jobObj.get("companyName").getAsString() : "";
            job.location = "Remote";
            job.url = jobObj.has("url") ? jobObj.get("url").getAsString() : "";
            job.source = "Jobicy";

            if (!job.jobTitle.isEmpty()) {
                jobs.add(job);
            }
        }

        System.out.println("Jobicy '" + tag + "': Got " + jobs.size() + " jobs");
        return jobs;
    }

    private List<Job> crawlRemotive(String category) throws Exception {
        List<Job> jobs = new ArrayList<>();

        // If category is empty, get ALL jobs
        String url = category.isEmpty()
                ? "https://remotive.com/api/remote-jobs?limit=100"
                : "https://remotive.com/api/remote-jobs?category=" + category + "&limit=100";

        Connection.Response response = Jsoup.connect(url)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .execute();

        String json = response.body();
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

        if (!obj.has("jobs")) {
            System.out.println("Remotive '" + (category.isEmpty() ? "ALL" : category) + "': No jobs field found");
            return jobs;
        }

        JsonArray jobsArray = obj.getAsJsonArray("jobs");

        for (JsonElement elem : jobsArray) {
            JsonObject jobObj = elem.getAsJsonObject();

            Job job = new Job();
            job.jobTitle = jobObj.has("title") ? jobObj.get("title").getAsString() : "";
            job.companyName = jobObj.has("company_name") ? jobObj.get("company_name").getAsString() : "";
            job.location = "Remote";
            job.url = jobObj.has("url") ? jobObj.get("url").getAsString() : "";
            job.source = "Remotive";

            if (!job.jobTitle.isEmpty()) {
                jobs.add(job);
            }
        }

        System.out.println("Remotive '" + (category.isEmpty() ? "ALL" : category) + "': Got " + jobs.size() + " jobs");
        return jobs;
    }

    private List<Job> crawlHimalayas() throws Exception {
        List<Job> allJobs = new ArrayList<>();

        int limit = 20;
        int maxPages = 50;

        for (int page = 0; page < maxPages; page++) {
            try {
                Thread.sleep(1500);

                int offset = page * limit;
                String url = "https://himalayas.app/jobs/api?limit=" + limit + "&offset=" + offset;

                Connection.Response response = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .userAgent(
                                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(20000)
                        .execute();

                String json = response.body();
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

                if (!obj.has("jobs")) {
                    System.out.println("Himalayas page " + page + ": No jobs field");
                    break;
                }

                JsonArray jobsArray = obj.getAsJsonArray("jobs");

                if (jobsArray.size() == 0) {
                    System.out.println("Himalayas page " + page + ": No more jobs");
                    break;
                }

                int addedFromPage = 0;
                int totalInPage = jobsArray.size();

                for (JsonElement elem : jobsArray) {
                    JsonObject jobObj = elem.getAsJsonObject();

                    String title = jobObj.has("title") ? jobObj.get("title").getAsString() : "";
                    if (title.isEmpty())
                        continue;

                    String lowerTitle = title.toLowerCase();

                    boolean isTechJob =
                            // Core SWE roles
                            lowerTitle.contains("engineer") ||
                                    lowerTitle.contains("developer") ||
                                    lowerTitle.contains("software") ||
                                    lowerTitle.contains("swe") ||
                                    lowerTitle.contains("programmer") ||
                                    lowerTitle.contains("coding") ||

                                    // Web + Mobile
                                    lowerTitle.contains("web") ||
                                    lowerTitle.contains("mobile") ||
                                    lowerTitle.contains("frontend") ||
                                    lowerTitle.contains("front end") ||
                                    lowerTitle.contains("backend") ||
                                    lowerTitle.contains("back end") ||
                                    lowerTitle.contains("full stack") ||
                                    lowerTitle.contains("fullstack") ||

                                    // UI/UX & Designer
                                    lowerTitle.contains("ui") ||
                                    lowerTitle.contains("ux") ||
                                    lowerTitle.contains("ui/ux") ||
                                    lowerTitle.contains("designer") ||

                                    // Infra / DevOps / Cloud
                                    lowerTitle.contains("devops") ||
                                    lowerTitle.contains("sre") ||
                                    lowerTitle.contains("site reliability") ||
                                    lowerTitle.contains("cloud") ||
                                    lowerTitle.contains("infrastructure") ||

                                    // General technical
                                    lowerTitle.contains("technical") ||

                                    // Languages
                                    lowerTitle.contains("java") ||
                                    lowerTitle.contains("python") ||
                                    lowerTitle.contains("javascript") ||
                                    lowerTitle.contains("typescript") ||
                                    lowerTitle.contains("node") ||
                                    lowerTitle.contains(".net") ||
                                    lowerTitle.contains("php") ||
                                    lowerTitle.contains("golang") ||
                                    lowerTitle.contains("ruby");
                    lowerTitle.contains("react");
                    lowerTitle.contains("vue");
                    lowerTitle.contains("spring");

                    if (!isTechJob)
                        continue;

                    Job job = new Job();
                    job.jobTitle = title;
                    job.companyName = jobObj.has("companyName") ? jobObj.get("companyName").getAsString() : "";
                    job.location = "Remote";
                    job.url = jobObj.has("applicationLink") ? jobObj.get("applicationLink").getAsString() : "";
                    job.source = "Himalayas";

                    allJobs.add(job);
                    addedFromPage++;
                }

                System.out.println("Himalayas page " + page + " (offset=" + offset + "): Found " + totalInPage
                        + " jobs, added " + addedFromPage + " tech jobs (total: " + allJobs.size() + ")");

                // Stop if less than limit (last page)
                if (jobsArray.size() < limit) {
                    System.out.println("Himalayas: Reached last page");
                    break;
                }

            } catch (Exception e) {
                System.err.println("Himalayas page " + page + " error: " + e.getMessage());
                break;
            }
        }

        System.out.println("Himalayas API Total: Got " + allJobs.size() + " jobs");
        return allJobs;
    }

    private List<Job> removeDuplicates(List<Job> jobs) {
        Set<String> seenUrls = new HashSet<>();
        List<Job> uniqueJobs = new ArrayList<>();

        for (Job job : jobs) {
            if (job.url != null && !job.url.isEmpty() && !seenUrls.contains(job.url)) {
                seenUrls.add(job.url);
                uniqueJobs.add(job);
            }
        }

        int duplicates = jobs.size() - uniqueJobs.size();
        if (duplicates > 0) {
            System.out.println("Removed " + duplicates + " duplicate jobs");
        }

        return uniqueJobs;
    }

    @Override
    public void insertToDatabase(List<Job> jobs) throws Exception {
        for (Job job : jobs) {
            try {
                Database.insertJob(job);
            } catch (Exception e) {
                // Silent fail for duplicates
            }
        }
    }
}