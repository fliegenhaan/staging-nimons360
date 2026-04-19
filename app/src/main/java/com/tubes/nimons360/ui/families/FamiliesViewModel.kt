package com.tubes.nimons360.ui.families

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tubes.nimons360.Nimons360App
import com.tubes.nimons360.data.api.RetrofitClient
import com.tubes.nimons360.data.local.AppDatabase
import com.tubes.nimons360.data.local.PinnedFamilyEntity
import com.tubes.nimons360.data.repository.FamilyRepository
import com.tubes.nimons360.model.FamilyItem
import com.tubes.nimons360.model.MyFamily
import com.tubes.nimons360.utils.AppResult
import com.tubes.nimons360.utils.NetworkUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FilterMode { ALL, MY_FAMILIES }

sealed class FamiliesUiState {
    object Loading : FamiliesUiState()
    object Success : FamiliesUiState()
    data class Error(val message: String) : FamiliesUiState()
}

class FamiliesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FamilyRepository(
        RetrofitClient.instance,
        AppDatabase.getDatabase(app).pinnedFamilyDao(),
        app
    )

    private val _allFamilies = MutableStateFlow<List<FamilyItem>>(emptyList())
    private val _myFamilies = MutableStateFlow<List<MyFamily>>(emptyList())

    private val _pinnedFamilies = MutableStateFlow<List<PinnedFamilyEntity>>(emptyList())
    val pinnedFamilies: StateFlow<List<PinnedFamilyEntity>> = _pinnedFamilies.asStateFlow()

    private val _filterMode = MutableStateFlow(FilterMode.ALL)
    val filterMode: StateFlow<FilterMode> = _filterMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<FamiliesUiState>(FamiliesUiState.Loading)
    val uiState: StateFlow<FamiliesUiState> = _uiState.asStateFlow()

    val displayedFamilies: StateFlow<List<FamilyItem>> = combine(
        _allFamilies, _myFamilies, _filterMode, _searchQuery
    ) { all, my, mode, query ->
        val base = when (mode) {
            FilterMode.ALL -> all
            FilterMode.MY_FAMILIES -> my.map { FamilyItem(it.id, it.name, it.iconUrl) }
        }
        if (query.isBlank()) base
        else base.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Observe pinned families from Room
        viewModelScope.launch {
            repo.getPinnedFamilies().collect { pinned ->
                _pinnedFamilies.value = pinned
            }
        }
        viewModelScope.launch {
            (app.applicationContext as Nimons360App).connectionRestored.filter {
                _uiState.value is FamiliesUiState.Error
            }.collect {
                loadData()
            }
        }
        loadData()
    }

    fun loadData() {
        if (!NetworkUtils.isConnected(getApplication())) {
            _uiState.value = FamiliesUiState.Error("Tidak ada koneksi internet")
            return
        }
        _uiState.value = FamiliesUiState.Loading
        viewModelScope.launch {
            val allDeferred = async { repo.getAllFamilies() }
            val myDeferred = async { repo.getMyFamilies() }

            val allResult = allDeferred.await()
            val myResult = myDeferred.await()

            if (allResult is AppResult.Error) {
                _uiState.value = FamiliesUiState.Error(allResult.message)
                return@launch
            }
            _allFamilies.value = (allResult as AppResult.Success).data
            if (myResult is AppResult.Success) {
                _myFamilies.value = myResult.data
            }
            _uiState.value = FamiliesUiState.Success
        }
    }

    fun setFilter(mode: FilterMode) {
        _filterMode.value = mode
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun pinFamily(family: FamilyItem) {
        viewModelScope.launch {
            repo.pinFamily(PinnedFamilyEntity(family.id, family.name, family.iconUrl))
        }
    }

    fun unpinFamily(id: Int) {
        viewModelScope.launch {
            repo.unpinFamily(id)
        }
    }
}
