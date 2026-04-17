package com.tubes.nimons360.data.repository

import android.content.Context
import com.tubes.nimons360.data.api.ApiService
import com.tubes.nimons360.data.local.PinnedFamilyDao
import com.tubes.nimons360.data.local.PinnedFamilyEntity
import com.tubes.nimons360.model.*
import com.tubes.nimons360.utils.AppResult
import com.tubes.nimons360.utils.NetworkUtils
import com.tubes.nimons360.utils.TokenManager
import kotlinx.coroutines.flow.Flow

class FamilyRepository(
    private val api: ApiService,
    private val dao: PinnedFamilyDao,
    private val context: Context
) {

    private fun token() = TokenManager.getBearerToken(context)

    private fun handleCode(code: Int): AppResult.Error {
        if (code == 409) NetworkUtils.handleExpiredToken(context)
        return AppResult.Error("Request gagal (HTTP $code)", code)
    }

    suspend fun getMyFamilies(): AppResult<List<MyFamily>> {
        return try {
            val response = api.getMyFamilies(token())
            if (response.isSuccessful) {
                AppResult.Success(response.body()?.data ?: emptyList())
            } else {
                handleCode(response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Terjadi kesalahan")
        }
    }

    suspend fun discoverFamilies(): AppResult<List<DiscoverFamily>> {
        return try {
            val response = api.discoverFamilies(token())
            if (response.isSuccessful) {
                AppResult.Success(response.body()?.data ?: emptyList())
            } else {
                handleCode(response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Terjadi kesalahan")
        }
    }

    suspend fun getAllFamilies(): AppResult<List<FamilyItem>> {
        return try {
            val response = api.getAllFamilies(token())
            if (response.isSuccessful) {
                AppResult.Success(response.body()?.data ?: emptyList())
            } else {
                handleCode(response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Terjadi kesalahan")
        }
    }

    suspend fun getFamilyDetail(familyId: Int): AppResult<FamilyDetail> {
        return try {
            val response = api.getFamilyDetail(token(), familyId)
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) AppResult.Success(data)
                else AppResult.Error("Data tidak ditemukan")
            } else {
                handleCode(response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Terjadi kesalahan")
        }
    }

    suspend fun createFamily(name: String, iconUrl: String): AppResult<FamilyDetail> {
        return try {
            val response = api.createFamily(token(), CreateFamilyRequest(name, iconUrl))
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) AppResult.Success(data)
                else AppResult.Error("Data tidak ditemukan")
            } else {
                handleCode(response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Terjadi kesalahan")
        }
    }

    suspend fun joinFamily(familyId: Int, familyCode: String): AppResult<Boolean> {
        return try {
            val response = api.joinFamily(token(), JoinFamilyRequest(familyId, familyCode))
            if (response.isSuccessful) {
                AppResult.Success(response.body()?.data?.joined ?: false)
            } else {
                handleCode(response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Terjadi kesalahan")
        }
    }

    suspend fun leaveFamily(familyId: Int): AppResult<Boolean> {
        return try {
            val response = api.leaveFamily(token(), LeaveFamilyRequest(familyId))
            if (response.isSuccessful) {
                AppResult.Success(response.body()?.data?.left ?: false)
            } else {
                handleCode(response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Terjadi kesalahan")
        }
    }

    // Room operations
    fun getPinnedFamilies(): Flow<List<PinnedFamilyEntity>> = dao.getAllPinned()

    suspend fun pinFamily(family: PinnedFamilyEntity) = dao.pin(family)

    suspend fun unpinFamily(id: Int) = dao.unpin(id)

    suspend fun isPinned(id: Int): Boolean = dao.isPinned(id)
}
