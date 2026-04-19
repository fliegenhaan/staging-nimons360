package com.tubes.nimons360.data.repository

import android.content.Context
import com.tubes.nimons360.data.api.ApiService
import com.tubes.nimons360.data.utils.ErrorMessage
import com.tubes.nimons360.data.utils.UserOp
import com.tubes.nimons360.model.UpdateNameRequest
import com.tubes.nimons360.model.UserDetail
import com.tubes.nimons360.utils.AppResult
import com.tubes.nimons360.utils.NetworkUtils
import com.tubes.nimons360.utils.TokenManager

class UserRepository(
    private val api: ApiService,
    private val context: Context
) {

    private fun token() = TokenManager.getBearerToken(context)

    private fun handleCode(code: Int, op: UserOp): AppResult.Error {
        if (code == 409) NetworkUtils.handleExpiredToken(context)
        return ErrorMessage.userError(code, op)
    }

    suspend fun getMe(): AppResult<UserDetail> {
        return try {
            val response = api.getMe(token())
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) AppResult.Success(data)
                else AppResult.Error("Data tidak ditemukan")
            } else {
                handleCode(response.code(), UserOp.GET_PROFILE)
            }
        } catch (e: Exception) {
            AppResult.Error(ErrorMessage.NETWORK_ERROR)
        }
    }

    suspend fun updateName(fullName: String): AppResult<UserDetail> {
        return try {
            val response = api.updateMe(token(), UpdateNameRequest(fullName))
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) AppResult.Success(data)
                else AppResult.Error("Data tidak ditemukan")
            } else {
                handleCode(response.code(), UserOp.UPDATE_NAME)
            }
        } catch (e: Exception) {
            AppResult.Error(ErrorMessage.NETWORK_ERROR)
        }
    }
}
