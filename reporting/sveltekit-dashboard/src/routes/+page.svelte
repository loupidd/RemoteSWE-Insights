<script lang="ts">
  import { onMount } from "svelte";
  import type { Job } from "$lib/types";

  let jobs: Job[] = [];
  let loading = true;
  let error: string | null = null;

  // Helper to format salary
  function formatSalary(min?: number, max?: number) {
    if (min && max) return `$${min.toLocaleString()} - $${max.toLocaleString()}`;
    if (min) return `$${min.toLocaleString()}+`;
    return "N/A";
  }

  onMount(async () => {
    try {
      const res = await fetch("/api/jobs");
      if (!res.ok) throw new Error(`Failed to fetch jobs: ${res.status}`);
      const data: Job[] = await res.json();

      // TypeScript-safe mapping: ensures no undefined optional fields
      jobs = data.map(job => ({
        ...job,
        jobType: job.jobType || "Unknown",
        location: job.location || "Remote",
        tags: job.tags ?? [], // always an array
        salaryMin: job.salaryMin ?? undefined,
        salaryMax: job.salaryMax ?? undefined,
        experienceLevel: job.experienceLevel || "N/A",
        employmentTypeDetail: job.employmentTypeDetail || "N/A",
        jobDescriptionRaw: job.jobDescriptionRaw || "",
      }));
    } catch (err: any) {
      console.error(err);
      error = err.message;
    } finally {
      loading = false;
    }
  });
</script>

<main class="p-6 max-w-7xl mx-auto">
  <h1 class="text-4xl font-bold mb-8 text-center">Remote Job Listings</h1>

  {#if loading}
    <p class="text-gray-500 text-center">Loading jobs...</p>
  {:else if error}
    <p class="text-red-500 text-center">Error: {error}</p>
  {:else if jobs.length === 0}
    <p class="text-gray-500 text-center">No jobs found.</p>
  {:else}
    <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
      {#each jobs as job}
        <div class="border rounded-xl p-6 shadow-md hover:shadow-xl transition bg-white flex flex-col justify-between">
          <div>
            <!-- Job Title & Company -->
            <a href={job.url} target="_blank" class="text-2xl font-bold text-blue-600 hover:underline">{job.jobTitle}</a>
            <p class="text-gray-700 mt-1 font-medium">{job.companyName}</p>

            <!-- Location & Job Type -->
            <p class="text-gray-500 text-sm mt-1">{job.location} | {job.jobType}</p>

            <!-- Salary -->
            {#if job.salaryMin || job.salaryMax}
              <p class="text-gray-600 text-sm mt-1">Salary: {formatSalary(job.salaryMin, job.salaryMax)}</p>
            {/if}

            <!-- Experience & Employment Detail -->
            <p class="text-gray-600 text-sm mt-1">Experience: {job.experienceLevel}</p>
            <p class="text-gray-600 text-sm mt-1">Employment: {job.employmentTypeDetail}</p>

            <!-- Tags -->
          {#if (job.tags ?? []).length > 0}
              <div class="flex flex-wrap gap-2 mt-3">
              {#each job.tags ?? [] as tag}
                <span class="bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full">{tag}</span>
              {/each}
            </div>
          {:else}
            <p class="text-gray-400 text-xs mt-1">No tags</p>
          {/if}
          </div>

          <!-- Footer with Source & Apply -->
          <div class="mt-4 flex justify-between items-center text-sm text-gray-400">
            <span>Source: {job.source}</span>
            <a href={job.url} target="_blank" class="text-blue-500 hover:underline font-medium">Apply</a>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</main>

<style>
  main {
    font-family: system-ui, sans-serif;
    background-color: #f9fafb;
  }
</style>
