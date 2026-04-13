import com.mgbheights.android.databinding.FragmentComplaintDetailBinding
    private var _binding: FragmentComplaintDetailBinding? = null
        return binding.root
        super.onViewCreated(view, savedInstanceState)
            binding.progressLoading.isVisible = state is Resource.Loading
class ComplaintDetailFragment : Fragment(R.layout.fragment_complaint_detail)
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentComplaintDetailBinding
import com.mgbheights.shared.domain.model.ComplaintStatus
import com.mgbheights.shared.util.DateTimeUtil
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComplaintDetailFragment : Fragment() {

    private var _binding: FragmentComplaintDetailBinding? = null
    private val binding get() = _binding!!
    private val args: ComplaintDetailFragmentArgs by navArgs()
    private val viewModel: ComplaintViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComplaintDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        observeViewModel()
        viewModel.loadComplaint(args.complaintId)
    }

    private fun observeViewModel() {
        viewModel.complaintDetail.observe(viewLifecycleOwner) { state ->
            binding.progressLoading.isVisible = state is Resource.Loading
            when (state) {
                is Resource.Success -> {
                    val complaint = state.data
                    binding.tvTitle.text = complaint.title
                    binding.tvDescription.text = complaint.description
                    binding.tvTimestamp.text = DateTimeUtil.formatDateTime(complaint.createdAt)
                    
                    binding.chipStatus.text = complaint.status.name.lowercase().replaceFirstChar { it.uppercase() }
                    val statusColor = when (complaint.status) {
                        ComplaintStatus.OPEN -> R.color.status_warning
                        ComplaintStatus.IN_PROGRESS -> R.color.status_info
                        ComplaintStatus.RESOLVED -> R.color.status_success
                        else -> R.color.outline
                    }
                    binding.chipStatus.setChipBackgroundColorResource(statusColor)

                    if (complaint.resolution.isNotBlank()) {
                        binding.cardResolution.isVisible = true
                        binding.tvResolution.text = complaint.resolution
                        binding.tvResolvedAt.text = "Resolved on ${DateTimeUtil.formatDate(complaint.resolvedAt)}"
                    } else {
                        binding.cardResolution.isVisible = false
                    }
                    
                    // Photos
                    if (complaint.imageUrls.isNotEmpty()) {
                        binding.cardPhotos.isVisible = true
                        // Here you would normally setup a photo adapter for binding.rvPhotos
                    } else {
                        binding.cardPhotos.isVisible = false
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
