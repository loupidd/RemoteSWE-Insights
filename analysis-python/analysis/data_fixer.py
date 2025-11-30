import psycopg2
import random

DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "dbname": "remoteswe_db",
    "user": "remoteswe_user",
    "password": "08080701"
}

def fix_missing_data():
    """
    This script adds realistic test data to existing job records
    that are missing experience_level and salary information.
    
    WARNING: This modifies your database! Only use for testing.
    """
    
    conn = psycopg2.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    # Experience levels to randomly assign
    experience_levels = [
        'Entry Level',
        'Mid Level', 
        'Senior',
        'Lead',
        'Staff'
    ]
    
    # Get all jobs missing experience_level
    print("Checking for jobs missing experience_level...")
    cursor.execute("""
        SELECT id, job_title 
        FROM jobs 
        WHERE experience_level IS NULL OR experience_level = ''
    """)
    jobs_no_exp = cursor.fetchall()
    print(f"Found {len(jobs_no_exp)} jobs without experience level")
    
    # Update experience levels based on job title keywords
    for job_id, job_title in jobs_no_exp:
        title_lower = job_title.lower() if job_title else ''
        
        # Smart assignment based on title
        if any(word in title_lower for word in ['senior', 'sr.', 'lead', 'principal', 'staff', 'head', 'director', 'vp']):
            exp_level = random.choice(['Senior', 'Lead', 'Staff'])
        elif any(word in title_lower for word in ['junior', 'jr.', 'entry', 'graduate', 'intern', 'associate']):
            exp_level = 'Entry Level'
        else:
            exp_level = random.choice(['Mid Level', 'Senior'])
        
        cursor.execute("""
            UPDATE jobs 
            SET experience_level = %s 
            WHERE id = %s
        """, (exp_level, job_id))
    
    print(f"✓ Updated {len(jobs_no_exp)} experience levels")
    
    # Get jobs missing salary
    print("\nChecking for jobs missing salary...")
    cursor.execute("""
        SELECT id, experience_level 
        FROM jobs 
        WHERE salary_min IS NULL AND salary_max IS NULL
    """)
    jobs_no_salary = cursor.fetchall()
    print(f"Found {len(jobs_no_salary)} jobs without salary")
    
    # Add realistic salaries based on experience (annual USD)
    salary_ranges = {
        'Entry Level': (60000, 95000),
        'Mid Level': (90000, 140000),
        'Senior': (130000, 190000),
        'Lead': (160000, 230000),
        'Staff': (180000, 280000)
    }
    
    for job_id, exp_level in jobs_no_salary:
        if exp_level in salary_ranges:
            base_min, base_max = salary_ranges[exp_level]
        else:
            base_min, base_max = (80000, 150000)
        
        # Generate salary range
        # Add variance: ±10% to base_min
        variance = int(base_min * 0.1)
        salary_min = random.randint(base_min - variance, base_min + variance)
        
        # Ensure max is always higher than min with good spread
        spread = random.randint(30000, 50000)
        salary_max = salary_min + spread
        
        # Cap at base_max
        salary_max = min(salary_max, base_max)
        
        cursor.execute("""
            UPDATE jobs 
            SET salary_min = %s, salary_max = %s 
            WHERE id = %s
        """, (salary_min, salary_max, job_id))
    
    print(f"✓ Updated {len(jobs_no_salary)} salary ranges")
    
    conn.commit()
    
    # Show summary statistics
    print("\n" + "="*60)
    print("SUMMARY AFTER FIX:")
    print("="*60)
    
    cursor.execute("""
        SELECT experience_level, COUNT(*) as count,
               AVG(salary_min) as avg_min, AVG(salary_max) as avg_max
        FROM jobs
        WHERE experience_level IS NOT NULL AND experience_level != ''
        GROUP BY experience_level
        ORDER BY avg_min
    """)
    
    summary = cursor.fetchall()
    print("\nExperience Level Distribution & Salaries:")
    for row in summary:
        exp, count, avg_min, avg_max = row
        print(f"  {exp:15s}: {count:3d} jobs | Avg Salary: ${avg_min:,.0f} - ${avg_max:,.0f}")
    
    conn.close()
    
    print("\n" + "="*60)
    print("✓ Database fixed! Run your analysis scripts now:")
    print("  python analysis/salary_analysis.py")
    print("  python analysis/seniority_distribution.py")
    print("  python analysis/skills_trend.py")
    print("="*60)

if __name__ == "__main__":
    print("="*60)
    print("DATABASE FIXER - Add Test Data")
    print("="*60)
    print("\nThis script will:")
    print("  1. Assign experience levels based on job titles")
    print("  2. Generate realistic salary ranges")
    print("\n⚠️  WARNING: This will modify your database!")
    print("   (Only affects records with missing data)")
    print("="*60)
    
    response = input("\nContinue? (yes/no): ")
    if response.lower() == 'yes':
        try:
            fix_missing_data()
        except Exception as e:
            print(f"\n✗ Error: {e}")
            import traceback
            traceback.print_exc()
    else:
        print("Cancelled.")