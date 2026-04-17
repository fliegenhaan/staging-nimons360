package com.tubes.nimons360.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tubes.nimons360.databinding.FragmentUserInfoBinding
import com.tubes.nimons360.model.MemberPresence

class UserInfoBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentUserInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments ?: return

        binding.tvName.text = args.getString("fullName", "")
        binding.tvEmail.text = args.getString("email", "")
        binding.tvLatitude.text = "%.4f".format(args.getDouble("latitude"))
        binding.tvLongitude.text = "%.4f".format(args.getDouble("longitude"))
        val isCharging = args.getBoolean("isCharging")
        val chargingText = if (isCharging) " ⚡" else ""
        binding.tvBattery.text = "${args.getInt("batteryLevel")}%$chargingText"
        binding.tvInternet.text = when (args.getString("internetStatus")) {
            "wifi" -> "WiFi"
            "mobile" -> "Data Seluler"
            else -> args.getString("internetStatus") ?: "-"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(presence: MemberPresence): UserInfoBottomSheet {
            return UserInfoBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt("userId", presence.userId)
                    putString("fullName", presence.fullName)
                    putString("email", presence.email)
                    putDouble("latitude", presence.latitude)
                    putDouble("longitude", presence.longitude)
                    putFloat("rotation", presence.rotation)
                    putInt("batteryLevel", presence.batteryLevel)
                    putBoolean("isCharging", presence.isCharging)
                    putString("internetStatus", presence.internetStatus)
                }
            }
        }
    }
}
