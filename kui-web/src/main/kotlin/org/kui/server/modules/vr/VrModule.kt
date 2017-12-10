package org.kui.server.modules.vr

import org.kui.security.GROUP_ADMIN
import org.kui.security.GROUP_USER
import org.kui.security.Safe
import org.kui.server.modules.vr.model.Scene
import org.kui.server.modules.vr.model.SceneLink
import vr.network.model.Node

object VrModule {

    fun initialize() {
        Safe.registerType(Scene::class.java)
        Safe.permitAllOperations(Scene::class.java, listOf(GROUP_ADMIN))
        Safe.registerType(SceneLink::class.java)
        Safe.permitAllOperations(SceneLink::class.java, listOf(GROUP_ADMIN))
        Safe.registerType(Node::class.java)
        Safe.permitAllOperations(Node::class.java, listOf(GROUP_USER))
    }
}