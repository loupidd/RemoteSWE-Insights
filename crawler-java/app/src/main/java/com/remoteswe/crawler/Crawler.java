package com.remoteswe.crawler;

import java.util.List;

public interface Crawler {
    /**
     * Crawl data from the target website.
     * 
     * @return List of Job objects
     * @throws Exception
     */
    List<Job> crawl() throws Exception;

    /**
     * Insert crawled data into PostgreSQL database.
     * 
     * @param jobs List of jobs
     * @throws Exception
     */
    void insertToDatabase(List<Job> jobs) throws Exception;
}
