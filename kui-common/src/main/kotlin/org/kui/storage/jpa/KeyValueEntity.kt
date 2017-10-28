package org.kui.storage.jpa

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "KeyValueEntity")
data class KeyValueEntity(
        @Id var id: String = "",
        @Column(nullable = false) var key: String = "",
        @Column(name="type", nullable = false) var type: String = "",
        @Column(nullable = false) var bytes: ByteArray = kotlin.ByteArray(0)) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyValueEntity

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}