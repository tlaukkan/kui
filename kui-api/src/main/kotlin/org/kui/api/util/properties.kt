/**
 * Copyright 2013 Tommi S.E. Laukkanen

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kui.util

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.HashMap
import java.util.Properties


/** The loaded properties.  */
private val PROPERTIES_MAP = HashMap<String, Properties?>()
/** The loaded extension properties.  */
private val EXTENDED_PROPERTIES_MAP = HashMap<String, Properties?>()
/** Map of property overrides  */
private val OVERRIDE_PROPERTIES_MAP = HashMap<String, Properties?>()
/** Map of environment variable property overrides.  */
private val ENVIRONMENT_PROPERTIES_MAP = mutableMapOf<String, MutableMap<String, String?>>()
/** The category redirection map.  */
private val categoryRedirection = HashMap<String, String>()

/**
 * Redirect extended property loading from source to target directory.
 * This enables loading for example site kit built in properties
 * from application specific property file.
 * @param sourceCategory the source category
 * @param targetCategory the target category
 */
fun setPropertyCategoryRedirection(sourceCategory: String, targetCategory: String) {
    categoryRedirection.put(sourceCategory, targetCategory)
}

/**
 * Sets explicit override value for a property.
 * @param category the category
 * @param propertyKey the property key
 * @param propertyValue the property value
 */
@Synchronized fun setProperty(category: String, propertyKey: String, propertyValue: String) {
    if (!OVERRIDE_PROPERTIES_MAP.containsKey(category)) {
        OVERRIDE_PROPERTIES_MAP.put(category, Properties())
    }
    OVERRIDE_PROPERTIES_MAP[category]!!.put(propertyKey, propertyValue)
}

/**
 * Gets property value String or null if no value is defined.
 * @param categoryKey Category defines the property file prefix.
 * @param propertyKey Property key defines the key in property file.
 * @return property value String or null.
 */
@Synchronized fun getProperty(categoryKey: String, propertyKey: String): String {

    val baseCategoryKey: String
    val extendedCategoryKey: String
    if (categoryRedirection.containsKey(categoryKey)) {
        baseCategoryKey = categoryRedirection[categoryKey]!!
        extendedCategoryKey = categoryRedirection[categoryKey] + "-ext"
    } else {
        baseCategoryKey = categoryKey
        extendedCategoryKey = categoryKey + "-ext"
    }

    if (!ENVIRONMENT_PROPERTIES_MAP.containsKey(baseCategoryKey)) {
        ENVIRONMENT_PROPERTIES_MAP[baseCategoryKey] = mutableMapOf<String, String?>()
    }

    if (!ENVIRONMENT_PROPERTIES_MAP[baseCategoryKey]!!.contains(propertyKey)) {
        val environmentVariableName = "${categoryKey}_$propertyKey".replace('.','_').replace('-','_').toUpperCase()
        val environmentVariableValue = System.getenv(environmentVariableName)
        ENVIRONMENT_PROPERTIES_MAP[baseCategoryKey]!![propertyKey]=environmentVariableValue
        if (environmentVariableValue != null) {
            println("Loaded environment variable: $environmentVariableName as property $baseCategoryKey:$propertyKey")
        }
    }

    if (!PROPERTIES_MAP.containsKey(baseCategoryKey)) {
        PROPERTIES_MAP.put(baseCategoryKey, getProperties(baseCategoryKey))
    }

    if (!EXTENDED_PROPERTIES_MAP.containsKey(extendedCategoryKey)) {
        EXTENDED_PROPERTIES_MAP.put(extendedCategoryKey, getProperties(extendedCategoryKey))
    }

    if (OVERRIDE_PROPERTIES_MAP.containsKey(baseCategoryKey)) {
        val valueString = OVERRIDE_PROPERTIES_MAP[baseCategoryKey]!![propertyKey]
        if (valueString != null) {
            return valueString as String
        }
    }

    if (ENVIRONMENT_PROPERTIES_MAP.containsKey(baseCategoryKey)) {
        val valueString = ENVIRONMENT_PROPERTIES_MAP[baseCategoryKey]!![propertyKey]
        if (valueString != null) {
            return valueString
        }
    }

    if (EXTENDED_PROPERTIES_MAP[extendedCategoryKey] != null) {
        val valueString = EXTENDED_PROPERTIES_MAP[extendedCategoryKey]!![propertyKey]
        if (valueString != null) {
            return valueString as String
        }
    }

    if (PROPERTIES_MAP[baseCategoryKey] != null) {
        val valueString = PROPERTIES_MAP[baseCategoryKey]!![propertyKey]
        if (valueString != null) {
            return valueString as String
        }
    }

    throw RuntimeException("Property not found: $baseCategoryKey / $propertyKey")
}

class Helper

/**
 * Loads properties with given category key.
 * @param categoryKey The category key.
 * @return Properties or null.
 */
@Synchronized private fun getProperties(categoryKey: String): Properties? {
    val propertiesFileName = categoryKey + ".properties"
    val properties = Properties()
    var inputStream: InputStream? = Helper::class.java.classLoader.getResourceAsStream(propertiesFileName)
    if (inputStream == null) {
        try {
            if (!File(propertiesFileName).exists()) {
                return null
            }
            inputStream = FileInputStream(propertiesFileName)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        if (inputStream == null) {
            return null
        }
    }
    try {
        properties.load(inputStream)
        inputStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }

    return properties
}
