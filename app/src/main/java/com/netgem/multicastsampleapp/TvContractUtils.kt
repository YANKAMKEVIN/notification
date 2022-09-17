package com.netgem.multicastsampleapp

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.media.tv.TvContract
import android.net.Uri
import android.util.Log
import android.util.LongSparseArray
import androidx.tvprovider.media.tv.Channel
import java.util.*


class

TvContractUtils {

    companion object {
        private const val TAG = "TvContractUtils"

        @SuppressLint("RestrictedApi")
        fun buildChannelMap(
            resolver: ContentResolver,
            inputId: String?
        ): LongSparseArray<Channel> {
            val uri: Uri = TvContract.buildChannelsUriForInput(inputId)
            val channelMap: LongSparseArray<Channel> = LongSparseArray()
            var cursor: Cursor? = null

            Log.d(TAG, "Resolve query '$uri'");

            try {
                cursor = resolver.query(uri, Channel.PROJECTION, null, null, null)
                if (cursor == null) {
                    Log.d(TAG, "Cursor is null");
                    return channelMap
                }

                if (cursor.count === 0) {
                    Log.d(TAG, "cursor found no results");
                    return channelMap
                }

                while (cursor.moveToNext()) {
                    val nextChannel: Channel = Channel.fromCursor(cursor)
                    channelMap.put(nextChannel.getId(), nextChannel)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Content provider query: " + Arrays.toString(e.stackTrace))
                return channelMap
            } finally {
                cursor?.close()
            }
            return channelMap
        }
    }

}