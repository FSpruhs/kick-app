package com.spruhs.kick_app.message.core

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

@PackageInfo
@ApplicationModule(allowedDependencies = ["common", "group::api", "match::api"])
class ModuleMetaData
