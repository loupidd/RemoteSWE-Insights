import pandas as pd
import matplotlib.pyplot as plt
import psycopg2
from psycopg2.extras import RealDictCursor
from collections import Counter

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
        SELECT tags
        FROM jobs
        WHERE tags IS NOT NULL
    """
    df = pd.read_sql(query, conn)
    conn.close()
    return df

def analyze_skills(df):
    # Flatten tags
    all_tags = [tag for sublist in df['tags'] for tag in sublist]
    counter = Counter(all_tags)
    top_skills = counter.most_common(15)

    skills, counts = zip(*top_skills)
    plt.figure(figsize=(12,6))
    plt.barh(skills, counts, color='salmon')
    plt.title("Top 15 Most In-Demand Skills")
    plt.xlabel("Number of Job Postings")
    plt.gca().invert_yaxis()
    plt.tight_layout()
    plt.savefig("../charts/top_skills.png")
    plt.show()

if __name__ == "__main__":
    df = fetch_jobs()
    analyze_skills(df)
