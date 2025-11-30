<script lang="ts">
  import { onMount } from "svelte";
  import type { Job } from "$lib/types";

  let jobs: Job[] = [];
  let loading = true;
  let error: string | null = null;

  function formatSalary(min?: number | null, max?: number | null) {
    if (min && max) return `$${min.toLocaleString()} - $${max.toLocaleString()}`;
    if (min) return `$${min.toLocaleString()}+`;
    if (max) return `Up to $${max.toLocaleString()}`;
    return null;
  }

  onMount(async () => {
    try {
      const res = await fetch("/api/jobs");
      if (!res.ok) throw new Error(`Failed to fetch jobs: ${res.status}`);
      
      const data = await res.json();
      
      console.log("Raw API response:", data);
      console.log("First job sample:", JSON.stringify(data[0], null, 2));
      
      jobs = data;
    } catch (err: any) {
      console.error("Error fetching jobs:", err);
      error = err.message;
    } finally {
      loading = false;
    }
  });
</script>

<svelte:head>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
</svelte:head>

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
      {#each jobs as job (job.url)}
        <div class="border rounded-xl p-6 shadow-md hover:shadow-xl transition bg-white flex flex-col justify-between">
          <div>
            <a 
              href={job.url} 
              target="_blank" 
              rel="noopener noreferrer"
              class="text-2xl font-bold text-blue-600 hover:underline block mb-2"
            >
              {job.jobTitle}
            </a>
            <p class="text-gray-700 mt-1 font-medium">{job.companyName}</p>

            <p class="text-gray-500 text-sm mt-2">
              <i class="fas fa-map-marker-alt"></i> {job.location || 'Remote'} 
              {#if job.jobType && job.jobType.trim()}
                | <i class="fas fa-briefcase"></i> {job.jobType}
              {/if}
            </p>

            {#if job.salaryMin || job.salaryMax}
              <p class="text-green-600 text-sm mt-2 font-semibold">
                <i class="fas fa-dollar-sign"></i> {formatSalary(job.salaryMin, job.salaryMax)}
              </p>
            {/if}

            {#if job.experienceLevel && job.experienceLevel.trim()}
              <p class="text-gray-600 text-sm mt-2">
                <i class="fas fa-chart-line"></i> {job.experienceLevel}
              </p>
            {/if}

            {#if job.employmentTypeDetail && job.employmentTypeDetail.trim()}
              <p class="text-gray-600 text-sm mt-2">
                <i class="fas fa-clock"></i> {job.employmentTypeDetail}
              </p>
            {/if}

            {#if job.tags && job.tags.length > 0}
              <div class="flex flex-wrap gap-2 mt-3">
                {#each job.tags as tag}
                  <span class="bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full">
                    {tag}
                  </span>
                {/each}
              </div>
            {:else}
              <p class="text-gray-400 text-xs mt-3 italic">No tags available</p>
            {/if}
          </div>

          <div class="mt-4 pt-4 border-t flex justify-between items-center text-sm text-gray-400">
            <span class="truncate mr-2">
              <i class="fas fa-tag"></i> {job.source}
            </span>
            <a 
              href={job.url} 
              target="_blank" 
              rel="noopener noreferrer"
              class="text-blue-500 hover:underline font-medium whitespace-nowrap"
            >
              Apply <i class="fas fa-arrow-right"></i>
            </a>
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
    min-height: 100vh;
  }
</style>