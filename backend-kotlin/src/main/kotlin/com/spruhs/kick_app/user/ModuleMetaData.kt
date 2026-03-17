package com.spruhs.kick_app.user

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

@ApplicationModule(
    allowedDependencies = [
        "user :: user-api",
        "common :: common-es",
        "common :: common-types",
        "common :: common-exceptions",
        "common :: common-helper",
        "common :: common-configs",
    ],
)
class ModuleMetaData
