package com.yr.restfulapidemo.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * This data class defines a Mars photo which includes an ID, and the image URL.
 * The property names of this data class are used by Moshi to match the names of values in JSON.
 */
data class TodoPostItem(
    val name: String,
    val task: String,
    val status: Int,
    val updateTime: String
) : Serializable
