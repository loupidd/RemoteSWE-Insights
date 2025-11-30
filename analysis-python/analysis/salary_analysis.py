import os
import pandas as pd
import matplotlib.pyplot as plt
import psycopg2
from psycopg2.extras import RealDictCursor

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
    conn = psycopg2.connect(**DB_CONFIG, cursor_factory=RealDictCursor)
    query = """
        SELECT job_title, salary_min, salary_max, experience_level, source
        FROM jobs
        WHERE salary_min IS NOT NULL OR salary_max IS NOT NULL
    """
    df = pd.read_sql(query, conn)
    conn.close()
    return df

def analyze_salary(df):
    df['salary_max'] = df['salary_max'].fillna(df['salary_min'])
    df['avg_salary'] = (df['salary_min'] + df['salary_max']) / 2

    # Save CSV summary
    df_summary = df.groupby('experience_level')['avg_salary'].mean().reset_index()
    df_summary.to_csv(os.path.join(EXPORT_PATH, "salary_summary.csv"), index=False)

    # Plot
    plt.figure(figsize=(10,6))
    df_summary.sort_values('avg_salary', inplace=True)
    plt.bar(df_summary['experience_level'], df_summary['avg_salary'], color='skyblue')
    plt.title("Average Salary by Experience Level")
    plt.ylabel("Average Salary (USD)")
    plt.xlabel("Experience Level")
    plt.tight_layout()
    plt.savefig(os.path.join(CHART_PATH, "avg_salary_by_experience.png"))
    plt.show()

if __name__ == "__main__":
    jobs_df = fetch_jobs()
    analyze_salary(jobs_df)
