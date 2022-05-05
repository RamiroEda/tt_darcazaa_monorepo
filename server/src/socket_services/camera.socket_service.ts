import { PlatformApplication } from '@tsed/common';
import { Inject } from '@tsed/di';
import { Args, Input, Nsp, SocketService } from '@tsed/socketio';
import * as SocketIO from 'socket.io';
import rstp from 'rtsp-relay';

@SocketService('/camera')
export class CameraSocketService {
    @Inject()
    app!: PlatformApplication;

    @Nsp nsp!: SocketIO.Namespace;

    rstp?: any;

    url?: string;

    $onConnection() {
        console.log('👀 New camera viewer');
    }

    $onNamespaceInit() {
        this.rstp = rstp(this.app.raw);
        this.initListener();
    }

    changeUrl(url: string) {
        this.url = url;
        this.initListener();
    }

    @Input('camera_data')
    sendCamedaBuffer(@Args(0) data: any) {
        console.log(data);

        this.nsp.send(data);
    }

    initListener() {
        if (this.url) {
            this.rstp.proxy({
                url: this.url,
                verbose: true,
                transport: 'tcp',
            })({
                send: (data: Buffer) => {
                    this.nsp.send(data);
                },
                on: this.nsp.on,
            });
        }
    }
}
