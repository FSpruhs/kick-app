package com.spruhs.kick_app.message.core

import org.springframework.modulith.ApplicationModule

@ApplicationModule(
    allowedDependencies = [
        "group :: group-api",
        "match :: match-api",
        "common :: common-es",
        "common :: common-types",
        "common :: common-exceptions",
        "common :: common-helper",
        "common :: common-configs",
    ],
)
class ModuleMetaData
