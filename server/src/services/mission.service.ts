import { circularOffset } from '@/utils/array.utils';
import { PrismaClient, Routine, Waypoint } from '@prisma/client';
import { Injectable } from '@tsed/di';
import moment from 'moment';

@Injectable()
export class MissionService {
    prisma = new PrismaClient();

    async getAll(): Promise<Routine[]> {
        return this.prisma.routine.findMany();
    }

    async getPendingRoutines(): Promise<
        Array<Routine & { waypoints: Waypoint[] }>
    > {
        const now = moment();
        const currentHour = now.hours() + now.minutes() / 60.0;
        const currentDay = circularOffset({
            number: now.day(),
            offset: -1,
            min: 0,
            max: 7,
        });

        return this.prisma.routine.findMany({
            where: {
                AND: [
                    {
                        OR: [
                            {
                                repeat: '',
                            },
                            {
                                repeat: {
                                    contains: `${currentDay}`,
                                },
                            },
                        ],
                    },
                    {
                        start: {
                            lte: currentHour,
                        },
                    },
                    {
                        OR: [
                            {
                                executedAt: {
                                    not: {
                                        equals: now.dayOfYear(),
                                    },
                                },
                            },
                            {
                                executedAt: null,
                            },
                        ],
                    },
                ],
            },
            orderBy: {
                start: 'asc',
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

    async markCompleted(mission: Routine, is_cancel: boolean) {
        await this.prisma.routine.update({
            where: {
                id: mission.id,
            },
            data: {
                executedAt: moment().dayOfYear(),
            },
        });

        await this.prisma.history.create({
            data: {
                status: is_cancel ? 'Cancelado' : 'Completado',
                executedAt: moment().toDate(),
                routine_hash: mission.hash,
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
        await this.prisma.$transaction([
            ...data.routines.map((data) =>
                this.prisma.routine.create({ data }),
            ),
            ...data.waypoints.map((data) =>
                this.prisma.waypoint.create({ data }),
            ),
        ]);
    }

    async sync(data: {
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
        const localHashes = (await this.getAll()).map((it) => it.hash);
        const newHashes = data.routines.map((it) => it.hash);

        const deletedHashes = localHashes.filter(
            (it) => !newHashes.includes(it),
        );

        await Promise.all(
            deletedHashes.map((it) =>
                this.prisma.routine.delete({
                    where: {
                        hash: it,
                    },
                }),
            ),
        );

        console.log('🗑️', deletedHashes.length, 'records deleted');

        for (const routine of data.routines) {
            if (!localHashes.includes(routine.hash)) {
                console.log(`✅ Routine ${routine.title} added`);
                await this.prisma.routine.create({
                    data: routine,
                });
                await Promise.all(
                    data.waypoints
                        .filter((it) => it.routine_hash === routine.hash)
                        .map((it) => this.prisma.waypoint.create({ data: it })),
                );
            }
        }
    }

    async deleteAll() {
        const del = await this.prisma.routine.deleteMany();
        console.log('🗑️', del.count, 'records deleted');
    }

    async delete(id: number) {
        await this.prisma.routine.delete({
            where: {
                id,
            },
        });
    }
}
