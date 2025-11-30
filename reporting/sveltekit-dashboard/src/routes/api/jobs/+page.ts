export async function load({ fetch }) {
  const res = await fetch("/api/jobs");
  const jobs = await res.json();
  return { jobs };
}
