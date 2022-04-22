package mx.ipn.upiiz.darcazaa.view_models

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.ipn.upiiz.darcazaa.data.models.History
import mx.ipn.upiiz.darcazaa.data.repositories.HistoryRepository

class HistoryViewModel (
    private val hash: String,
    private val historyRepository: HistoryRepository
): ViewModel() {
    val history = mutableStateListOf<History>()

    init {
        fetchHistory()
    }

    private fun fetchHistory() = viewModelScope.launch {
        kotlin.runCatching {
            historyRepository.historyByHash(hash)
        }.onSuccess {
            history.addAll(it)
        }.onFailure {
            it.printStackTrace()
        }
    }
}