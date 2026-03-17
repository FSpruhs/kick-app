package com.spruhs.kick_app.user.core
import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

@PackageInfo
@ApplicationModule(allowedDependencies = ["common", "user::api", "view::api"])
class ModuleMetaData
