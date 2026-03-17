package com.spruhs.kick_app.match.core
import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo
@PackageInfo
@ApplicationModule(allowedDependencies = ["common", "match::api", "view::api"])
class ModuleMetaData
