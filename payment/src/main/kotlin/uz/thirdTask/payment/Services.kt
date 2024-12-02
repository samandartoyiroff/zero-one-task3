package uz.thirdTask.payment

import org.springframework.stereotype.Service

interface PaymentService {

    fun create(request: PaymentCreateRequest)

    fun getAll(): List<PaymentResponse>

    fun getById(id: Long): PaymentResponse

    fun getAllByUserId(userId: Long) : List<PaymentResponse>

}

@Service
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository,

) : PaymentService {

    override fun create(request: PaymentCreateRequest) {
            request.run {
                paymentRepository.save(this.toEntity())
            }
    }

    override fun getAll(): List<PaymentResponse> {
        return paymentRepository.findAllNotDeleted().map {
            PaymentResponse.toResponse(it)
        }
    }

    override fun getById(id: Long): PaymentResponse {

        return paymentRepository.findByIdAndDeletedFalse(id)?.let {
            PaymentResponse.toResponse(it) }?: throw PaymentNotFoundException()

    }

    override fun getAllByUserId(userId: Long): List<PaymentResponse> {
       return paymentRepository.findAllByUserIdAndDeletedFalse(userId).map {
            PaymentResponse.toResponse(it)
        }
    }
}

