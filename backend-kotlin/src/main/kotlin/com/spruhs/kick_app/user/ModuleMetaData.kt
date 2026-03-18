package com.spruhs.kick_app.user

import org.springframework.modulith.ApplicationModule

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
