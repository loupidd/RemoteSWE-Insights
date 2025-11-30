import psycopg2

# Connect to PostgreSQL
conn = psycopg2.connect(
    host="localhost",
    port=5432,
    database="remoteswe_db",
    user="remoteswe_user",
    password="08080701"
)

cur = conn.cursor()
cur.execute("SELECT version();")
print(cur.fetchone())
conn.close()
