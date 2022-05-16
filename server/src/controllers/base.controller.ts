import { HOST_IP, PORT } from '@/constants';
import { Controller } from '@tsed/di';
import { ContentType, Get } from '@tsed/schema';
import os = require('os');
import fs = require('fs');

@Controller('/')
@ContentType('.html')
export class BaseController {
    getWifiInfo(): { ssid: string; pass: string } | undefined {
        let file: string | undefined;
        console.log(
            'wpa_supplicant',
            fs.existsSync('/etc/wpa_supplicant/wpa_supplicant.conf'),
        );

        if (fs.existsSync('/etc/wpa_supplicant/wpa_supplicant.conf')) {
            file = fs
                .readFileSync('/etc/wpa_supplicant/wpa_supplicant.conf')
                .toString('utf8');
        }

        const matches = file?.match(/(?<=network=\{)(\n|.)*(?=\})/);

        const wifiConfig = matches?.[0].replace(/ +/gm, '');

        if (!wifiConfig) return undefined;

        const config = Object.fromEntries(
            wifiConfig
                .split('\n')
                .filter((it) => it.length > 0)
                .map((it) => it.replace(/"/g, '').split('=')),
        );

        return {
            ssid: config.ssid,
            pass: config.psk,
        };
    }
    async getNetworks(): Promise<string[]> {
        const result = [];
        const wifi = this.getWifiInfo();

        if (HOST_IP) {
            result.push(
                JSON.stringify({
                    ip: `${HOST_IP}:${PORT}`,
                    ...wifi,
                }),
            );
        } else {
            const interfaces = Object.entries(os.networkInterfaces());

            for (const netInterface of interfaces) {
                for (const net of netInterface[1] ?? []) {
                    if (net.family === 'IPv4' && !net.internal) {
                        result.push(
                            JSON.stringify({
                                ip: `${net.address}:${PORT}`,
                                ...wifi,
                            }),
                        );
                    }
                }
            }
        }

        return result;
    }

    @Get()
    async index() {
        const result = await this.getNetworks();
        return `
            <html>
                <body>
                    ${result.map(
                        (it) =>
                            `
                                ${it}
                                <br/>
                                <img src="https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=${encodeURI(
                                    it,
                                )}"/>
                                <br/>
                                <br/>
                                <br/>
                            `,
                    )}
                </body>
            </html>
        `;
    }
}
