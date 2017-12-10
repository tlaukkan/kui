package org.kui.server.modules.vr.model

import org.kui.security.model.Record
import vr.network.model.DataVector3
import java.util.*

/**
 * Data class for VR scenes.
 */
data class Scene(
        override var key: String? = null,
        override var created: Date? = null,
        override var modified: Date? = null,
        var position: DataVector3 = DataVector3()) : Record