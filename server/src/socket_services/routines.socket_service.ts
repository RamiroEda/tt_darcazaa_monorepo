import { MissionService } from '@/services/mission.service';
import {
    Nsp,
    SocketService,
    Input,
    Args,
    Socket,
    Broadcast,
    SocketSession,
} from '@tsed/socketio';
import * as SocketIO from 'socket.io';
import { SyncingStatus } from '@/models/syncing_status';
import { MissionStatus } from '@/models/mission_status';
import { Inject } from '@tsed/di';
import { Routine } from '@prisma/client';

@SocketService('/routines')
export class RoutinesSocketService {
    @Inject() missionService!: MissionService;

    @Nsp nsp?: SocketIO.Namespace;

    status: MissionStatus = MissionStatus.IDLE;
    battery: any = {
        level: 100,
        current: 0.0,
        voltage: 0.0,
    };
    location: any;
    currentMission: Routine | undefined;
    systemStatus: string | undefined;
    windspeed = 0;
    streamUri: string | undefined =
        'rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mp4'; //TODO: ELIMINAR

    $onConnection(
        @Socket socket: SocketIO.Socket,
        @SocketSession session: SocketSession,
    ) {
        console.log(`👋 New connection at ${socket.handshake.address}`);

        if (socket.handshake.auth?.authorization == 'driver') {
            console.log(`🛣️ Driver connection`);
            session.set('is_driver', true);
        }

        this.emitData(socket);
    }

    $onDisconnect(@SocketSession session: SocketSession) {
        if (session.get('is_driver')) {
            this.nsp?.emit('change_mode', 'AUTO');
        }
    }

    @Input('sync_routines')
    async syncRoutines(
        @Args(0)
        routines: {
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
        },
    ) {
        console.log('💱 Syncing...');

        this.nsp?.emit('sync', SyncingStatus.SYNCING);
        try {
            await this.missionService.deleteAll();
            await this.missionService.addAll(routines);
            await new Promise((res) => setTimeout(res, 2000));
            this.nsp?.emit('sync', SyncingStatus.SYNCED);
            this.nsp?.emit('hashes', await this.missionService.getHashes());
            console.log(`✅ Synced ${routines.routines.length} routines`);
        } catch (e) {
            console.error(e);
            this.nsp?.emit('sync', SyncingStatus.ERROR);
        }
    }

    @Input('emit_data')
    async emitData(@Socket socket: SocketIO.Socket) {
        socket.emit('status', this.status);
        if (this.battery) {
            socket.emit('battery', this.battery);
        }
        if (this.location) {
            socket.emit('location', this.location);
        }
        if (this.streamUri) {
            socket.emit('camera_stream_uri', this.streamUri);
        }
        socket.emit('current_mission', this.currentMission);
        socket.emit('hashes', await this.missionService.getHashes());
        socket.emit('windspeed', this.windspeed);
    }

    @Input('location')
    @Broadcast('location')
    updateLocation(@Args(0) location: any) {
        this.location = location;
        return this.location;
    }

    @Input('status')
    @Broadcast('status')
    updateStatus(@Args(0) status: MissionStatus) {
        this.status = status;
        return this.status;
    }

    setWindspeed(@Args(0) speed: number) {
        this.windspeed = speed;
        console.log(
            `🍃 Wind speed: ${speed} m/s  ${(speed * 3.6).toFixed(2)} km/h`,
        );

        this.nsp?.emit('windspeed', speed);
    }

    @Input('battery')
    @Broadcast('battery')
    updateBattery(@Args(0) battery: any) {
        this.battery = battery;
        return this.battery;
    }

    @Input('current_mission')
    @Broadcast('current_mission')
    async updateCurrentMission(@Args(0) currentMission: any) {
        console.log(this.currentMission, currentMission);

        if (this.currentMission && !currentMission) {
            if (this.currentMission.repeat.length > 0) {
                console.log('Deleting', this.currentMission.title);

                await this.missionService.delete(this.currentMission.id);
            } else {
                console.log('Marking as completed', this.currentMission.title);

                await this.missionService.markCompleted(this.currentMission);
            }
        }
        this.currentMission = currentMission;
        return this.currentMission;
    }

    @Input('system_status')
    @Broadcast('system_status')
    updateSystemStatus(@Args(0) systemStatus: any) {
        this.systemStatus = systemStatus;
        return this.systemStatus;
    }

    @Input('cancel')
    @Broadcast('cancel')
    cancelRoutine() {
        return;
    }

    @Input('translate')
    @Broadcast('translate')
    translate(@Args(0) velocities: any, @SocketSession session: SocketSession) {
        if (session.get('is_driver')) {
            return velocities;
        }
    }

    @Input('takeoff')
    @Broadcast('takeoff')
    takeoff(@SocketSession session: SocketSession) {
        if (session.get('is_driver')) {
            return;
        }
    }

    @Input('land')
    @Broadcast('land')
    land(@SocketSession session: SocketSession) {
        if (session.get('is_driver')) {
            return;
        }
    }

    @Input('camera_stream_uri')
    @Broadcast('camera_stream_uri')
    cameraStreamUri(@Args(0) uri: string) {
        console.log(`📺 Stream URI at ${uri}`);

        this.streamUri = uri;
        return this.streamUri;
    }

    @Input('rotate')
    @Broadcast('rotate')
    rotate(@Args(0) direction: number, @SocketSession session: SocketSession) {
        if (session.get('is_driver')) {
            return direction;
        }
    }

    @Input('change_mode')
    @Broadcast('change_mode')
    changeMode(@Args(0) mode: string, @SocketSession session: SocketSession) {
        if (session.get('is_driver')) {
            return mode;
        }
    }

    @Input('run_mission')
    async runMissionByHash(@Args(0) hash: string) {
        const mission = await this.missionService.getByHash(hash);

        if (mission) {
            this.runMission(mission);
        }
    }

    runMission(mission: Routine) {
        console.log(`🗺️ Mission found: ${mission.title}`);
        this.nsp?.emit('mission', mission);
    }
}
