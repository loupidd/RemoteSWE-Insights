import os
import pandas as pd
import matplotlib.pyplot as plt
import psycopg2
from collections import Counter

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
    """Fetch jobs with PostgreSQL array handling"""
    conn = psycopg2.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    # Fetch tags as array - PostgreSQL will return them as Python lists
    query = """
        SELECT tags 
        FROM jobs 
        WHERE tags IS NOT NULL 
        AND array_length(tags, 1) > 0
    """
    
    cursor.execute(query)
    rows = cursor.fetchall()
    
    # Extract just the tags column (first element of each tuple)
    tags_list = [row[0] for row in rows if row[0]]
    
    conn.close()
    return tags_list

def analyze_skills(tags_list):
    if not tags_list:
        print("No tags data found!")
        return
    
    print(f"✓ Found {len(tags_list)} jobs with tags")
    
    # Flatten all tags into a single list
    all_tags = []
    for tags_array in tags_list:
        if tags_array and isinstance(tags_array, list):
            for tag in tags_array:
                if tag and isinstance(tag, str) and tag.strip():
                    # Clean and normalize
                    cleaned_tag = tag.strip().lower()
                    # Skip generic or meaningless tags
                    if cleaned_tag not in ['tag', 'tags', '']:
                        all_tags.append(cleaned_tag)
    
    if not all_tags:
        print("No valid tags found after cleaning!")
        return
    
    print(f"✓ Total tags extracted: {len(all_tags)}")
    print(f"✓ Unique tags: {len(set(all_tags))}")
    
    # Count occurrences
    counter = Counter(all_tags)
    top_skills = counter.most_common(15)
    
    if not top_skills:
        print("No skills to plot!")
        return
    
    skills, counts = zip(*top_skills)
    
    print(f"\nTop 15 Skills:")
    for i, (skill, count) in enumerate(top_skills, 1):
        print(f"  {i:2d}. {skill:20s}: {count:4d} jobs")
    
    # Save CSV
    skills_df = pd.DataFrame(top_skills, columns=['skill', 'count'])
    skills_df.to_csv(os.path.join(EXPORT_PATH, "top_skills.csv"), index=False)
    
    # Plot
    plt.figure(figsize=(12, 8))
    bars = plt.barh(range(len(skills)), counts, color='salmon', edgecolor='darkred', linewidth=1.2)
    
    # Add value labels
    for i, (bar, count) in enumerate(zip(bars, counts)):
        plt.text(count + max(counts)*0.01, i, f'{count}', 
                va='center', fontsize=11, fontweight='bold')
    
    plt.yticks(range(len(skills)), skills, fontsize=11)
    plt.xlabel("Number of Job Postings", fontsize=13, fontweight='bold')
    plt.title("Top 15 Most In-Demand Skills", fontsize=16, fontweight='bold', pad=15)
    plt.gca().invert_yaxis()
    plt.grid(axis='x', alpha=0.3, linestyle='--')
    plt.tight_layout()
    plt.savefig(os.path.join(CHART_PATH, "top_skills.png"), dpi=300, bbox_inches='tight')
    print(f"\n✓ Chart saved to: {CHART_PATH}/top_skills.png")
    plt.show()

if __name__ == "__main__":
    print("="*60)
    print("SKILLS TREND ANALYSIS")
    print("="*60 + "\n")
    
    tags_list = fetch_jobs()
    
    if tags_list:
        analyze_skills(tags_list)
    else:
        print("✗ No data found!")
        print("  Check if tags column has data")
        print("  Run: python analysis/db_inspector.py")