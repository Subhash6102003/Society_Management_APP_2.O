package com.mgbheights.android.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.mgbheights.android.R

/**
 * Extension to check if the device is connected to the internet.
 */
fun Context.isNetworkAvailable(): Boolean {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
    return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}

/**
 * Show a short toast message.
 */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * Show a long toast message.
 */
fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

/**
 * Show a Snackbar with optional action.
 */
fun View.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    actionText: String? = null,
    action: (() -> Unit)? = null
) {
    val snackbar = Snackbar.make(this, message, duration)
    if (actionText != null && action != null) {
        snackbar.setAction(actionText) { action() }
    }
    snackbar.show()
}

/**
 * Show error Snackbar with retry action.
 */
fun View.showErrorSnackbar(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    if (onRetry != null) {
        snackbar.setAction("Retry") { onRetry() }
    }
    snackbar.setBackgroundTint(context.getColor(R.color.error))
    snackbar.setTextColor(context.getColor(R.color.on_error))
    snackbar.show()
}

/**
 * Fragment extension to show toast.
 */
fun Fragment.showToast(message: String) {
    requireContext().showToast(message)
}

/**
 * Fragment extension for Snackbar on root view.
 */
fun Fragment.showSnackbar(message: String) {
    view?.showSnackbar(message)
}

