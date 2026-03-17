package com.spruhs.kick_app.match

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

@ApplicationModule(
    allowedDependencies = [
        "group :: group-api",
        "match :: match-api",
        "common :: common-es",
        "common :: common-types",
        "common :: common-exceptions",
        "common :: common-helper",
    ],
)
class ModuleMetaData
