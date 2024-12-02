package uz.thirdTask.payment

import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
//@RequestMapping("course")
class CourseController(
    private val paymentService: PaymentService
) {

    @PostMapping
    fun create(@Valid @RequestBody request: PaymentCreateRequest) = paymentService.create(request)

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): PaymentResponse = paymentService.getById(id)

    @GetMapping
    fun getAll() = paymentService.getAll()

}




