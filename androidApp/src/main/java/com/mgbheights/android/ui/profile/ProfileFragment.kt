package com.mgbheights.android.ui.profile
                .setTitle("Logout")
                if (photoBase64.isNotEmpty()) {
        // Edit profile button

class ProfileFragment : Fragment(R.layout.fragment_profile)
                }
                .setNegativeButton("Cancel", null)
                .show()
        binding.fabChangePhoto.setOnClickListener { cameraHelper.showPhotoPicker() }
import androidx.navigation.NavOptions
        binding.btnEditProfile.setOnClickListener { findNavController().navigate(R.id.action_profile_to_editProfile) }
                    // We can add a field in the UI for this or use it in other places
                    PhotoCompressor.loadPhotoIntoView(
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
import com.mgbheights.app.utils.ImageUtils
                }
                    val isStaffRole = user.role == UserRole.WORKER ||
                            user.role == UserRole.SECURITY_GUARD ||
                        user.profilePhotoUrl,
                    binding.tvEmail.text = user.email.ifBlank { "Not set" }
                    }
                    // Show account creation date (Fix for 1970 issue - using shared utility)

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var cameraHelper: CameraHelper

    override fun onCreateView(
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Load user data
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener
import androidx.fragment.app.Fragment
                binding.tvName.text          = doc.getString("name") ?: ""
                binding.tvEmail.text         = doc.getString("email") ?: ""
                binding.tvPhone.text         = doc.getString("phone") ?: ""
                binding.tvBuilding.text      = doc.getString("buildingNumber") ?: ""
                binding.tvFlat.text          = doc.getString("flatNumber") ?: ""
import com.mgbheights.android.R
                // Load profile photo from Base64
                val photoBase64 = doc.getString("profilePhotoBase64") ?: ""
                if (photoBase64.isNotEmpty()) {
                    val bitmap = ImageUtils.base64ToBitmap(photoBase64)
                    bitmap?.let { binding.ivProfilePhoto.setImageBitmap(it) }
@AndroidEntryPoint
    private val binding get() = _binding!!
    private lateinit var cameraHelper: CameraHelper
        // Edit profile button
        binding.btnEditProfile.setOnClickListener {
            // Navigate to edit profile using correct action ID
            findNavController().navigate(R.id.action_profile_to_edit)
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraHelper = CameraHelper(this) { uri ->
            val base64 = PhotoCompressor.compressProfilePhoto(requireContext(), uri)
            if (base64 != null) {
                PhotoCompressor.loadPhotoIntoView(binding.ivProfilePhoto, base64, R.drawable.ic_profile)
            }
        }
        cameraHelper = CameraHelper(this) { uri ->
        // Logout button
            val base64 = PhotoCompressor.compressProfilePhoto(requireContext(), uri)
            FirebaseAuth.getInstance().signOut()
            // Navigate to enterEmailFragment and clear back stack
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph_auth, true)
                .build()
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
        super.onViewCreated(view, savedInstanceState)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
        viewModel.user.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    val user = state.data
                    binding.tvUserName.text = user.name.ifBlank { "User" }
                    binding.tvUserRole.text = user.role.name.replace("_", " ")
                    binding.tvPhone.text = user.phoneNumber.ifBlank { "Not set" }
                    binding.tvEmail.text = user.email.ifBlank { "Not set" }

                    // Display profile photo
                    PhotoCompressor.loadPhotoIntoView(
                        binding.ivProfilePhoto,
                        user.profilePhotoUrl,
                        R.drawable.ic_profile
                    )

                    // Show account creation date (Fix for 1970 issue - using shared utility)
                    // We can add a field in the UI for this or use it in other places

                    val isStaffRole = user.role == UserRole.WORKER ||
                            user.role == UserRole.SECURITY_GUARD ||
                            user.role == UserRole.SECURITY_GUARD_WORKER
                    binding.cardHouseDetails.isVisible = !isStaffRole
                    if (!isStaffRole) {
                        binding.tvFlatNumber.text = user.flatNumber.ifBlank { "N/A" }
                        binding.tvTowerBlock.text = user.towerBlock.ifBlank { "N/A" }
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.fabChangePhoto.setOnClickListener { cameraHelper.showPhotoPicker() }
        binding.btnEditProfile.setOnClickListener { findNavController().navigate(R.id.action_profile_to_editProfile) }
        binding.btnPaymentHistory.setOnClickListener { findNavController().navigate(R.id.action_profile_to_paymentHistory) }
        binding.btnMyComplaints.setOnClickListener { findNavController().navigate(R.id.action_profile_to_complaints) }

        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    viewModel.logout()
                    findNavController().navigate(R.id.action_profile_to_login)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
