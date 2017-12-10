package org.kui.server.modules.vr.model

import org.kui.security.model.Record
import java.util.*

/**
 * Data class for VR scenes.
 */
data class SceneLink(
        /**
         * Key format is <source scene key>/<target scene key>
         */
        override var key: String? = null,
        override var created: Date? = null,
        override var modified: Date? = null) : Record