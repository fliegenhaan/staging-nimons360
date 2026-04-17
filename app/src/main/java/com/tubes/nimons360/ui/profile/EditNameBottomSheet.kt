package com.tubes.nimons360.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tubes.nimons360.databinding.FragmentEditNameBinding

class EditNameBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentEditNameBinding? = null
    private val binding get() = _binding!!

    private var onSaveCallback: ((String) -> Unit)? = null

    fun setOnSaveCallback(callback: (String) -> Unit): EditNameBottomSheet {
        onSaveCallback = callback
        return this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentName = arguments?.getString("currentName") ?: ""
        binding.etNewName.setText(currentName)

        binding.btnSave.setOnClickListener {
            val newName = binding.etNewName.text?.toString()?.trim() ?: ""
            if (newName.isEmpty()) {
                binding.tilNewName.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }
            binding.tilNewName.error = null
            onSaveCallback?.invoke(newName)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(currentName: String): EditNameBottomSheet {
            return EditNameBottomSheet().apply {
                arguments = Bundle().apply { putString("currentName", currentName) }
            }
        }
    }
}
