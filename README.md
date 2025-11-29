# RemoteSWE-Insights

RemoteSWE-Insights is a full-stack system designed to collect, analyze, and report remote software engineering job postings from top platforms: RemoteOK, WeWorkRemotely, and Wellfound. The system provides a unified view of job trends, salaries, and in-demand skills, with enterprise-ready reporting.

---

## System Architecture

      ┌────────────────────┐
      │  Job Websites      │
      │  (RemoteOK, WWR,   │
      │   Wellfound)       │
      └────────┬──────────┘
               │ HTTP Requests
               ▼
      ┌────────────────────┐
      │  Java Crawlers      │
      │  - RemoteOKCrawler │
      │  - WWRCrawler      │
      │  - WellfoundCrawler│
      └────────┬──────────┘
               │ Normalized Job Objects
               ▼
      ┌────────────────────┐
      │ PostgreSQL Database │
      │  jobs table schema  │
      └────────┬──────────┘
               │ Query & Analysis
               ▼
      ┌────────────────────┐
      │ Python Analysis     │
      │  - Pandas/NumPy     │
      │  - Matplotlib       │
      │  - Skills & Salary  │
      └────────┬──────────┘
               │ JSON / API
               ▼
      ┌────────────────────┐
      │ SvelteKit Dashboard │
      │  - Graphs & Charts │
      │  - PDF Reporting   │
      └────────────────────┘


## Components Overview

### 1. Java Crawlers
- **Purpose:** Crawl job listings and normalize data.  
- **Tech:** Java 21, Jsoup, JDBC, SLF4J  
- **Modules:**
  - `remoteok.RemoteOKCrawler`
  - `weworkremotely.WWRCrawler`
  - `wellfound.WellfoundCrawler`  

### 2. Database
- **Purpose:** Store normalized job data.  
- **Tech:** PostgreSQL  
- **Schema Highlights:**
  - `jobs` table with fields like `job_title`, `company_name`, `location`, `salary_min`, `salary_max`, `tags`, `experience_level`, and timestamps.  
  - Indexed on `job_title`, `company_name`, `tags`, and `source` for fast queries.  

### 3. Python Analysis
- **Purpose:** Analyze trends, salaries, skills, and seniority distributions.  
- **Tech:** Python, Pandas, NumPy, Matplotlib  
- **Example Analysis:**
  - Most used programming languages  
  - Salary heatmaps per platform  
  - Seniority distribution charts  

### 4. Reporting Dashboard
- **Purpose:** Display analysis results and generate enterprise-ready reports.  
- **Tech:** SvelteKit + TailwindCSS, Python ReportLab for PDFs  
- **Features:**
  - Interactive dashboards with charts from Python API  
  - PDF report generation for executive presentations  

---

## Data Flow
1. Crawlers scrape HTML pages from multiple remote job boards.  
2. Job data is normalized and inserted into PostgreSQL.  
3. Python scripts query the database and compute analytics.  
4. Dashboard fetches analytics via JSON API to display charts and generate PDFs.  

---

## Key Considerations
- Designed for extensibility: new job boards can be added by creating additional crawler modules.  
- Data normalization ensures consistent schema across sources.  
- Indexed database for high performance in analytics queries.  
- Separate layers for crawling, analysis, and reporting to allow independent scaling.  

---

## Goal
Provide developers, recruiters, and analysts with **actionable insights** into the remote software engineering job market, enabling data-driven decision-making.
