package com.netgem.multicastsampleapp

import android.content.Context
import android.media.tv.TvContentRating
import android.media.tv.TvInputManager
import android.media.tv.TvTrackInfo
import android.media.tv.TvView
import android.net.Uri
import android.os.RemoteException
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.tvprovider.media.tv.Channel
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName


class MulticastTvView : TvView {

    private val TAG = "MulticastTvView"
    private lateinit var mActivity: MulticastActivity;

    data class ChannelData (
        @SerializedName("channelcode") val channelCode: Int,
        @SerializedName("channelurl") val channelUrl: String,
        @SerializedName("channelname") val channelName: String,
        @SerializedName("timeshifturl") val timeshiftUrl: String,
        @SerializedName("timeshiftenable") val timeshiftEnable: Boolean
    )

    data class ChannelsData (
        @SerializedName("channels") val channels: List<ChannelData>? = null
    ) {
    }

    constructor(context: MulticastActivity) : super(context) {
        mActivity = context
        setCallback(mCallback)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setCallback(mCallback)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow()")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(TAG, "onDetachedFromWindow()")
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        Log.d(TAG, "onVisibilityChanged(visibility=$visibility)")
    }

    private val mCallback = object : TvInputCallback() {
        override fun onConnectionFailed(inputId: String?) {
            super.onConnectionFailed(inputId)
            Log.w(TAG, "Failed to bind an input")
        }

        override fun onDisconnected(inputId: String?) {
            super.onDisconnected(inputId)
            Log.w(TAG, "Session is released by crash")
        }

        override fun onChannelRetuned(inputId: String, channelUri: Uri) {
            super.onChannelRetuned(inputId, channelUri)
            Log.d(TAG, "onChannelRetuned(inputId=$inputId, channelUri=$channelUri)")
        }

        override fun onVideoSizeChanged(inputId: String?, width: Int, height: Int) {
            super.onVideoSizeChanged(inputId, width, height)
            Log.d(TAG, "onVideoSizeChanged(inputId=$inputId, width=$width height=$height)")
        }

        override fun onVideoAvailable(inputId: String) {
            super.onVideoAvailable(inputId)
            Log.d(TAG, "onVideoAvailable: {inputId=$inputId}")
        }

        override fun onVideoUnavailable(inputId: String?, reason: Int) {
            super.onVideoUnavailable(inputId, reason)
            Log.d(TAG, "onVideoUnavailable: {inputId=$inputId} reason=$reason")
        }

        override fun onContentAllowed(inputId: String?) {
            super.onContentAllowed(inputId)
            Log.d(TAG, "onContentAllowed: {inputId=$inputId}")
        }

        override fun onContentBlocked(inputId: String?, rating: TvContentRating) {
            super.onContentBlocked(inputId, rating)
            Log.d(TAG, "onContentBlocked: {inputId=$inputId}")
        }

        override fun onTracksChanged(inputId: String?, tracks: MutableList<TvTrackInfo>?) {
            super.onTracksChanged(inputId, tracks)
            Log.d(TAG, "onTracksChanged: {inputId=$inputId}")
        }

        override fun onTrackSelected(inputId: String?, type: Int, trackId: String?) {
            super.onTrackSelected(inputId, type, trackId)
            Log.d(TAG, "onTrackSelected: {inputId=$inputId}")
        }
    }

    private fun setInputInfo(channels: ChannelsData) {
        val strChannelList = Gson().toJson(channels)
        val tmpBuffer = StringBuffer()
        tmpBuffer.append("{\"channels\":").append(strChannelList).append("}")
        val strChannelInfo = tmpBuffer.toString()
        try {
            mActivity.setInfo(strChannelInfo)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun tuneToChannel(channel: Channel) {
        val inputId = channel.getInputId()
        if (inputId != null) {
            tune(inputId, Uri.parse("content://android.media.tv/channel/" + channel.getId()))
        }
    }

    fun setSubtitle(id: String) {
        selectTrack(TvTrackInfo.TYPE_SUBTITLE, id);
    }

    fun setAudio(id: String) {
        selectTrack(TvTrackInfo.TYPE_AUDIO, id);
    }
}