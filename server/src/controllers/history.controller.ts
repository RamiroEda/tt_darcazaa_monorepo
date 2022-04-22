import { HistoryService } from '@/services/history.service';
import { PathParams } from '@tsed/common';
import { Controller, Inject } from '@tsed/di';
import { Get, Required } from '@tsed/schema';

@Controller('/history')
export class HistoryController {
    @Inject()
    historyService!: HistoryService;

    @Get('/:id')
    getHistoryOfRoutine(@Required() @PathParams('id') id: string) {
        return this.historyService.getByRoutine(id);
    }
}
