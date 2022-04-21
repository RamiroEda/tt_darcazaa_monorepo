import { Configuration, Inject } from '@tsed/di';
import { PlatformApplication } from '@tsed/common';
import bodyParser from 'body-parser';
import compress from 'compression';
import cookieParser from 'cookie-parser';
import methodOverride from 'method-override';
import schedule = require('node-schedule');
import missionService from '@/services/mission.service';
import '@tsed/socketio';
import { RoutinesSocketService } from './socket_services/routines.socket_service';
import { PORT } from './constants';
import axios = require('axios');
import qrTerminal = require('qrcode-terminal');
import os = require('os');

const rootDir = __dirname;

@Configuration({
    rootDir,
    acceptMimes: ['application/json'],
    socketIO: {},
    port: PORT,
    logger: {
        disableRoutesSummary: true,
    },
})
export class Server {
    @Inject()
    app!: PlatformApplication;

    @Configuration()
    settings!: Configuration;

    isRunning = false;

    constructor(private routinesService: RoutinesSocketService) {}

    public async $onInit() {
        await this.checkWeather();
        this.checkDatabase();
        schedule.scheduleJob('*/1 * * * *', async () => {
            await this.checkWeather();
            this.checkDatabase();
        });

        const interfaces = Object.entries(os.networkInterfaces());

        for (const netInterface of interfaces) {
            for (const net of netInterface[1] ?? []) {
                if (net.family === 'IPv4' && !net.internal) {
                    console.log(
                        `\n\nIP para ${netInterface[0]}: ${net.address}`,
                    );

                    qrTerminal.generate(net.address);
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

    private async checkDatabase() {
        if (this.isRunning) return;
        console.log('⌛ Checking database...');

        const missions = await missionService.getAll();

        if (missions.length > 0) {
            this.isRunning = true;
            const mission = missions[0];
            this.routinesService.runMission(mission);

            this.isRunning = false;
        } else {
            console.log('❎ No missions found');
        }
    }

    public $beforeRoutesInit(): void | Promise<any> {
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
}
