package org.kui.security.model

data class Host (
         var key: String? = null,
         var created: Long? = null,
         var modified: Long? = null,
         var environment: String? = null,
         var environmentType: String? = null,
         var hostType: String? = null,
         var owner: String? = null)