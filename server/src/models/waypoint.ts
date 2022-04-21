export interface Waypoint {
    routine_id: number;
    index: number;
    latitude: number;
    longitude: number;
}

export interface WaypointModel extends Partial<Waypoint> {
    id: string;
    routine_id: number;
    index: number;
    latitude: number;
    longitude: number;
}
