package com.mgbheights.shared.domain.usecase.visitor

import com.mgbheights.shared.domain.model.Visitor
import com.mgbheights.shared.domain.repository.VisitorRepository
import com.mgbheights.shared.util.Resource
import com.mgbheights.shared.util.Validators

class AddVisitorUseCase(private val visitorRepository: VisitorRepository) {
    suspend operator fun invoke(visitor: Visitor): Resource<Visitor> {
        if (visitor.name.isBlank()) return Resource.error("Visitor name is required")
        if (!Validators.isValidName(visitor.name)) return Resource.error("Invalid visitor name")
        if (visitor.phoneNumber.isNotBlank() && !Validators.isValidPhoneNumber(visitor.phoneNumber)) {
            return Resource.error("Invalid phone number")
        }
        if (visitor.flatId.isBlank()) return Resource.error("Destination flat is required")
        if (visitor.purpose.isBlank()) return Resource.error("Visit purpose is required")
        if (visitor.vehicleNumber.isNotBlank() && !Validators.isValidVehicleNumber(visitor.vehicleNumber)) {
            return Resource.error("Invalid vehicle number format")
        }
        return visitorRepository.createVisitor(visitor)
    }
}

