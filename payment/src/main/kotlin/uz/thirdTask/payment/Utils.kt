package uz.thirdTask.payment

import org.springframework.security.core.context.SecurityContextHolder

fun userId() = SecurityContextHolder.getContext().getUserId()!!
fun getRole() = SecurityContextHolder.getContext().getRole()!!
fun currentUserName() = SecurityContextHolder.getContext().authentication.principal as String
fun currentUserAuthorities() = SecurityContextHolder.getContext().authentication.authorities.map { it.authority }
