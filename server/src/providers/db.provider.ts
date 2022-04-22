import { Database, open } from 'sqlite';
import {
    ROUTINE_TABLE,
    WAYPOINT_TABLE,
    ROUTINE_HISTORY_TABLE,
} from '../services/mission.service';
import { Injectable } from '@tsed/di';
import os = require('os');
import path = require('path');

@Injectable()
export class DatabaseProvider {
    private db?: Database;

    get database(): Database {
        if (this.db) {
            return this.db;
        }

        throw new Error('❌ Database not initialized');
    }

    async connect() {
        console.log('🧩 Connecting to database');

        this.db = await open({
            filename: path.join(os.tmpdir(), 'database.db'),
            driver: Database,
        });

        await this.db.run(`
            CREATE TABLE IF NOT EXISTS '${ROUTINE_TABLE}' (
                'id'            INTEGER PRIMARY KEY AUTOINCREMENT,
                'hash'          TEXT NOT NULL,
                'start'         REAL NOT NULL,
                'repeat'        INTEGER  NOT NULL,
                'title'         TEXT NOT NULL,
                'executedAt'    INTEGER DEFAULT -1
            );
        `);

        await this.db.run(`
            CREATE TABLE IF NOT EXISTS '${WAYPOINT_TABLE}' (
                'id'            INTEGER PRIMARY KEY AUTOINCREMENT,
                'routine_id'    INTEGER NOT NULL,
                'index'         INTEGER  NOT NULL,
                'latitude'      REAL NOT NULL,
                'longitude'     REAL NOT NULL
            );
        `);

        await this.db.run(`
            CREATE TABLE IF NOT EXISTS '${ROUTINE_HISTORY_TABLE}' (
                'id'            INTEGER PRIMARY KEY AUTOINCREMENT,
                'routine_id'    INTEGER NOT NULL,
                'executedAt'    INTEGER  NOT NULL,
                'status'        TEXT NOT NULL
            );
        `);

        console.log('✅ Database connected');
    }
}
