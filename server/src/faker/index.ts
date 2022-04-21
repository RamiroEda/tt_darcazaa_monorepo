import missionService from '@/services/mission.service';

async function bootstrap() {
    missionService.deleteAll();
    missionService.addAll({
        routines: [
            {
                repeat: 0b1111111,
                start: 0,
                title: 'Waypoint test',
            },
        ],
        waypoints: [
            {
                index: 0,
                latitude: 22.757022,
                longitude: -102.580889,
                routine_id: 1,
            },
            {
                index: 1,
                latitude: 22.75622,
                longitude: -102.581167,
                routine_id: 1,
            },
            {
                index: 2,
                latitude: 22.75438,
                longitude: -102.580792,
                routine_id: 1,
            },
            {
                index: 3,
                latitude: 22.753886,
                longitude: -102.583335,
                routine_id: 1,
            },
            {
                index: 4,
                latitude: 22.755102,
                longitude: -102.584633,
                routine_id: 1,
            },
            {
                index: 5,
                latitude: 22.756676,
                longitude: -102.584021,
                routine_id: 1,
            },
        ],
    });
}

bootstrap();
