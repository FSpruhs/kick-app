package com.spruhs.kick_app.view

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

@ApplicationModule(
    allowedDependencies = [
        "group :: group-api",
        "user :: user-api",
        "match :: match-api",
        "common :: common-es",
        "common :: common-types",
        "common :: common-exceptions",
        "common :: common-helper",
        "common :: common-configs",
    ],
)
class ModuleMetaData
