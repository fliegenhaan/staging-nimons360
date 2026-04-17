package com.tubes.nimons360.data.api

import com.tubes.nimons360.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<UserResponse>

    @PATCH("api/me")
    suspend fun updateMe(
        @Header("Authorization") token: String,
        @Body request: UpdateNameRequest
    ): Response<UserResponse>

    @GET("api/families")
    suspend fun getAllFamilies(@Header("Authorization") token: String): Response<FamiliesResponse>

    @GET("api/me/families")
    suspend fun getMyFamilies(@Header("Authorization") token: String): Response<MyFamiliesResponse>

    @GET("api/families/discover")
    suspend fun discoverFamilies(@Header("Authorization") token: String): Response<DiscoverResponse>

    @GET("api/families/{familyId}")
    suspend fun getFamilyDetail(
        @Header("Authorization") token: String,
        @Path("familyId") familyId: Int
    ): Response<FamilyDetailResponse>

    @POST("api/families")
    suspend fun createFamily(
        @Header("Authorization") token: String,
        @Body request: CreateFamilyRequest
    ): Response<FamilyDetailResponse>

    @POST("api/families/join")
    suspend fun joinFamily(
        @Header("Authorization") token: String,
        @Body request: JoinFamilyRequest
    ): Response<JoinResponse>

    @POST("api/families/leave")
    suspend fun leaveFamily(
        @Header("Authorization") token: String,
        @Body request: LeaveFamilyRequest
    ): Response<LeaveResponse>
}