package ru.ilyasshafigin.openrndr.editor

import java.net.URL

/**
 * Resolves resource named [name] relative to [clazz] as a [String] based URL.
 */
fun resourceUrl(name: String, clazz: Class<*> = Editor::class.java): String {
    var resource: URL? = clazz.getResource(name)
    return if (resource == null) {
        resource = clazz.classLoader.getResource(name)
        if (resource == null) {
            return name
        } else {
            resource.toExternalForm()
        }
    } else {
        resource.toExternalForm()
    }
}

/**
 * Resolves resource named [name] relative to [clazz] as a [String] based URL.
 */
fun Editor<*>.resourceUrl(name: String, clazz: Class<*> = this::class.java): String {
    return resourceUrl(name, clazz)
}
