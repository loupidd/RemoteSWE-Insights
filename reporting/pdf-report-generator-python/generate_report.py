import os
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Image
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet

# Paths
BASE_PATH = os.path.dirname(os.path.abspath(__file__))
CHART_PATH = os.path.join(BASE_PATH, "../analysis/charts")
EXPORT_PATH = os.path.join(BASE_PATH, "../analysis/exports")

# Create PDF
pdf_path = os.path.join(BASE_PATH, "RemoteSWE_Report.pdf")
doc = SimpleDocTemplate(pdf_path, pagesize=A4)
styles = getSampleStyleSheet()
story = []

# Title
story.append(Paragraph("RemoteSWE Insights Report", styles["Title"]))
story.append(Spacer(1, 20))

# Salary Analysis
story.append(Paragraph("1. Average Salary by Experience Level", styles["Heading2"]))
salary_chart = os.path.join(CHART_PATH, "avg_salary_by_experience.png")
if os.path.exists(salary_chart):
    story.append(Image(salary_chart, width=400, height=250))
story.append(Spacer(1, 20))

# Seniority Distribution
story.append(Paragraph("2. Seniority Distribution", styles["Heading2"]))
seniority_chart = os.path.join(CHART_PATH, "seniority_distribution.png")
if os.path.exists(seniority_chart):
    story.append(Image(seniority_chart, width=400, height=250))
story.append(Spacer(1, 20))

# Skills Trend
story.append(Paragraph("3. Top 15 Most In-Demand Skills", styles["Heading2"]))
skills_chart = os.path.join(CHART_PATH, "top_skills.png")
if os.path.exists(skills_chart):
    story.append(Image(skills_chart, width=400, height=250))
story.append(Spacer(1, 20))

# Footer / notes
story.append(Paragraph("Report generated using RemoteSWE data", styles["Normal"]))

# Build PDF
doc.build(story)
print(f"PDF report generated successfully at {pdf_path}")
