package com.remoteswe.crawler;

import com.remoteswe.arbeitnow.ArbeitNowCrawler;
import com.remoteswe.jsonapi.JSONAPICrawler;
import com.remoteswe.remoteok.RemoteOKCrawler;
import com.remoteswe.rss.RSSCrawler;
import com.remoteswe.weworkremotely.WWRRSSCrawler;

import java.util.List;

public class App {
    public static void main(String[] args) {
        System.out.println("Starting RemoteSWE Crawlers...");
        int totalJobs = 0;

        // RemoteOK (JSON API)
        try {
            RemoteOKCrawler remoteOK = new RemoteOKCrawler();
            List<Job> remoteOKJobs = remoteOK.crawl();
            remoteOK.insertToDatabase(remoteOKJobs);
            totalJobs += remoteOKJobs.size();
            System.out.println("✓ RemoteOK: " + remoteOKJobs.size() + " jobs");
        } catch (Exception e) {
            System.err.println("✗ RemoteOK failed: " + e.getMessage());
        }

        // WeWorkRemotely RSS
        try {
            WWRRSSCrawler wwrRSS = new WWRRSSCrawler();
            List<Job> wwrJobs = wwrRSS.crawl();
            wwrRSS.insertToDatabase(wwrJobs);
            totalJobs += wwrJobs.size();
            System.out.println("✓ WWR RSS: " + wwrJobs.size() + " jobs");
        } catch (Exception e) {
            System.err.println("✗ WWR RSS failed: " + e.getMessage());
        }

        // Rss Feeds
        try {
            RSSCrawler rssCrawler = new RSSCrawler();
            List<Job> rssJobs = rssCrawler.crawl();
            rssCrawler.insertToDatabase(rssJobs);
            totalJobs += rssJobs.size();
            System.out.println("✓ Tech RSS Feeds: " + rssJobs.size() + " jobs");
        } catch (Exception e) {
            System.err.println("✗ RSS Feeds failed: " + e.getMessage());
        }

        // JSON APIs (Remotive, etc.)
        try {
            JSONAPICrawler jsonAPI = new JSONAPICrawler();
            List<Job> apiJobs = jsonAPI.crawl();
            jsonAPI.insertToDatabase(apiJobs);
            totalJobs += apiJobs.size();
            System.out.println("✓ JSON APIs: " + apiJobs.size() + " jobs");
        } catch (Exception e) {
            System.err.println("✗ JSON APIs failed: " + e.getMessage());
        }

        // ArbeitNow API
        try {
            ArbeitNowCrawler arbeitNow = new ArbeitNowCrawler();
            List<Job> arbeitJobs = arbeitNow.crawl();
            arbeitNow.insertToDatabase(arbeitJobs);
            totalJobs += arbeitJobs.size();
            System.out.println("✓ ArbeitNow API: " + arbeitJobs.size() + " jobs");
        } catch (Exception e) {
            System.err.println("✗ ArbeitNow failed: " + e.getMessage());
        }

        System.out.println("\n========================================");
        System.out.println("TOTAL JOBS CRAWLED: " + totalJobs);
        System.out.println("========================================");
    }
}
