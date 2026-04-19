package com.tubes.nimons360.ui.family_detail

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tubes.nimons360.ui.theme.Nimons360Theme

class FamilyDetailFragment : Fragment() {

    private val viewModel: FamilyDetailViewModel by viewModels {
        val familyId = arguments?.getInt("familyId", -1) ?: -1
        FamilyDetailViewModelFactory(
            requireActivity().application as Application,
            familyId
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadDetail()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Nimons360Theme {
                    FamilyDetailScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
