package com.mgbheights.shared.util

object Validators {

    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.length == Constants.PHONE_NUMBER_LENGTH && phone.all { it.isDigit() }
    }

    fun isValidOtp(otp: String): Boolean {
        return otp.length == Constants.OTP_LENGTH && otp.all { it.isDigit() }
    }

    fun isValidName(name: String): Boolean {
        return name.length in Constants.MIN_NAME_LENGTH..Constants.MAX_NAME_LENGTH &&
                name.all { it.isLetter() || it.isWhitespace() }
    }

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return true // Email is optional
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(Regex(emailRegex))
    }

    fun isValidFlatNumber(flatNumber: String): Boolean {
        return flatNumber.isNotBlank() && flatNumber.length <= 10
    }

    fun isValidTowerBlock(towerBlock: String): Boolean {
        return towerBlock.isNotBlank() && towerBlock.length <= 20
    }

    fun isValidAmount(amount: Double): Boolean {
        return amount > 0
    }

    fun isValidDescription(description: String, maxLength: Int = Constants.MAX_COMPLAINT_DESCRIPTION_LENGTH): Boolean {
        return description.isNotBlank() && description.length <= maxLength
    }

    fun isValidVehicleNumber(vehicleNumber: String): Boolean {
        if (vehicleNumber.isBlank()) return true // Vehicle number is optional
        val vehicleRegex = "^[A-Z]{2}[0-9]{1,2}[A-Z]{0,3}[0-9]{1,4}$"
        return vehicleNumber.uppercase().replace(" ", "").replace("-", "").matches(Regex(vehicleRegex))
    }
}

