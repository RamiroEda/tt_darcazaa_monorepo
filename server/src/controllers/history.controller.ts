import { HistoryService } from '@/services/history.service';
import { PathParams } from '@tsed/common';
import { Controller, Inject } from '@tsed/di';
import { Get, Required } from '@tsed/schema';

@Controller('/history')
export class HistoryController {
    @Inject()
    historyService!: HistoryService;

    @Get('/:hash')
    getHistoryOfRoutine(@Required() @PathParams('hash') hash: string) {
        return this.historyService.getByRoutineHash(hash);
    }
}
