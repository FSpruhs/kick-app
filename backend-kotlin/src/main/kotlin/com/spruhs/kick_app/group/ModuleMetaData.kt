package com.spruhs.kick_app.group

import org.springframework.modulith.ApplicationModule

@ApplicationModule(
    allowedDependencies = [
        "group :: group-api",
        "user :: user-api",
        "common :: common-es",
        "common :: common-types",
        "common :: common-exceptions",
        "common :: common-helper",
    ],
)
class ModuleMetaData
