package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.helper.JWTParser
import com.spruhs.kick_app.common.aop.OwnerOnly
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.UserNotAuthorizedException
import com.spruhs.kick_app.common.types.UserNotFoundException
import com.spruhs.kick_app.user.core.application.*
import com.spruhs.kick_app.user.core.domain.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/user")
class UserRestController(
    private val userCommandsPort: UserCommandsPort,
    private val jwtParser: JWTParser
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun registerUser(@RequestBody request: RegisterUserRequest): String = userCommandsPort
        .registerUser(request.toCommand()).aggregateId

    @PutMapping("/{userId}/nickName")
    @OwnerOnly
    suspend fun changeNickName(
        @PathVariable userId: String,
        @RequestParam nickName: String,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        userCommandsPort.changeNickName(ChangeUserNickNameCommand(UserId(userId), NickName(nickName)))
    }

    @PutMapping("/{userId}/user-image")
    @OwnerOnly
    suspend fun changeUserImage(
        @RequestParam file: MultipartFile,
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): String {
        return userCommandsPort.updateUserImage(UserId(userId), file).value
    }
}

@ControllerAdvice
class UserExceptionHandler {

    @ExceptionHandler
    fun handleUserWithEmailAlreadyExistsException(e: UserWithEmailAlreadyExistsException) =
        ResponseEntity(e.message, HttpStatus.BAD_REQUEST)

    @ExceptionHandler
    fun handleUserNotAuthorizedException(e: UserNotAuthorizedException) =
        ResponseEntity(e.message, HttpStatus.UNAUTHORIZED)

    @ExceptionHandler
    fun handleUserNotFoundException(e: UserNotFoundException) = ResponseEntity(e.message, HttpStatus.BAD_REQUEST)

    @ExceptionHandler
    fun handleCreateUserIdentityProviderException(e: CreateUserIdentityProviderException) =
        ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
}

data class RegisterUserRequest(
    val nickName: String,
    val email: String,
    val password: String?,
)

private fun RegisterUserRequest.toCommand() = RegisterUserCommand(
    nickName = NickName(this.nickName),
    email = Email(this.email),
    password = this.password?.let { Password.fromPlaintext(it) }
)
