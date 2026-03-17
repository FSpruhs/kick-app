package com.spruhs.kick_app.group.api

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.NamedInterface
import org.springframework.modulith.PackageInfo

@PackageInfo
@NamedInterface
@ApplicationModule(type = ApplicationModule.Type.OPEN, allowedDependencies = ["group.core", "common"])
class ModuleMetaData
