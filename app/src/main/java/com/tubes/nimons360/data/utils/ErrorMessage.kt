package com.tubes.nimons360.data.utils

import com.tubes.nimons360.utils.AppResult

enum class FamilyOp {
    CREATE,
    JOIN,
    LEAVE,
    GET_DETAIL,
    GET_LIST,
}

enum class UserOp {
    GET_PROFILE,
    UPDATE_NAME,
}

object ErrorMessage {

    fun familyError(code: Int, op: FamilyOp): AppResult.Error {
        val message = when (code) {
            400 -> when (op) {
                FamilyOp.CREATE -> "Data yang dikirim tidak valid. Periksa kembali formulir."
                FamilyOp.JOIN -> "Kode keluarga tidak valid."
                else -> "Gagal memuat keluarga. Coba lagi."
            }
            401 -> "Sesi berakhir. Silakan masuk kembali."
            403 -> when (op) {
                FamilyOp.JOIN -> "Kamu tidak memiliki akses untuk bergabung."
                FamilyOp.LEAVE, FamilyOp.GET_DETAIL -> "Kamu tidak memiliki akses ke keluarga ini."
                else -> "Tidak ada akses."
            }
            404 -> "Keluarga tidak ditemukan."
            409 -> when (op) {
                FamilyOp.CREATE -> "Nama keluarga sudah digunakan. Coba nama lain."
                FamilyOp.JOIN -> "Kamu sudah bergabung dengan keluarga ini."
                else -> "Operasi tidak dapat dilakukan."
            }
            429 -> "Terlalu banyak permintaan. Tunggu sebentar."
            in 500..599 -> "Server sedang bermasalah. Coba beberapa saat lagi."
            else -> when (op) {
                FamilyOp.CREATE -> "Gagal membuat keluarga. Coba lagi."
                FamilyOp.JOIN -> "Gagal bergabung. Coba lagi."
                FamilyOp.LEAVE -> "Gagal keluar keluarga. Coba lagi."
                FamilyOp.GET_DETAIL -> "Gagal memuat detail keluarga. Coba lagi."
                FamilyOp.GET_LIST -> "Gagal memuat keluarga. Coba lagi."
            }
        }
        return AppResult.Error(message, code)
    }

    fun userError(code: Int, op: UserOp): AppResult.Error {
        val message = when (code) {
            400 -> when (op) {
                UserOp.UPDATE_NAME -> "Nama tidak valid."
                else -> "Gagal memuat profil. Coba lagi."
            }
            401, 409 -> "Sesi berakhir. Silakan masuk kembali."
            403 -> "Tidak ada akses."
            404 -> "Akun tidak ditemukan."
            429 -> "Terlalu banyak permintaan. Tunggu sebentar."
            in 500..599 -> "Server sedang bermasalah."
            else -> when (op) {
                UserOp.GET_PROFILE -> "Gagal memuat profil. Coba lagi."
                UserOp.UPDATE_NAME -> "Gagal memperbarui nama. Coba lagi."
            }
        }
        return AppResult.Error(message, code)
    }

    const val NETWORK_ERROR = "Tidak dapat terhubung ke server. Periksa koneksi internet."
}