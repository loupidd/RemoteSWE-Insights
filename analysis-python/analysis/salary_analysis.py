import pandas as pd
import matplotlib.pyplot as plt
import psycopg2
from psycopg2.extras import RealDictCursor

# PostgreSQL connection configuration
DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "dbname": "remote_jobs",
    "user": "your_user",
    "password": "your_password"
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
    # Fill missing salary_max with salary_min
    df['salary_max'] = df['salary_max'].fillna(df['salary_min'])
    df['avg_salary'] = (df['salary_min'] + df['salary_max']) / 2

    plt.figure(figsize=(10,6))
    df.groupby('experience_level')['avg_salary'].mean().sort_values().plot(kind='bar', color='skyblue')
    plt.title("Average Salary by Experience Level")
    plt.ylabel("Average Salary (USD)")
    plt.xlabel("Experience Level")
    plt.tight_layout()
    plt.savefig("../charts/avg_salary_by_experience.png")
    plt.show()

if __name__ == "__main__":
    jobs_df = fetch_jobs()
    analyze_salary(jobs_df)
