export interface Job {
  jobTitle: string;
  companyName: string;
  location: string;
  salaryMin?: number;
  salaryMax?: number;
  jobType?: string;
  tags?: string[];
  url: string;
  source: string;
  postedDate?: string;
  crawledAt?: string;
  jobDescriptionRaw?: string;
  experienceLevel?: string;
  employmentTypeDetail?: string;
  industry?: string;
  experienceRequired?: string;
}
