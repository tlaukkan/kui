package org.kui.storage.jpa

import java.util.*
import javax.persistence.*

@Entity(name = "TimeValueEntity")
data class TimeValueEntity(
    @Id @GeneratedValue(generator = "uuid") var id: String? = null,
    @Column(nullable = false) var key: String = "",
    @Temporal(TemporalType.TIMESTAMP) @Column(nullable = false)  var time: Date? = null,
    @Temporal(TemporalType.TIMESTAMP) @Column(nullable = false) var received: Date? = null,
    @Column(nullable = false) var bytes: ByteArray = kotlin.ByteArray(0)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimeValueEntity

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}