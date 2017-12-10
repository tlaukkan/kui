package org.kui.server.modules.vr

import org.kui.security.Safe
import org.kui.server.modules.vr.model.Scene
import org.kui.server.modules.vr.model.SceneLink
import vr.network.model.Node

object VrModule {

    fun initialize() {
        Safe.registerType(Scene::class.java)
        Safe.registerType(SceneLink::class.java)
        Safe.registerType(Node::class.java)
    }
}