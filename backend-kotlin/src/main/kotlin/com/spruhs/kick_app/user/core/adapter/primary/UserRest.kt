package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.common.UserNotFoundException
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

    @PutMapping("/{id}/nickName")
    suspend fun changeNickName(
        @PathVariable id: String,
        @RequestParam nickName: String,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        require(id == jwtParser.getUserId(jwt).value) { throw UserNotAuthorizedException(UserId(id)) }
        userCommandsPort.changeNickName(ChangeUserNickNameCommand(UserId(id), NickName(nickName)))
    }

    @PutMapping("/{id}/user-image")
    suspend fun changeUserImage(
        @RequestParam file: MultipartFile,
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt
    ): String {
        require(id == jwtParser.getUserId(jwt).value) { throw UserNotAuthorizedException(UserId(id)) }
        return userCommandsPort.updateUserImage(jwtParser.getUserId(jwt), file).value
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
