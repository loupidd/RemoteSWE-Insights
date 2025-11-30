import pkg from 'pg';
const { Pool } = pkg;

export const db = new Pool({
  user: 'remoteswe_user',
  host: 'localhost',
  database: 'remoteswe_db',
  password: '08080701',
  port: 5432
});
