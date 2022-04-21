import { connectToDb } from '@/db';
import { Routine, RoutineModel } from '@/models/routine';
import { WaypointModel } from '@/models/waypoint';
import { getDayOfWeek, numberToDays } from '@/utils';

export const ROUTINE_TABLE = 'routines';
export const WAYPOINT_TABLE = 'waypoints';

class MissionService {
    db = connectToDb();

    async getAll(): Promise<Routine[]> {
        const date = new Date();
        const currentHour = date.getHours() + date.getMinutes() / 60.0;

        const routines: RoutineModel[] | undefined = await (
            await this.db
        ).all(
            `SELECT * FROM ${ROUTINE_TABLE} WHERE start < ? AND (repeat & (1 << ?)) != 0 AND executedAt != ?`,
            currentHour,
            getDayOfWeek(),
            date.getDate(),
        );

        if (!routines) return [];

        return Promise.all(
            routines.map(async (routine) => {
                const waypoints: WaypointModel[] | undefined = await (
                    await this.db
                ).all(
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
        const routine: RoutineModel | undefined = await (
            await this.db
        ).get(`SELECT * FROM ${ROUTINE_TABLE} WHERE hash = ?`, hash);

        if (!routine) return undefined;

        const waypoints: WaypointModel[] | undefined = await (
            await this.db
        ).all(
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
        const hashes: Array<{ hash: string }> | undefined = await (
            await this.db
        ).all(`SELECT hash FROM ${ROUTINE_TABLE}`);

        if (!hashes) return [];

        return hashes.map((val) => val.hash);
    }

    async markCompleted(mission: Routine) {
        await (
            await this.db
        ).run(
            `UPDATE ${ROUTINE_TABLE} SET executedAt = ? WHERE id = ?`,
            new Date().getDate(),
            mission.id,
        );
    }

    async add(data: Partial<RoutineModel>) {
        await (
            await this.db
        ).run(
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
            await (
                await this.db
            ).run(
                `INSERT INTO ${ROUTINE_TABLE} (id, start, repeat, title, hash) VALUES (?, ?, ?, ?, ?)`,
                routine.id,
                routine.start,
                routine.repeat,
                routine.title,
                routine.hash,
            );
        }
        for (const wp of data.waypoints) {
            await (
                await this.db
            ).run(
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
        await (await this.db).run(`DELETE FROM ${ROUTINE_TABLE}`);
        await (await this.db).run(`DELETE FROM ${WAYPOINT_TABLE}`);
        await (
            await this.db
        ).run(
            `DELETE FROM sqlite_sequence WHERE name = ? OR name = ?`,
            ROUTINE_TABLE,
            WAYPOINT_TABLE,
        );
    }

    async delete(id: number): Promise<boolean> {
        try {
            await (
                await this.db
            ).run(`DELETE FROM ${ROUTINE_TABLE} WHERE id = ?`, id);
            await (
                await this.db
            ).run(`DELETE FROM ${WAYPOINT_TABLE} WHERE routine_id = ?`, id);
        } catch (e) {
            return false;
        }

        return true;
    }
}

export default new MissionService();
