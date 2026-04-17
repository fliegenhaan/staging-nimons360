package com.tubes.nimons360.ui.family_detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tubes.nimons360.data.api.RetrofitClient
import com.tubes.nimons360.data.local.AppDatabase
import com.tubes.nimons360.data.local.PinnedFamilyEntity
import com.tubes.nimons360.data.repository.FamilyRepository
import com.tubes.nimons360.model.FamilyDetail
import com.tubes.nimons360.utils.AppResult
import com.tubes.nimons360.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FamilyDetailUiState {
    object Loading : FamilyDetailUiState()
    data class Success(val family: FamilyDetail, val isPinned: Boolean) : FamilyDetailUiState()
    data class Error(val message: String) : FamilyDetailUiState()
}

sealed class ActionState {
    object Idle : ActionState()
    object Loading : ActionState()
    object Success : ActionState()
    data class Error(val message: String) : ActionState()
}

class FamilyDetailViewModel(
    app: Application,
    private val familyId: Int
) : AndroidViewModel(app) {

    private val repo = FamilyRepository(
        RetrofitClient.instance,
        AppDatabase.getDatabase(app).pinnedFamilyDao(),
        app
    )

    private val _uiState = MutableStateFlow<FamilyDetailUiState>(FamilyDetailUiState.Loading)
    val uiState: StateFlow<FamilyDetailUiState> = _uiState.asStateFlow()

    private val _joinState = MutableStateFlow<ActionState>(ActionState.Idle)
    val joinState: StateFlow<ActionState> = _joinState.asStateFlow()

    private val _leaveState = MutableStateFlow<ActionState>(ActionState.Idle)
    val leaveState: StateFlow<ActionState> = _leaveState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        if (!NetworkUtils.isConnected(getApplication())) {
            _uiState.value = FamilyDetailUiState.Error("Tidak ada koneksi internet")
            return
        }
        _uiState.value = FamilyDetailUiState.Loading
        viewModelScope.launch {
            when (val result = repo.getFamilyDetail(familyId)) {
                is AppResult.Success -> {
                    val isPinned = repo.isPinned(familyId)
                    _uiState.value = FamilyDetailUiState.Success(result.data, isPinned)
                }
                is AppResult.Error -> {
                    _uiState.value = FamilyDetailUiState.Error(result.message)
                }
            }
        }
    }

    fun join(code: String) {
        _joinState.value = ActionState.Loading
        viewModelScope.launch {
            when (val result = repo.joinFamily(familyId, code)) {
                is AppResult.Success -> {
                    _joinState.value = ActionState.Success
                    loadDetail()
                }
                is AppResult.Error -> {
                    _joinState.value = ActionState.Error(result.message)
                }
            }
        }
    }

    fun leave() {
        _leaveState.value = ActionState.Loading
        viewModelScope.launch {
            when (val result = repo.leaveFamily(familyId)) {
                is AppResult.Success -> {
                    _leaveState.value = ActionState.Success
                    loadDetail()
                }
                is AppResult.Error -> {
                    _leaveState.value = ActionState.Error(result.message)
                }
            }
        }
    }

    fun togglePin() {
        val current = _uiState.value as? FamilyDetailUiState.Success ?: return
        viewModelScope.launch {
            if (current.isPinned) {
                repo.unpinFamily(familyId)
            } else {
                repo.pinFamily(
                    PinnedFamilyEntity(
                        familyId = current.family.id,
                        name = current.family.name,
                        iconUrl = current.family.iconUrl
                    )
                )
            }
            val newPinned = repo.isPinned(familyId)
            _uiState.value = current.copy(isPinned = newPinned)
        }
    }

    fun resetJoinState() { _joinState.value = ActionState.Idle }
    fun resetLeaveState() { _leaveState.value = ActionState.Idle }
}

class FamilyDetailViewModelFactory(
    private val app: Application,
    private val familyId: Int
) : ViewModelProvider.AndroidViewModelFactory(app) {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FamilyDetailViewModel(app, familyId) as T
    }
}
