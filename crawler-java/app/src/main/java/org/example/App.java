package org.example;

import com.remoteswe.remoteok.RemoteOKCrawler;

public class App {
    public static void main(String[] args) {
        try {
            RemoteOKCrawler crawler = new RemoteOKCrawler();
            var jobs = crawler.crawl();
            crawler.insertToDatabase(jobs);
            System.out.println("RemoteOK jobs crawled: " + jobs.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Dummy test method
    public String getGreeting() {
        return "Hello RemoteSWE-Insights!";
    }
}
