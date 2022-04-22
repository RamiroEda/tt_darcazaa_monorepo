import { DatabaseProvider } from '@/providers/db.provider';
import { Inject, Injectable } from '@tsed/di';
import { ROUTINE_HISTORY_TABLE } from './mission.service';

@Injectable()
export class HistoryService {
    @Inject()
    databaseProvider!: DatabaseProvider;

    getByRoutine(id: string) {
        return this.databaseProvider.database.all(
            `SELECT * FROM ${ROUTINE_HISTORY_TABLE} WHERE routine_id = ?`,
            id,
        );
    }
}
