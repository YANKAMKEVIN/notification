package com.netgem.multicastsampleapp

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class InternalProviderData {
    companion object {
        private val TAG = "InternalProviderData"
        private val DEBUG = true

        private val KEY_VIDEO_TYPE = "type"
        private val KEY_VIDEO_URL = "url"
        private val KEY_REPEATABLE = "repeatable"
        private val KEY_CUSTOM_DATA = "custom"
        private val KEY_ADVERTISEMENTS = "advertisements"
        private val KEY_ADVERTISEMENT_START = "start"
        private val KEY_ADVERTISEMENT_STOP = "stop"
        private val KEY_ADVERTISEMENT_TYPE = "type"
        private val KEY_ADVERTISEMENT_REQUEST_URL = "requestUrl"
        private val KEY_RECORDING_START_TIME = "recordingStartTime"
    }

    private var mJsonObject: JSONObject? = null

    /** Creates a new empty object  */
    constructor() {
        mJsonObject = JSONObject()
    }

    /**
     * Creates a new object and attempts to populate from the provided String
     *
     * @param data Correctly formatted InternalProviderData
     * @throws ParseException If data is not formatted correctly
     */
    @Throws(ParseException::class)
    constructor(data: String) {
        mJsonObject = try {
            JSONObject(data)
        } catch (e: JSONException) {
            throw ParseException(e.message)
        }
    }

    /**
     * Creates a new object and attempts to populate by obtaining the String representation of the
     * provided byte array
     *
     * @param bytes Byte array corresponding to a correctly formatted String representation of
     * InternalProviderData
     * @throws ParseException If data is not formatted correctly
     */
    @Throws(ParseException::class)
    constructor(bytes: ByteArray) {
        mJsonObject = try {
            JSONObject(String(bytes))
        } catch (e: JSONException) {
            throw ParseException(e.message)
        }
    }

    private fun jsonHash(jsonObject: JSONObject?): Int {
        var hashSum = 0
        val keys = jsonObject!!.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            try {
                hashSum += if (jsonObject[key] is JSONObject) {
                    // This is a branch, get hash of this object recursively
                    val branch = jsonObject.getJSONObject(key)
                    jsonHash(branch)
                } else {
                    // If this key does not link to a JSONObject, get hash of leaf
                    key.hashCode() + jsonObject[key].hashCode()
                }
            } catch (ignored: JSONException) {
            }
        }
        return hashSum
    }

    override fun hashCode(): Int {
        // Recursively get the hashcode from all internal JSON keys and values
        return jsonHash(mJsonObject)
    }

    private fun jsonEquals(json1: JSONObject?, json2: JSONObject?): Boolean {
        val keys = json1!!.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            try {
                if (json1[key] is JSONObject) {
                    // This is a branch, check equality of this object recursively
                    val thisBranch = json1.getJSONObject(key)
                    val otherBranch = json2!!.getJSONObject(key)
                    return jsonEquals(thisBranch, otherBranch)
                } else {
                    // If this key does not link to a JSONObject, check equality of leaf
                    if (json1[key] != json2!![key]) {
                        // The VALUE of the KEY does not match
                        return false
                    }
                }
            } catch (e: JSONException) {
                return false
            }
        }
        // Confirm that no key has been missed in the check
        return json1.length() == json2!!.length()
    }

    /**
     * Tests that the value of each key is equal. Order does not matter.
     *
     * @param obj The object you are comparing to.
     * @return Whether the value of each key between both objects is equal.
     */
    override fun equals(obj: Any?): Boolean {
        if (obj == null || obj !is InternalProviderData) {
            return false
        }
        val otherJsonObject = obj.mJsonObject
        return jsonEquals(mJsonObject, otherJsonObject)
    }

    override fun toString(): String {
        return mJsonObject.toString()
    }

    /**
     * Gets the video type of the program.
     *
     * @return The video type of the program, -1 if no value has been given.
     */
    fun getVideoType(): Int {
        if (mJsonObject!!.has(KEY_VIDEO_TYPE)) {
            try {
                return mJsonObject!!.getInt(KEY_VIDEO_TYPE)
            } catch (ignored: JSONException) {
            }
        }
        return -1
    }

    /**
     * Sets the video type of the program.
     *
     * @param videoType The video source type. Could be [TvContractUtils.SOURCE_TYPE_HLS],
     * [TvContractUtils.SOURCE_TYPE_HTTP_PROGRESSIVE], or [     ][TvContractUtils.SOURCE_TYPE_MPEG_DASH].
     */
    fun setVideoType(videoType: Int) {
        try {
            mJsonObject!!.put(KEY_VIDEO_TYPE, videoType)
        } catch (ignored: JSONException) {
        }
    }

    /**
     * Gets the video url of the program if valid.
     *
     * @return The video url of the program if valid, null if no value has been given.
     */
    fun getVideoUrl(): String? {
        if (mJsonObject!!.has(KEY_VIDEO_URL)) {
            try {
                return mJsonObject!!.getString(KEY_VIDEO_URL)
            } catch (ignored: JSONException) {
            }
        }
        return null
    }

    /**
     * Gets recording start time of program for recorded program. For a non-recorded program, this
     * value will not be set.
     *
     * @return Recording start of program in UTC milliseconds, 0 if no value is given.
     */
    fun getRecordedProgramStartTime(): Long {
        try {
            return mJsonObject!!.getLong(KEY_RECORDING_START_TIME)
        } catch (ignored: JSONException) {
        }
        return 0
    }

    /**
     * Sets the video url of the program.
     *
     * @param videoUrl A valid url pointing to the video to be played.
     */
    fun setVideoUrl(videoUrl: String?) {
        try {
            mJsonObject!!.put(KEY_VIDEO_URL, videoUrl)
        } catch (ignored: JSONException) {
        }
    }

    /**
     * Checks whether the programs on this channel should be repeated periodically in order.
     *
     * @return Whether to repeat programs. Returns false if no value has been set.
     */
    fun isRepeatable(): Boolean {
        if (mJsonObject!!.has(KEY_REPEATABLE)) {
            try {
                return mJsonObject!!.getBoolean(KEY_REPEATABLE)
            } catch (ignored: JSONException) {
            }
        }
        return false
    }

    /**
     * Sets whether programs assigned to this channel should be repeated periodically. This field is
     * relevant to channels.
     *
     * @param repeatable Whether to repeat programs.
     */
    fun setRepeatable(repeatable: Boolean) {
        try {
            mJsonObject!!.put(KEY_REPEATABLE, repeatable)
        } catch (ignored: JSONException) {
        }
    }

    /**
     * Sets the recording program start time for a recorded program.
     *
     * @param startTime Recording start time in UTC milliseconds of recorded program.
     */
    fun setRecordingStartTime(startTime: Long) {
        try {
            mJsonObject!!.put(KEY_RECORDING_START_TIME, startTime)
        } catch (ignored: JSONException) {
        }
    }

    /**
     * Adds some custom data to the InternalProviderData.
     *
     * @param key The key for this data
     * @param value The value this data should take
     * @return This InternalProviderData object to allow for chaining of calls
     * @throws ParseException If there is a problem adding custom data
     */
    @Throws(ParseException::class)
    fun put(key: String?, value: Any): InternalProviderData? {
        try {
            if (!mJsonObject!!.has(KEY_CUSTOM_DATA)) {
                mJsonObject!!.put(KEY_CUSTOM_DATA, JSONObject())
            }
            mJsonObject!!.getJSONObject(KEY_CUSTOM_DATA).put(key, value.toString())
        } catch (e: JSONException) {
            throw ParseException(e.message)
        }
        return this
    }

    /**
     * Gets some previously added custom data stored in InternalProviderData.
     *
     * @param key The key assigned to this data
     * @return The value of this key if it has been defined. Returns null if the key is not found.
     * @throws ParseException If there is a problem getting custom data
     */
    @Throws(ParseException::class)
    operator fun get(key: String?): Any? {
        return if (!mJsonObject!!.has(KEY_CUSTOM_DATA)) {
            null
        } else try {
            mJsonObject!!.getJSONObject(KEY_CUSTOM_DATA).opt(key)
        } catch (e: JSONException) {
            throw ParseException(e.message)
        }
    }

    /**
     * Checks whether a custom key is found in InternalProviderData.
     *
     * @param key The key assigned to this data
     * @return Whether this key is found.
     * @throws ParseException If there is a problem checking custom data
     */
    @Throws(ParseException::class)
    fun has(key: String?): Boolean {
        return if (!mJsonObject!!.has(KEY_CUSTOM_DATA)) {
            false
        } else try {
            mJsonObject!!.getJSONObject(KEY_CUSTOM_DATA).has(key)
        } catch (e: JSONException) {
            throw ParseException(e.message)
        }
    }

    /**
     * This exception is thrown when an error occurs in getting or setting data for the
     * InternalProviderData.
     */
    class ParseException(s: String?) : JSONException(s)
}