package com.mgbheights.android.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mgbheights.android.R
import java.io.File

/**
 * Reusable helper for camera/gallery photo capture in fragments.
 *
 * Usage in a Fragment:
 * 1. Create in onCreateView/onViewCreated:
 *      cameraHelper = CameraHelper(this) { uri -> handlePhotoResult(uri) }
 * 2. Show picker:
 *      cameraHelper.showPhotoPicker()
 */
class CameraHelper(
    private val fragment: Fragment,
    private val onPhotoResult: (Uri) -> Unit
) {
    private var photoUri: Uri? = null

    // Camera permission launcher
    private val cameraPermissionLauncher: ActivityResultLauncher<String> =
        fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCamera()
            } else {
                showPermissionDeniedDialog()
            }
        }

    // Camera capture launcher
    private val takePictureLauncher: ActivityResultLauncher<Uri> =
        fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                onPhotoResult(photoUri!!)
            }
        }

    // Gallery picker launcher
    private val pickImageLauncher: ActivityResultLauncher<String> =
        fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { onPhotoResult(it) }
        }

    /**
     * Show a dialog to choose Camera or Gallery.
     */
    fun showPhotoPicker() {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle("Choose Photo")
            .setItems(arrayOf("📷 Take Photo", "🖼 Choose from Gallery")) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        val ctx = fragment.requireContext()
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val ctx = fragment.requireContext()
        val photoFile = File(ctx.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(photoUri!!)
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle("Camera Permission Required")
            .setMessage("Camera access is required to capture photos. Please enable it in your phone settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", fragment.requireContext().packageName, null)
                }
                fragment.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        /**
         * Load an image URL into an ImageView with Coil, with a placeholder.
         */
        fun loadPhoto(imageView: ImageView, url: String?, placeholder: Int = R.drawable.ic_profile, circle: Boolean = true) {
            if (url.isNullOrBlank()) {
                imageView.setImageResource(placeholder)
            } else {
                imageView.load(url) {
                    crossfade(true)
                    placeholder(placeholder)
                    error(placeholder)
                    if (circle) transformations(CircleCropTransformation())
                }
            }
        }
    }
}

