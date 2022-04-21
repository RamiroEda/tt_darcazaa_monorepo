import 'reflect-metadata';
import { $log } from '@tsed/common';
import { PlatformExpress } from '@tsed/platform-express';
import { Server } from './server';

async function bootstrap() {
    try {
        const platform = await PlatformExpress.bootstrap(Server);

        await platform.listen();
    } catch (er) {
        $log.error(er);
    }
}

bootstrap();
