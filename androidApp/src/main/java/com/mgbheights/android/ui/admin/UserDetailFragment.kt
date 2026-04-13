import com.mgbheights.android.databinding.FragmentUserDetailBinding
    override fun onCreateView(
        return binding.root
        super.onViewCreated(view, savedInstanceState)
                // Load photo
class UserDetailFragment : Fragment(R.layout.fragment_user_detail)
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentUserDetailBinding
import com.mgbheights.android.util.PhotoCompressor
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.DateTimeUtil
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserDetailFragment : Fragment() {

    private var _binding: FragmentUserDetailBinding? = null
    private val binding get() = _binding!!

    // Safe Args — correct class name matches fragment class name
    private val args: UserDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId   = args.userId    // fix for unresolved 'userId'
        val userType = args.userType

        // Load user from Firestore
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                binding.tvName.text  = doc.getString("name") ?: ""
                binding.tvEmail.text = doc.getString("email") ?: ""
                binding.tvPhone.text = doc.getString("phone") ?: ""

                // Load photo
                val photoBase64 = when (userType) {
                    "guard", "worker" -> doc.getString("photoBase64") ?: ""
                    else              -> doc.getString("profilePhotoBase64") ?: ""
                }
                if (photoBase64.isNotEmpty()) {
                    PhotoCompressor.loadPhotoIntoView(binding.ivProfilePhoto, photoBase64, R.drawable.ic_profile)
                }

                // Show ID proof for guard/worker
                if (userType == "guard" || userType == "worker") {
                    binding.cardIdProof.visibility = View.VISIBLE
                    val idProof = doc.getString("idProofBase64") ?: ""
                    if (idProof.isNotEmpty()) {
                        PhotoCompressor.loadPhotoIntoView(binding.ivIdProof, idProof, R.drawable.ic_notices)
                    }
                    binding.tvStaffId.text = doc.getString("staffId") ?: ""
                }

                // Show residentName for tenant
                if (userType == "tenant") {
                    binding.tvResidentOwnerName.visibility = View.VISIBLE
                    binding.tvResidentOwnerName.text =
                        "Owner: ${doc.getString("residentName") ?: ""}"
                }

                // Show building and flat
                binding.tvFlat.text =
                    "Flat: ${doc.getString("flatNumber") ?: ""}, " +
                    "Building: ${doc.getString("buildingNumber") ?: ""}"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
