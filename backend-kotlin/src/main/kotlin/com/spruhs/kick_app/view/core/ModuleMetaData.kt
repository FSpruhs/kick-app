package com.spruhs.kick_app.view.core
import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo
@PackageInfo
@ApplicationModule(allowedDependencies = ["common", "view::api", "user::api", "match::api", "group::api"])
class ModuleMetaData
