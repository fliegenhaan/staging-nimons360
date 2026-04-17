package com.tubes.nimons360.model

data class FamiliesResponse(
    val data: List<FamilyItem>
)

data class FamilyItem(
    val id: Int,
    val name: String,
    val iconUrl: String
)

data class MyFamiliesResponse(
    val data: List<MyFamily>
)

data class MyFamily(
    val id: Int,
    val name: String,
    val iconUrl: String,
    val familyCode: String,
    val createdAt: String,
    val updatedAt: String,
    val members: List<FamilyMember>
)

data class FamilyMember(
    val id: Int? = null,
    val fullName: String,
    val email: String,
    val joinedAt: String? = null
)

data class DiscoverResponse(
    val data: List<DiscoverFamily>
)

data class DiscoverFamily(
    val id: Int,
    val name: String,
    val iconUrl: String,
    val createdAt: String,
    val members: List<FamilyMember>
)

data class FamilyDetailResponse(
    val data: FamilyDetail
)

data class FamilyDetail(
    val id: Int,
    val name: String,
    val iconUrl: String,
    val isMember: Boolean,
    val familyCode: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val members: List<FamilyMember>
)

data class CreateFamilyRequest(
    val name: String,
    val iconUrl: String
)

data class JoinFamilyRequest(
    val familyId: Int,
    val familyCode: String
)

data class JoinResponse(
    val data: JoinData
)

data class JoinData(
    val joined: Boolean
)

data class LeaveFamilyRequest(
    val familyId: Int
)

data class LeaveResponse(
    val data: LeaveData
)

data class LeaveData(
    val left: Boolean
)