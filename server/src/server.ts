import { BaseController } from './controllers/base.controller';
import { MissionService } from './services/mission.service';
import { Configuration, Inject } from '@tsed/di';
import {
    AfterRoutesInit,
    BeforeRoutesInit,
    PlatformApplication,
} from '@tsed/common';
import bodyParser from 'body-parser';
import compress from 'compression';
import cookieParser from 'cookie-parser';
import methodOverride from 'method-override';
import schedule = require('node-schedule');
import { RoutinesSocketService } from './socket_services/routines.socket_service';
import { PORT } from './constants';
import axios = require('axios');
import qrTerminal = require('qrcode-terminal');
import { HistoryController } from './controllers/history.controller';
import '@tsed/socketio';
import '@tsed/platform-express';
import { Status } from './models/system_status';

const rootDir = __dirname;

@Configuration({
    rootDir,
    acceptMimes: ['application/json'],
    socketIO: {},
    port: PORT,
    mount: {
        '/': [HistoryController, BaseController],
    },
    logger: {
        disableRoutesSummary: true,
    },
})
export class Server implements BeforeRoutesInit, AfterRoutesInit {
    @Inject()
    app!: PlatformApplication;

    @Inject()
    missionService!: MissionService;

    @Inject()
    routinesSocketService!: RoutinesSocketService;

    @Inject()
    baseController!: BaseController;

    @Configuration()
    settings!: Configuration;

    constructor(private routinesService: RoutinesSocketService) {}

    async $beforeRoutesInit(): Promise<any> {
        this.app
            .use(cookieParser())
            .use(compress({}))
            .use(methodOverride())
            .use(bodyParser.json())
            .use(
                bodyParser.urlencoded({
                    extended: true,
                }),
            );
    }

    async $afterRoutesInit(): Promise<any> {
        await this.checkWeather();
        await this.checkPendingRoutines();
        schedule.scheduleJob('*/1 * * * *', async () => {
            await this.checkWeather();
            await this.checkPendingRoutines();
        });
        this.printNetworkInterfaces();
    }

    private async printNetworkInterfaces() {
        const networks = await this.baseController.getNetworks();

        networks.forEach((it) => {
            console.log(it);

            qrTerminal.generate(it);
        });
    }

    private async checkWeather() {
        try {
            const response = await axios.default.get(
                'https://api.openweathermap.org/data/2.5/weather?lat=22.73897&lon=-102.66891&appid=28df6777f87bddccdea41dd51c9446a6', //TODO: LatLng dinamico
            );

            this.routinesService.setWindspeed(response.data.wind.speed);
        } catch (e) {
            console.log('🌐 No internet connection');
        }
    }

    private async checkPendingRoutines() {
        if (this.routinesSocketService.systemStatus?.status === Status.ACTIVE)
            return;
        console.log('⌛ Checking database...');

        const missions = await this.missionService.getPendingRoutines();

        if (missions.length > 0) {
            const mission = missions[0];
            this.routinesService.runMission(mission);
        } else {
            console.log('❎ No missions found');
        }
    }
}
