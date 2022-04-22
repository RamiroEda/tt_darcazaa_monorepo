import { PrismaClient } from '@prisma/client';
import { Injectable } from '@tsed/di';

@Injectable()
export class HistoryService {
    prisma = new PrismaClient();

    getByRoutineHash(id: string) {
        return this.prisma.history.findMany({
            where: {
                routine_hash: id,
            },
        });
    }
}
