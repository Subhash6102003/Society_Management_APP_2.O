package com.mgbheights.android.data.remote

import android.app.Activity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import timber.log.Timber

/**
 * Razorpay payment integration helper.
 * Handles checkout initialization and payment processing.
 */
object RazorpayHelper {

    private const val RAZORPAY_KEY = "rzp_test_XXXXXXXXXX" // Replace with actual key

    /**
     * Initialize Razorpay SDK.
     */
    fun init(activity: Activity) {
        Checkout.preload(activity.applicationContext)
    }

    /**
     * Start a Razorpay payment checkout.
     *
     * @param activity The current activity (must implement PaymentResultListener)
     * @param orderId Razorpay order ID from backend
     * @param amount Amount in paise (INR * 100)
     * @param description Payment description
     * @param userName User's name
     * @param userPhone User's phone number
     * @param userEmail User's email (optional)
     */
    fun startPayment(
        activity: Activity,
        orderId: String,
        amount: Long,
        description: String,
        userName: String,
        userPhone: String,
        userEmail: String = ""
    ) {
        try {
            val checkout = Checkout()
            checkout.setKeyID(RAZORPAY_KEY)

            val options = JSONObject().apply {
                put("name", "MGB Heights")
                put("description", description)
                put("order_id", orderId)
                put("currency", "INR")
                put("amount", amount)
                put("theme", JSONObject().apply {
                    put("color", "#EC3713")
                })
                put("prefill", JSONObject().apply {
                    put("contact", "+91$userPhone")
                    if (userEmail.isNotBlank()) put("email", userEmail)
                })
                put("retry", JSONObject().apply {
                    put("enabled", true)
                    put("max_count", 3)
                })
                put("send_sms_hash", true)
                put("remember_customer", true)
            }

            checkout.open(activity, options)
        } catch (e: Exception) {
            Timber.e(e, "Razorpay checkout error")
        }
    }

    /**
     * Start a maintenance payment.
     */
    fun startMaintenancePayment(
        activity: Activity,
        orderId: String,
        amount: Double,
        month: String,
        flatNumber: String,
        userName: String,
        userPhone: String,
        userEmail: String = ""
    ) {
        startPayment(
            activity = activity,
            orderId = orderId,
            amount = (amount * 100).toLong(), // Convert to paise
            description = "Maintenance - $month ($flatNumber)",
            userName = userName,
            userPhone = userPhone,
            userEmail = userEmail
        )
    }
}

