import com.mgbheights.android.databinding.FragmentNoticeListBinding
    private var _binding: FragmentNoticeListBinding? = null
        _binding = FragmentNoticeListBinding.inflate(inflater, container, false)
        noticeAdapter = NoticeAdapter { notice ->
    }
class NoticeListFragment : Fragment(R.layout.fragment_notice_list)
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentNoticeListBinding
import com.mgbheights.android.ui.adapter.NoticeAdapter
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeListFragment : Fragment() {

    private var _binding: FragmentNoticeListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NoticeViewModel by viewModels()
    private lateinit var noticeAdapter: NoticeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNoticeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noticeAdapter = NoticeAdapter { notice ->
            val action = NoticeListFragmentDirections.actionNoticeListToNoticeDetail(notice.id)
            findNavController().navigate(action)
        }
        binding.rvNotices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotices.adapter = noticeAdapter

        loadNotices()
    }

    private fun loadNotices() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.progressLoading.isVisible = true

        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener { userDoc ->
            val userType = userDoc.getString("userType")
            binding.fabCreateNotice.isVisible = userType == "admin"

            FirebaseFirestore.getInstance()
                .collection("notices")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { snapshot ->
                    binding.progressLoading.isVisible = false
                    val notices = snapshot.toObjects(com.mgbheights.shared.domain.model.Notice::class.java)
                    val filteredNotices = notices.filter {
                        it.targetUserType.equals("all", ignoreCase = true) || it.targetUserType.equals(userType, ignoreCase = true)
                    }
                    noticeAdapter.submitList(filteredNotices)
                    binding.layoutEmpty.isVisible = filteredNotices.isEmpty()
                }
                .addOnFailureListener {
                    binding.progressLoading.isVisible = false
                    binding.layoutEmpty.isVisible = true
                }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
