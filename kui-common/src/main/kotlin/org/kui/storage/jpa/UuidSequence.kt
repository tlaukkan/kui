package org.kui.storage.jpa

import org.eclipse.persistence.config.SessionCustomizer
import org.eclipse.persistence.internal.databaseaccess.Accessor
import org.eclipse.persistence.internal.sessions.AbstractSession
import org.eclipse.persistence.sequencing.Sequence
import org.eclipse.persistence.sessions.Session

import java.util.UUID
import java.util.Vector

class UuidSequence : Sequence, SessionCustomizer {

    constructor(name: String) : super(name)

    @Throws(Exception::class)
    override fun customize(session: Session) {
        val sequence = UuidSequence("uuid")
        session.login.addSequence(sequence)
    }

    override fun getGeneratedValue(accessor: Accessor, writeSession: AbstractSession, seqName: String): Any {
        return UUID.randomUUID().toString()
    }

    override fun getGeneratedVector(accessor: Accessor, writeSession: AbstractSession, seqName: String, size: Int): Vector<*>? { return null }

    override fun onConnect() {}

    override fun onDisconnect() {}

    override fun shouldAcquireValueAfterInsert(): Boolean { return false }

    override fun shouldUseTransaction(): Boolean { return false }

    override fun shouldUsePreallocation(): Boolean { return false }

}
