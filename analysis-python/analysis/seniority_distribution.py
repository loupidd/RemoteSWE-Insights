import pandas as pd
import matplotlib.pyplot as plt
import psycopg2
from psycopg2.extras import RealDictCursor

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
        SELECT experience_level
        FROM jobs
        WHERE experience_level IS NOT NULL
    """
    df = pd.read_sql(query, conn)
    conn.close()
    return df

def plot_seniority(df):
    counts = df['experience_level'].value_counts()
    plt.figure(figsize=(8,6))
    counts.plot(kind='pie', autopct='%1.1f%%', startangle=90, colors=['#4CAF50','#2196F3','#FF9800','#E91E63'])
    plt.title("Distribution of Experience Level")
    plt.ylabel("")
    plt.tight_layout()
    plt.savefig("../charts/seniority_distribution.png")
    plt.show()

if __name__ == "__main__":
    df = fetch_jobs()
    plot_seniority(df)
