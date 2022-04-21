import { Routine } from './models/routine';

export function daysToNumber(days: boolean[]): number {
    let num = 0;

    days.forEach((_, index) => (num |= 1 << index));

    return num;
}

export function numberToDays(num: number): boolean[] {
    return new Array(7).map((_, index) => (num & (1 << index)) != 0);
}

export function executesToday(routine: Routine): boolean {
    return routine.repeat[new Date().getDay()];
}

export function getDayOfWeek(): number {
    const date = new Date();
    const day = date.getDay() - 1;

    if (day < 0) {
        return 6;
    } else {
        return day;
    }
}
