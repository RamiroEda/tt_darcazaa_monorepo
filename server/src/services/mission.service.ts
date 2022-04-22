import { PrismaClient, Routine, Waypoint } from '@prisma/client';
import { Injectable } from '@tsed/di';
import moment from 'moment';

@Injectable()
export class MissionService {
    prisma = new PrismaClient();

    async getAll(): Promise<Array<Routine & { waypoints: Waypoint[] }>> {
        const now = moment();
        const currentHour = now.hours() + now.minutes() / 60.0;

        return this.prisma.routine.findMany({
            where: {
                start: {
                    lt: currentHour,
                },
                repeat: {
                    contains: `${now.day()}`,
                },
                executedAt: {
                    not: now.dayOfYear(),
                },
            },
            include: {
                waypoints: true,
            },
        });
    }

    async getByHash(
        hash: string,
    ): Promise<(Routine & { waypoints: Waypoint[] }) | null> {
        return this.prisma.routine.findFirst({
            where: {
                hash,
            },
            include: {
                waypoints: true,
            },
        });
    }

    async getHashes(): Promise<string[]> {
        return (
            await this.prisma.routine.findMany({
                select: {
                    hash: true,
                },
            })
        ).map((it) => it.hash);
    }

    async markCompleted(mission: Routine) {
        await this.prisma.routine.update({
            where: {
                id: mission.id,
            },
            data: {
                executedAt: moment().dayOfYear(),
            },
        });
    }

    async add(data: {
        start: number;
        repeat: string;
        title: string;
        hash: string;
    }): Promise<Routine> {
        return await this.prisma.routine.create({
            data,
        });
    }

    async addAll(data: {
        routines: Array<{
            start: number;
            repeat: string;
            title: string;
            hash: string;
        }>;
        waypoints: Array<{
            index: number;
            latitude: number;
            longitude: number;
            routine_hash: string;
        }>;
    }) {
        this.prisma.$transaction([
            ...data.routines.map((data) =>
                this.prisma.routine.create({ data }),
            ),
            ...data.waypoints.map((data) =>
                this.prisma.waypoint.create({ data }),
            ),
        ]);
    }

    async deleteAll() {
        await this.prisma.routine.deleteMany();
    }

    async delete(id: number) {
        await this.prisma.routine.delete({
            where: {
                id,
            },
        });
    }
}
