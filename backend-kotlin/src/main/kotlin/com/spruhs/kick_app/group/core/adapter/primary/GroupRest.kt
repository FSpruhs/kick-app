package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.group.core.application.CreateGroupCommand
import com.spruhs.kick_app.group.core.application.GroupUseCases
import com.spruhs.kick_app.group.core.domain.Name
import com.spruhs.kick_app.group.core.domain.UserId
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.RequestBody

@RestController
@RequestMapping("/api/v1/group")
class GroupRest(val groupUseCases: GroupUseCases, val jwtParser: JWTParser) {

    @PostMapping
    fun createGroup(@AuthenticationPrincipal jwt: Jwt, @RequestBody request: CreateGroupRequest) {
        val userId = jwtParser.getUserId(jwt)
        groupUseCases.create(CreateGroupCommand(UserId(userId), Name(request.name)))
    }
}

data class CreateGroupRequest(
    val name: String,
)