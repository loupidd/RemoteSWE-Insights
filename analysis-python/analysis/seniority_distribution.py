import os
import pandas as pd
import matplotlib.pyplot as plt
import psycopg2

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
    """Fetch jobs with proper column handling"""
    conn = psycopg2.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    # Filter out NULL, empty strings, and the literal string 'experience_level'
    query = """
        SELECT experience_level 
        FROM jobs 
        WHERE experience_level IS NOT NULL 
        AND experience_level != ''
        AND TRIM(experience_level) != ''
        AND experience_level != 'experience_level'
    """
    
    cursor.execute(query)
    rows = cursor.fetchall()
    
    # Create DataFrame with proper column name
    df = pd.DataFrame(rows, columns=['experience_level'])
    
    conn.close()
    return df

def plot_seniority(df):
    if df.empty:
        print("No experience level data found!")
        return
    
    # Clean the data - trim whitespace and normalize
    df['experience_level'] = df['experience_level'].str.strip()
    
    # Get value counts
    counts = df['experience_level'].value_counts()
    
    print(f"\nExperience Level Distribution:")
    print(counts.to_string())
    print(f"\nTotal jobs: {counts.sum()}")
    
    # Save to CSV
    counts.to_csv(os.path.join(EXPORT_PATH, "seniority_distribution.csv"), header=['count'])
    
    # Plot
    plt.figure(figsize=(10, 8))
    
    if len(counts) == 1:
        # If only one category, show a warning
        print("\nWARNING: Only one experience level found in data!")
        colors = ['#4CAF50']
    else:
        colors = ['#4CAF50', '#2196F3', '#FF9800', '#E91E63', '#9C27B0', '#00BCD4']
    
    # Create pie chart
    wedges, texts, autotexts = plt.pie(counts, 
                                        labels=counts.index,
                                        autopct='%1.1f%%',
                                        startangle=90,
                                        colors=colors[:len(counts)],
                                        textprops={'fontsize': 11},
                                        pctdistance=0.85)
    
    # Make percentage text bold and white
    for autotext in autotexts:
        autotext.set_color('white')
        autotext.set_fontweight('bold')
        autotext.set_fontsize(13)
    
    # Make labels bold
    for text in texts:
        text.set_fontweight('bold')
        text.set_fontsize(12)
    
    plt.title("Distribution of Experience Level", fontsize=16, fontweight='bold', pad=20)
    plt.tight_layout()
    plt.savefig(os.path.join(CHART_PATH, "seniority_distribution.png"), dpi=300, bbox_inches='tight')
    print(f"\n✓ Chart saved to: {CHART_PATH}/seniority_distribution.png")
    plt.show()

if __name__ == "__main__":
    print("="*60)
    print("SENIORITY DISTRIBUTION ANALYSIS")
    print("="*60)
    
    df = fetch_jobs()
    
    if not df.empty:
        print(f"\n✓ Fetched {len(df)} jobs with experience level")
        print(f"  Unique levels: {sorted(df['experience_level'].unique())}")
        plot_seniority(df)
    else:
        print("\n✗ No data found!")
        print("  Check if experience_level column has valid data")
        print("  Run: python analysis/db_inspector.py")