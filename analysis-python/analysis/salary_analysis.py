import os
import pandas as pd
import matplotlib.pyplot as plt
import psycopg2
from psycopg2.extras import RealDictCursor
import numpy as np

# Paths
BASE_PATH = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CHART_PATH = os.path.join(BASE_PATH, "charts")
EXPORT_PATH = os.path.join(BASE_PATH, "exports")
os.makedirs(CHART_PATH, exist_ok=True)
os.makedirs(EXPORT_PATH, exist_ok=True)

# DB config
DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "dbname": "remoteswe_db",
    "user": "remoteswe_user",
    "password": "08080701"
}

def fetch_jobs():
    """Fetch jobs with proper type handling for PostgreSQL"""
    conn = psycopg2.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    query = """
        SELECT 
            job_title, 
            CAST(salary_min AS NUMERIC) as salary_min, 
            CAST(salary_max AS NUMERIC) as salary_max, 
            experience_level, 
            source
        FROM jobs
        WHERE salary_min IS NOT NULL 
        AND salary_max IS NOT NULL
        AND experience_level IS NOT NULL 
        AND experience_level != ''
        AND experience_level != 'experience_level'
    """
    
    cursor.execute(query)
    rows = cursor.fetchall()
    
    # Manually create DataFrame with proper column names
    df = pd.DataFrame(rows, columns=['job_title', 'salary_min', 'salary_max', 'experience_level', 'source'])
    
    conn.close()
    return df

def analyze_salary(df):
    if df.empty:
        print("No data found! Check your database.")
        return
    
    # Convert to numeric (in case they're still strings)
    df['salary_min'] = pd.to_numeric(df['salary_min'], errors='coerce')
    df['salary_max'] = pd.to_numeric(df['salary_max'], errors='coerce')
    
    # Fill missing salary_max with salary_min
    df['salary_max'] = df['salary_max'].fillna(df['salary_min'])
    df['salary_min'] = df['salary_min'].fillna(df['salary_max'])
    
    # Calculate average salary
    df['avg_salary'] = (df['salary_min'] + df['salary_max']) / 2
    
    # Remove outliers (salaries that seem unrealistic)
    df = df[(df['avg_salary'] > 0) & (df['avg_salary'] < 1000000)]
    
    # Remove any NaN values
    df = df.dropna(subset=['avg_salary', 'experience_level'])
    
    # Group by experience level and calculate mean
    df_summary = df.groupby('experience_level').agg({
        'avg_salary': 'mean',
        'job_title': 'count'  # Count jobs per level
    }).reset_index()
    df_summary.columns = ['experience_level', 'avg_salary', 'job_count']
    
    # Filter out levels with too few jobs
    df_summary = df_summary[df_summary['job_count'] >= 1]
    
    if df_summary.empty:
        print("No valid salary data after filtering!")
        return
    
    # Save CSV summary
    df_summary.to_csv(os.path.join(EXPORT_PATH, "salary_summary.csv"), index=False)
    print(f"\nSalary Summary:")
    print(df_summary.to_string(index=False))
    
    # Sort for better visualization
    df_summary = df_summary.sort_values('avg_salary')
    
    # Plot
    plt.figure(figsize=(12, 7))
    bars = plt.bar(df_summary['experience_level'], df_summary['avg_salary'], 
                   color='skyblue', edgecolor='navy', linewidth=1.5)
    
    # Add value labels on bars
    for bar in bars:
        height = bar.get_height()
        plt.text(bar.get_x() + bar.get_width()/2., height,
                f'${height:,.0f}',
                ha='center', va='bottom', fontsize=10, fontweight='bold')
    
    plt.title("Average Salary by Experience Level", fontsize=16, fontweight='bold', pad=20)
    plt.ylabel("Average Salary (USD)", fontsize=13)
    plt.xlabel("Experience Level", fontsize=13)
    plt.xticks(rotation=45, ha='right')
    plt.grid(axis='y', alpha=0.3, linestyle='--')
    
    # Format y-axis as currency
    ax = plt.gca()
    ax.yaxis.set_major_formatter(plt.FuncFormatter(lambda x, p: f'${x:,.0f}'))
    
    plt.tight_layout()
    plt.savefig(os.path.join(CHART_PATH, "avg_salary_by_experience.png"), dpi=300, bbox_inches='tight')
    print(f"\n✓ Chart saved to: {CHART_PATH}/avg_salary_by_experience.png")
    plt.show()

if __name__ == "__main__":
    print("="*60)
    print("SALARY ANALYSIS")
    print("="*60)
    
    jobs_df = fetch_jobs()
    
    if not jobs_df.empty:
        print(f"\n✓ Fetched {len(jobs_df)} jobs with salary data")
        print(f"  Salary range: ${jobs_df['salary_min'].min():,.0f} - ${jobs_df['salary_max'].max():,.0f}")
        print(f"  Experience levels found: {sorted(jobs_df['experience_level'].unique())}")
        analyze_salary(jobs_df)
    else:
        print("\n✗ No data found in database!")
        print("  Run: python analysis/db_inspector.py to check your data")