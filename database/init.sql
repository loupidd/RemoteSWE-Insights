CREATE TABLE jobs (
    id SERIAL PRIMARY KEY,
    job_title TEXT NOT NULL,
    company_name TEXT NOT NULL,
    location TEXT,
    salary_min NUMERIC,
    salary_max NUMERIC,
    job_type VARCHAR(50),
    tags TEXT[],
    url TEXT NOT NULL,
    source VARCHAR(50),
    posted_date TIMESTAMP,
    crawled_at TIMESTAMP NOT NULL DEFAULT NOW(),

    job_description_raw TEXT,
    experience_level VARCHAR(50),
    employment_type_detail TEXT,
    industry TEXT,
    experience_required TEXT
);

CREATE INDEX idx_jobs_title ON jobs(job_title);
CREATE INDEX idx_jobs_company ON jobs(company_name);
CREATE INDEX idx_jobs_tags ON jobs USING GIN (tags);
CREATE INDEX idx_jobs_source ON jobs(source);

