import { Database } from 'sqlite3';
import { open } from 'sqlite';
import { ROUTINE_TABLE, WAYPOINT_TABLE } from './services/mission.service';

export async function connectToDb() {
    console.log('🧩 Connecting to database');

    const db = await open({
        filename: '/tmp/database.db',
        driver: Database,
    });

    await db.run(`
        CREATE TABLE IF NOT EXISTS '${ROUTINE_TABLE}' (
            'id' INTEGER PRIMARY KEY AUTOINCREMENT,
            'hash' TEXT NOT NULL,
            'start' REAL NOT NULL,
            'repeat' INTEGER  NOT NULL,
            'title'  TEXT NOT NULL,
            'executedAt' INTEGER DEFAULT -1
        );
    `);

    await db.run(`
        CREATE TABLE IF NOT EXISTS '${WAYPOINT_TABLE}' (
            'id' INTEGER PRIMARY KEY AUTOINCREMENT,
            'routine_id' INTEGER NOT NULL,
            'index' INTEGER  NOT NULL,
            'latitude'  REAL NOT NULL,
            'longitude'  REAL NOT NULL
        );
    `);

    console.log('✅ Database connected');

    return db;
}
