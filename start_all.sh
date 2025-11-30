#!/bin/bash

# Java crawler
(cd crawler-java && ./gradlew run)

# Python analysis
source analysis-python/venv/bin/activate
python3 analysis-python/analysis/salary_analysis.py
python3 analysis-python/analysis/seniority_distribution.py
python3 analysis-python/analysis/skills_trend.py
python3 reporting/pdf-report-generator-python/generate_report.py
deactivate

# SvelteKit dashboard
(cd reporting/sveltekit-dashboard && npm run dev)
