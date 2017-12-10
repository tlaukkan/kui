package vr.network.model

import org.kui.security.model.Record
import java.util.*

/**
 * Data class for 3D nodes in VR scene.
 */
data class Node(
        override var key: String? = null,
        override var created: Date? = null,
        override var modified: Date? = null,
        var scene: String = "",
        var position: DataVector3 = DataVector3(),
        var orientation: DataQuaternion = DataQuaternion(),
        var scale: DataVector3 = DataVector3(1.0, 1.0, 1.0),
        var volatile: Boolean = false,
        var removed: Boolean = false,
        var opacity: Double = 1.0,
        var modelUrl: String = "") : Record
