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
import os = require('os');
import { HistoryController } from './controllers/history.controller';
import '@tsed/socketio';
import '@tsed/platform-express';

const rootDir = __dirname;

@Configuration({
    rootDir,
    acceptMimes: ['application/json'],
    socketIO: {},
    port: PORT,
    mount: {
        '/': [HistoryController],
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

    @Configuration()
    settings!: Configuration;

    isExecutingRoutine = false;

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

    private printNetworkInterfaces() {
        const interfaces = Object.entries(os.networkInterfaces());

        for (const netInterface of interfaces) {
            for (const net of netInterface[1] ?? []) {
                if (net.family === 'IPv4' && !net.internal) {
                    console.log(
                        `\n\nIP para ${netInterface[0]}: ${net.address}:${PORT}`,
                    );

                    qrTerminal.generate(`${net.address}:${PORT}`);
                    console.log('\n\n');
                }
            }
        }
    }

    private async checkWeather() {
        const response = await axios.default.get(
            'https://api.openweathermap.org/data/2.5/weather?lat=22.73897&lon=-102.66891&appid=28df6777f87bddccdea41dd51c9446a6', //TODO: LatLng dinamico
        );

        this.routinesService.setWindspeed(response.data.wind.speed);
    }

    private async checkPendingRoutines() {
        if (this.isExecutingRoutine) return;
        console.log('⌛ Checking database...');

        const missions = await this.missionService.getAll();

        if (missions.length > 0) {
            this.isExecutingRoutine = true;
            const mission = missions[0];
            this.routinesService.runMission(mission);

            this.isExecutingRoutine = false;
        } else {
            console.log('❎ No missions found');
        }
    }
}
