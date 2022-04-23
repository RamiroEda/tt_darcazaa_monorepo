export interface SystemStatus {
    status: Status;
    canceled: boolean;
}

export enum Status {
    STANDBY = 'STANDBY',
    CRITICAL = 'CRITICAL',
    UNINIT = 'UNINIT',
    BOOT = 'BOOT',
    CALIBRATING = 'CALIBRATING',
    ACTIVE = 'ACTIVE',
    EMERGENCY = 'EMERGENCY',
    POWEROFF = 'POWEROFF',
}
