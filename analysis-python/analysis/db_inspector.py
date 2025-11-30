import psycopg2
from psycopg2.extras import RealDictCursor
import pandas as pd

DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "dbname": "remoteswe_db",
    "user": "remoteswe_user",
    "password": "08080701"
}

def inspect_database():
    conn = psycopg2.connect(**DB_CONFIG, cursor_factory=RealDictCursor)
    cursor = conn.cursor()
    
    print("=" * 80)
    print("DATABASE INSPECTION REPORT")
    print("=" * 80)
    
    # 1. Total jobs count
    cursor.execute("SELECT COUNT(*) as total FROM jobs")
    total = cursor.fetchone()['total']
    print(f"\n1. TOTAL JOBS: {total}")
    
    # 2. Check experience_level
    print("\n2. EXPERIENCE LEVEL DATA:")
    cursor.execute("""
        SELECT 
            COUNT(*) as total,
            COUNT(experience_level) as non_null,
            COUNT(CASE WHEN experience_level = '' THEN 1 END) as empty_string,
            COUNT(CASE WHEN experience_level IS NOT NULL AND experience_level != '' THEN 1 END) as valid
        FROM jobs
    """)
    exp_stats = cursor.fetchone()
    print(f"   Total rows: {exp_stats['total']}")
    print(f"   Non-NULL: {exp_stats['non_null']}")
    print(f"   Empty strings: {exp_stats['empty_string']}")
    print(f"   Valid values: {exp_stats['valid']}")
    
    # Show actual values
    cursor.execute("""
        SELECT experience_level, COUNT(*) as count 
        FROM jobs 
        WHERE experience_level IS NOT NULL 
        GROUP BY experience_level
    """)
    exp_values = cursor.fetchall()
    if exp_values:
        print("\n   Actual values found:")
        for row in exp_values:
            print(f"     '{row['experience_level']}': {row['count']} jobs")
    else:
        print("   ⚠️  NO VALUES FOUND - All are NULL!")
    
    # 3. Check salary data
    print("\n3. SALARY DATA:")
    cursor.execute("""
        SELECT 
            COUNT(*) as total,
            COUNT(salary_min) as has_min,
            COUNT(salary_max) as has_max,
            MIN(salary_min) as min_salary,
            MAX(salary_max) as max_salary,
            AVG((salary_min + salary_max)/2) as avg_salary
        FROM jobs
    """)
    salary_stats = cursor.fetchone()
    print(f"   Total rows: {salary_stats['total']}")
    print(f"   Has salary_min: {salary_stats['has_min']}")
    print(f"   Has salary_max: {salary_stats['has_max']}")
    if salary_stats['min_salary']:
        print(f"   Salary range: ${salary_stats['min_salary']:,.0f} - ${salary_stats['max_salary']:,.0f}")
        print(f"   Average: ${salary_stats['avg_salary']:,.0f}")
    else:
        print("   ⚠️  NO SALARY DATA FOUND!")
    
    # 4. Check tags
    print("\n4. TAGS DATA:")
    cursor.execute("""
        SELECT 
            COUNT(*) as total,
            COUNT(tags) as has_tags,
            COUNT(CASE WHEN array_length(tags, 1) > 0 THEN 1 END) as non_empty_tags
        FROM jobs
    """)
    tags_stats = cursor.fetchone()
    print(f"   Total rows: {tags_stats['total']}")
    print(f"   Has tags (not NULL): {tags_stats['has_tags']}")
    print(f"   Has non-empty tags: {tags_stats['non_empty_tags']}")
    
    # 5. Sample of actual data
    print("\n5. SAMPLE DATA (first 5 jobs):")
    cursor.execute("""
        SELECT job_title, company_name, experience_level, salary_min, salary_max, 
               tags, source, posted_date
        FROM jobs 
        LIMIT 5
    """)
    samples = cursor.fetchall()
    for i, job in enumerate(samples, 1):
        print(f"\n   Job #{i}:")
        print(f"     Title: {job['job_title']}")
        print(f"     Company: {job['company_name']}")
        print(f"     Experience: '{job['experience_level']}'")
        print(f"     Salary: {job['salary_min']} - {job['salary_max']}")
        print(f"     Tags: {job['tags']}")
        print(f"     Source: {job['source']}")
        print(f"     Posted: {job['posted_date']}")
    
    # 6. Check sources
    print("\n6. DATA BY SOURCE:")
    cursor.execute("""
        SELECT source, COUNT(*) as count 
        FROM jobs 
        GROUP BY source 
        ORDER BY count DESC
    """)
    sources = cursor.fetchall()
    for row in sources:
        print(f"   {row['source']}: {row['count']} jobs")
    
    # 7. Check all column data availability
    print("\n7. COLUMN DATA AVAILABILITY:")
    cursor.execute("""
        SELECT 
            COUNT(job_title) as has_title,
            COUNT(company_name) as has_company,
            COUNT(location) as has_location,
            COUNT(job_type) as has_job_type,
            COUNT(experience_level) as has_exp_level,
            COUNT(employment_type_detail) as has_emp_type,
            COUNT(industry) as has_industry,
            COUNT(experience_required) as has_exp_req,
            COUNT(job_description_raw) as has_description
        FROM jobs
    """)
    col_stats = cursor.fetchone()
    print(f"   job_title: {col_stats['has_title']}/{total}")
    print(f"   company_name: {col_stats['has_company']}/{total}")
    print(f"   location: {col_stats['has_location']}/{total}")
    print(f"   job_type: {col_stats['has_job_type']}/{total}")
    print(f"   experience_level: {col_stats['has_exp_level']}/{total} ⚠️")
    print(f"   employment_type_detail: {col_stats['has_emp_type']}/{total}")
    print(f"   industry: {col_stats['has_industry']}/{total}")
    print(f"   experience_required: {col_stats['has_exp_req']}/{total}")
    print(f"   job_description_raw: {col_stats['has_description']}/{total}")
    
    conn.close()
    print("\n" + "=" * 80)

if __name__ == "__main__":
    try:
        inspect_database()
    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()