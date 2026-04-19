package com.tubes.nimons360.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tubes.nimons360.data.local.AppDatabase
import com.tubes.nimons360.databinding.BottomSheetFavouriteInfoBinding
import kotlinx.coroutines.launch

class FavouriteInfoBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFavouriteInfoBinding? = null
    private val binding get() = _binding!!

    private val dao by lazy {
        AppDatabase.getDatabase(requireContext()).favouriteLocationDao()
    }

    private var favouriteId: Int = -1
    private var onDeleted: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFavouriteInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favouriteId = arguments?.getInt("id", -1) ?: -1
        val name = arguments?.getString("name", "") ?: ""

        binding.tvFavName.text = name
        binding.tvFavName.tag = name

        binding.btnDelete.setOnClickListener {
            if (favouriteId == -1) return@setOnClickListener
            lifecycleScope.launch {
                dao.delete(favouriteId)
                dismiss()
                onDeleted?.invoke()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setOnDeletedListener(listener: () -> Unit) {
        onDeleted = listener
    }

    companion object {
        fun newInstance(id: Int, name: String): FavouriteInfoBottomSheet {
            return FavouriteInfoBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt("id", id)
                    putString("name", name)
                }
            }
        }
    }
}
