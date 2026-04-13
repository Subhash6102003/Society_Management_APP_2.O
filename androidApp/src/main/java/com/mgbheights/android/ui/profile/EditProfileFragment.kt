import com.mgbheights.android.databinding.FragmentEditProfileBinding
            val updates = hashMapOf<String, Any>(

                    Toast.makeText(requireContext(),
            val building = binding.etBuilding.text.toString().trim()
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile)
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentEditProfileBinding
import com.mgbheights.android.util.CameraHelper
import com.mgbheights.android.util.PhotoCompressor
import com.mgbheights.android.util.ImageUtils
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private var selectedBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Load current data into edit fields
        FirebaseFirestore.getInstance()
            .collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                binding.etName.setText(doc.getString("name"))
                binding.etPhone.setText(doc.getString("phone"))
                binding.etBuilding.setText(doc.getString("buildingNumber"))
                binding.etFlat.setText(doc.getString("flatNumber"))
                val photo = doc.getString("profilePhotoBase64") ?: ""
                if (photo.isNotEmpty()) {
                    ImageUtils.base64ToBitmap(photo)?.let {
                        binding.ivPhoto.setImageBitmap(it)
                    }
                }
            }

        // Photo picker
        val launcher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(
                    requireActivity().contentResolver, it
                )
                selectedBitmap = bitmap
                binding.ivPhoto.setImageBitmap(bitmap)
            }
        }
        binding.btnChangePhoto.setOnClickListener {
            launcher.launch("image/*")
        }

        // Save button
        binding.btnSave.setOnClickListener {
            val name     = binding.etName.text.toString().trim()
            val phone    = binding.etPhone.text.toString().trim()
            val building = binding.etBuilding.text.toString().trim()
            val flat     = binding.etFlat.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() ||
                building.isEmpty() || flat.isEmpty()) {
                Toast.makeText(requireContext(),
                    "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updates = hashMapOf<String, Any>(
                "name"           to name,
                "phone"          to phone,
                "buildingNumber" to building,
                "flatNumber"     to flat
            )
            selectedBitmap?.let {
                updates["profilePhotoBase64"] = ImageUtils.bitmapToBase64(it, 50)
            }

            FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(),
                        "Profile updated!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
