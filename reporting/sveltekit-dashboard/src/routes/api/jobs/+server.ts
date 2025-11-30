import { db } from "$lib/server/db";
import { json } from "@sveltejs/kit";

export async function GET() {
  const result = await db.query(
    `SELECT * FROM jobs ORDER BY crawled_at DESC LIMIT 500`
  );

  return json(result.rows);
}
