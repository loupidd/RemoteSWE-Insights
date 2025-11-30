import os
import pandas as pd
import matplotlib.pyplot as plt
import psycopg2
from psycopg2.extras import RealDictCursor

BASE_PATH = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CHART_PATH = os.path.join(BASE_PATH, "charts")
EXPORT_PATH = os.path.join(BASE_PATH, "exports")
os.makedirs(CHART_PATH, exist_ok=True)
os.makedirs(EXPORT_PATH, exist_ok=True)

DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "dbname": "remoteswe_db",
    "user": "remoteswe_user",
    "password": "08080701"
}

def fetch_jobs():
    conn = psycopg2.connect(**DB_CONFIG, cursor_factory=RealDictCursor)
    query = "SELECT experience_level FROM jobs WHERE experience_level IS NOT NULL"
    df = pd.read_sql(query, conn)
    conn.close()
    return df

def plot_seniority(df):
    counts = df['experience_level'].value_counts()
    counts.to_csv(os.path.join(EXPORT_PATH,"seniority_distribution.csv"))

    plt.figure(figsize=(8,6))
    counts.plot(kind='pie', autopct='%1.1f%%', startangle=90, 
                colors=['#4CAF50','#2196F3','#FF9800','#E91E63'])
    plt.title("Distribution of Experience Level")
    plt.ylabel("")
    plt.tight_layout()
    plt.savefig(os.path.join(CHART_PATH,"seniority_distribution.png"))
    plt.show()

if __name__ == "__main__":
    df = fetch_jobs()
    plot_seniority(df)
