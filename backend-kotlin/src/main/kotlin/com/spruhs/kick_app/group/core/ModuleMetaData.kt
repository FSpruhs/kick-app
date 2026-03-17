package com.spruhs.kick_app.group.core
import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

@PackageInfo
@ApplicationModule(allowedDependencies = ["common", "group::api", "view::api"])
class ModuleMetaData
