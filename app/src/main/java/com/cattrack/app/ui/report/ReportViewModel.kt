package com.cattrack.app.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cattrack.app.data.model.*
import com.cattrack.app.data.repository.CatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportUiState(
    val selectedCat: Cat? = null,
    val cats: List<Cat> = emptyList(),
    val weeklyReport: HealthReport? = null,
    val monthlyReport: HealthReport? = null,
    val selectedPeriod: ReportPeriod = ReportPeriod.WEEKLY,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val catRepository: CatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                catRepository.getAllCats()
                    .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                    .collect { cats ->
                        val selected = _uiState.value.selectedCat ?: cats.firstOrNull()
                        _uiState.update { it.copy(cats = cats, selectedCat = selected) }
                        selected?.let { generateReports(it.id) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun selectPeriod(period: ReportPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
    }

    fun selectCat(cat: Cat) {
        _uiState.update { it.copy(selectedCat = cat) }
        generateReports(cat.id)
    }

    fun refresh() {
        _uiState.value.selectedCat?.let { generateReports(it.id) }
    }

    private fun generateReports(catId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val weeklyReport = catRepository.generateWeeklyReport(catId)
                val monthlyReport = catRepository.generateMonthlyReport(catId)
                _uiState.update {
                    it.copy(
                        weeklyReport = weeklyReport,
                        monthlyReport = monthlyReport,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
