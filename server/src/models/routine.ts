import { Waypoint } from './waypoint';

export interface Routine {
    id: number;
    hash: string;
    start: number;
    repeat: boolean[];
    title: string;
    executedAt: number;
    waypoints: Waypoint[];
}

export interface RoutineModel {
    id: number;
    hash: string;
    start: number;
    repeat: number;
    title: string;
    executedAt: number;
}
