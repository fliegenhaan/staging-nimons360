package com.tubes.nimons360.ui.families

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tubes.nimons360.R
import com.tubes.nimons360.ui.theme.Nimons360Theme

class FamiliesFragment : Fragment() {

    private val viewModel: FamiliesViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
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
                    FamiliesScreen(
                        viewModel = viewModel,
                        onFamilyClick = { familyId ->
                            val bundle = android.os.Bundle().apply { putInt("familyId", familyId) }
                            findNavController().navigate(R.id.action_families_to_familyDetail, bundle)
                        }
                    )
                }
            }
        }
    }
}
