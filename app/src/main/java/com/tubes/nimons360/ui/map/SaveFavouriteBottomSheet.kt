package com.tubes.nimons360.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tubes.nimons360.data.local.AppDatabase
import com.tubes.nimons360.data.local.FavouriteLocationEntity
import com.tubes.nimons360.databinding.BottomSheetSaveFavouriteBinding
import kotlinx.coroutines.launch

class SaveFavouriteBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSaveFavouriteBinding? = null
    private val binding get() = _binding!!

    private val dao by lazy {
        AppDatabase.getDatabase(requireContext()).favouriteLocationDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSaveFavouriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lat = arguments?.getDouble("latitude") ?: return dismiss()
        val lon = arguments?.getDouble("longitude") ?: return dismiss()

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSave.setOnClickListener {
            val name = binding.etLocationName.text?.toString()?.trim()
            if (name.isNullOrEmpty()) {
                binding.etLocationName.error = "Nama lokasi wajib diisi"
                return@setOnClickListener
            }
            lifecycleScope.launch {
                dao.insert(FavouriteLocationEntity(name = name, latitude = lat, longitude = lon))
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(lat: Double, lon: Double): SaveFavouriteBottomSheet {
            return SaveFavouriteBottomSheet().apply {
                arguments = Bundle().apply {
                    putDouble("latitude", lat)
                    putDouble("longitude", lon)
                }
            }
        }
    }
}
