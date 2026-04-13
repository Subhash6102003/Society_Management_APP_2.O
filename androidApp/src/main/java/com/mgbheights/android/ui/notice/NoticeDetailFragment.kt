package com.mgbheights.android.ui.notice

    }
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentNoticeDetailBinding
class NoticeDetailFragment : Fragment(R.layout.fragment_notice_detail)
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mgbheights.android.databinding.FragmentNoticeDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeDetailFragment : Fragment() {
    private var _binding: FragmentNoticeDetailBinding? = null
    private val binding get() = _binding!!
    private val args: NoticeDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNoticeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        // Load notice detail via ViewModel
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

