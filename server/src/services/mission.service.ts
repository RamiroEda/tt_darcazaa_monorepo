import { DatabaseProvider } from './../providers/db.provider';
import { Routine, RoutineModel } from '@/models/routine';
import { WaypointModel } from '@/models/waypoint';
import { getDayOfWeek, numberToDays } from '@/utils';
import { Inject, Injectable } from '@tsed/di';

export const ROUTINE_TABLE = 'routines';
export const WAYPOINT_TABLE = 'waypoints';
export const ROUTINE_HISTORY_TABLE = 'history';

@Injectable()
export class MissionService {
    @Inject()
    databaseProvider!: DatabaseProvider;

    async getAll(): Promise<Routine[]> {
        const date = new Date();
        const currentHour = date.getHours() + date.getMinutes() / 60.0;

        const routines: RoutineModel[] | undefined =
            await this.databaseProvider.database.all(
                `SELECT * FROM ${ROUTINE_TABLE} WHERE start < ? AND (repeat & (1 << ?)) != 0 AND executedAt != ?`,
                currentHour,
                getDayOfWeek(),
                date.getDate(),
            );

        if (!routines) return [];

        return Promise.all(
            routines.map(async (routine) => {
                const waypoints: WaypointModel[] | undefined =
                    await this.databaseProvider.database.all(
                        `SELECT * FROM ${WAYPOINT_TABLE} WHERE routine_id = ?`,
                        routine.id,
                    );

                return {
                    ...routine,
                    repeat: numberToDays(routine.repeat),
                    waypoints: waypoints ?? [],
                };
            }),
        );
    }

    async getByHash(hash: string): Promise<Routine | undefined> {
        const routine: RoutineModel | undefined =
            await this.databaseProvider.database.get(
                `SELECT * FROM ${ROUTINE_TABLE} WHERE hash = ?`,
                hash,
            );

        if (!routine) return undefined;

        const waypoints: WaypointModel[] | undefined =
            await this.databaseProvider.database.all(
                `SELECT * FROM ${WAYPOINT_TABLE} WHERE routine_id = ?`,
                routine.id,
            );

        return {
            ...routine,
            repeat: numberToDays(routine.repeat),
            waypoints: waypoints ?? [],
        };
    }

    async getHashes(): Promise<string[]> {
        const hashes: Array<{ hash: string }> | undefined =
            await this.databaseProvider.database.all(
                `SELECT hash FROM ${ROUTINE_TABLE}`,
            );

        if (!hashes) return [];

        return hashes.map((val) => val.hash);
    }

    async markCompleted(mission: Routine) {
        await this.databaseProvider.database.run(
            `UPDATE ${ROUTINE_TABLE} SET executedAt = ? WHERE id = ?`,
            new Date().getDate(),
            mission.id,
        );
    }

    async add(data: Partial<RoutineModel>) {
        await this.databaseProvider.database.run(
            `INSERT INTO ${ROUTINE_TABLE} (start, repeat, title) VALUES (?, ?, ?)`,
            data.start,
            data.repeat,
            data.title,
        );
    }

    async addAll(data: {
        routines: Array<Partial<RoutineModel>>;
        waypoints: Array<Partial<WaypointModel>>;
    }) {
        for (const routine of data.routines) {
            await this.databaseProvider.database.run(
                `INSERT INTO ${ROUTINE_TABLE} (id, start, repeat, title, hash) VALUES (?, ?, ?, ?, ?)`,
                routine.id,
                routine.start,
                routine.repeat,
                routine.title,
                routine.hash,
            );
        }
        for (const wp of data.waypoints) {
            await this.databaseProvider.database.run(
                `INSERT INTO ${WAYPOINT_TABLE} (id, \`index\`, latitude, longitude, routine_id) VALUES (?, ?, ?, ?, ?)`,
                wp.id,
                wp.index,
                wp.latitude,
                wp.longitude,
                wp.routine_id,
            );
        }
    }

    async deleteAll() {
        await this.databaseProvider.database.run(
            `DELETE FROM ${ROUTINE_TABLE}`,
        );
        await this.databaseProvider.database.run(
            `DELETE FROM ${WAYPOINT_TABLE}`,
        );
        await this.databaseProvider.database.run(
            `DELETE FROM sqlite_sequence WHERE name = ? OR name = ?`,
            ROUTINE_TABLE,
            WAYPOINT_TABLE,
        );
    }

    async delete(id: number): Promise<boolean> {
        try {
            await this.databaseProvider.database.run(
                `DELETE FROM ${ROUTINE_TABLE} WHERE id = ?`,
                id,
            );
            await this.databaseProvider.database.run(
                `DELETE FROM ${WAYPOINT_TABLE} WHERE routine_id = ?`,
                id,
            );
        } catch (e) {
            return false;
        }

        return true;
    }
}
