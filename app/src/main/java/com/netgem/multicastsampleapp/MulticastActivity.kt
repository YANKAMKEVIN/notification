package com.netgem.multicastsampleapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.tv.*
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.tvprovider.media.tv.Channel
import androidx.tvprovider.media.tv.TvContractCompat
import com.tif.tvinput.aidl.ITVInputCommonAidl
import com.tif.tvinput.aidl.ITVInputCommonCallbackAidl
import java.lang.reflect.Method


/** Loads [MulticastTvView]. */
class MulticastActivity : FragmentActivity() {

    private val TAG = "MulticastActivity"
    private var mInputCommonAidl: ITVInputCommonAidl? = null
    private lateinit var mView: TvView

    private val mAudioTracks: MutableList<TvTrackInfo> = mutableListOf()
    private val mSubtitleTracks: MutableList<TvTrackInfo> = mutableListOf()
    private val arrayId = mutableListOf<String>()

    private var mSelectedAudioTrackId: String? = null
    private var mSelectedSubtitleTrackId: String? = null

    private val TVINPUTEVENTIPTVCHINFO = "IPTVCHInfo"
    private val inputId = "com.tif.tvinput/.rich.RichTvInputService"
    private var previousValues = -1

    private val NOTIF_COUNT_CONTENT_URI: Uri =
        Uri.parse("content://com.android.tv.notifications.NotificationContentProvider/notifications/count")
    private val COLUMN_COUNT: String = "count"

    private val mNotifsCountCursor: Cursor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multicast)
        mView = findViewById(R.id.multicast_view)
        mView.setCallback(mCallback)
    }

    private fun installedApps() {
        Log.d("List App", "Je suis dans Installed Ap")
        val list = packageManager.getInstalledPackages(0)
        for (i in list.indices) {
            val packageInfo = list[i]
            if (packageInfo!!.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                Log.d("AppList AppList$i", appName)
            }
        }
    }

    /*private fun getNotifsCount(): Int {
        Log.d("Nombre Notif", " Dans getNotifsCount")
        if (mNotifsCountCursor != null && mNotifsCountCursor.moveToFirst()) {
            mNotifsCountCursor.moveToFirst()
            val index: Int = mNotifsCountCursor.getColumnIndex("count")
            Log.d("Nombre Notif", "${mNotifsCountCursor.getInt(index)}")
        }
        return 0
    }*/
    private fun totalNotifications() {
        // enforce() will ensure the calling uid has the correct permission
        val mContext: Context = this@MulticastActivity
        mContext.enforceCallingOrSelfPermission(
            android.Manifest.permission.ACCESS_NOTIFICATION_POLICY,
            "NotificationManagerService.getActiveNotifications"
        )

        Log.d("Nombre Notif", "Je suis dans Installed Ap")
        val count: Int
        val c: Cursor? = contentResolver.query(NOTIF_COUNT_CONTENT_URI, null, null, null, null)
        // if (c != null && c.moveToFirst()) {
        //val index: Int = c.getColumnIndex(COLUMN_COUNT)
        //count = c. getInt(index)
        //Log.d("Nombre Notif", "$count")
        // }

        //c?.close()
    }

    private val mCallback = object : TvView.TvInputCallback() {
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

            mAudioTracks.clear()
            mSubtitleTracks.clear()
            if (tracks != null) {
                for (i in 0 until tracks.size) {
                    val track = tracks[i]

                    when (track.type) {
                        TvTrackInfo.TYPE_AUDIO -> {
                            mAudioTracks.add(tracks[i])
                            Log.i(TAG, "    Track AUDIO [$i] ${track.id} ${track.language}")
                        }
                        TvTrackInfo.TYPE_SUBTITLE -> {
                            mSubtitleTracks.add(tracks[i])
                            Log.i(TAG, "    Track SUBTITLE [$i] ${track.id} ${track.language}")
                        }
                        TvTrackInfo.TYPE_VIDEO -> {
                            Log.i(
                                TAG,
                                "    Track VIDEO [$i] ${track.id} ${track.videoWidth}x${track.videoHeight}"
                            )
                        }
                    }
                }
            }
        }

        private fun findTrackById(type: Int, trackId: String?): TvTrackInfo? {
            var trackList: MutableList<TvTrackInfo>? = null

            when (type) {
                TvTrackInfo.TYPE_AUDIO -> {
                    trackList = mAudioTracks
                }
                TvTrackInfo.TYPE_SUBTITLE -> {
                    trackList = mSubtitleTracks
                }
            }

            if (trackList == null || trackId == null)
                return null

            for (i in 0 until trackList.size) {
                val track = trackList[i]

                if (track.id == trackId) {
                    return track
                }
            }
            return null
        }

        override fun onTrackSelected(inputId: String?, type: Int, trackId: String?) {
            super.onTrackSelected(inputId, type, trackId)
            val track = findTrackById(type, trackId)

            Log.d(TAG, "onTrackSelected: {inputId=$inputId} type=$type trackId=$trackId")

            when (type) {
                TvTrackInfo.TYPE_AUDIO -> {
                    mSelectedAudioTrackId =
                        if (trackId == null || trackId == "disable") null else trackId

                    if (track != null) {
                        Log.i(TAG, "    Selected track AUDIO ${track.id} ${track.language}")
                    }
                }
                TvTrackInfo.TYPE_SUBTITLE -> {
                    mSelectedSubtitleTrackId =
                        if (trackId == null || trackId == "disable") null else trackId

                    if (track != null) {
                        Log.i(TAG, "    Selected track SUBTITLE ${track.id} ${track.language}")
                    }
                }
            }
        }
    }

    private fun injectChannel() {
        Log.i(TAG, "Inject channel")

        val assetMgr = this.assets
        val channelListFile = assetMgr.open("channel_list.json")
        val channelListBytes = ByteArray(channelListFile.available())
        channelListFile.read(channelListBytes)
        channelListFile.close()

        val channelListStr = String(channelListBytes)
        Log.i(TAG, channelListStr)

        setInfo(channelListStr)

    }

    private fun trackTypeToString(type: Int): String {
        when (type) {
            TvTrackInfo.TYPE_VIDEO -> {
                return "VIDEO"
            }
            TvTrackInfo.TYPE_AUDIO -> {
                return "AUDIO"
            }
            TvTrackInfo.TYPE_SUBTITLE -> {
                return "SUBTITLE"
            }
        }
        return "UNDEFINED"
    }

    private fun selectNextTrack(type: Int) {
        var trackList: MutableList<TvTrackInfo>? = null
        var trackId: String? = null

        when (type) {
            TvTrackInfo.TYPE_AUDIO -> {
                trackList = mAudioTracks
                trackId = mSelectedAudioTrackId
            }
            TvTrackInfo.TYPE_SUBTITLE -> {
                trackList = mSubtitleTracks
                trackId = mSelectedSubtitleTrackId
            }
        }

        if (trackList == null || trackList.size == 0)
            return

        if (trackId == null) {
            val track = trackList[0]
            mView.selectTrack(type, track.id)
            return
        }

        for (i in 0 until trackList.size) {
            val track = trackList[i]

            if (track.id == trackId) {
                if (i == trackList.size - 1) {
                    Log.d(TAG, "Unselect track ${trackTypeToString(type)}")
                    mView.selectTrack(type, null)
                    return
                }

                val nextTrack = trackList[i + 1]
                if (nextTrack.id == track.id)
                    return

                mView.selectTrack(type, nextTrack.id)
                return
            }
        }

        Log.e(
            TAG,
            "Cannot select next ${trackTypeToString(type)} track: track id '$trackId' not found"
        )
    }

    private fun selectPrevTrack(type: Int) {
        var trackList: MutableList<TvTrackInfo>? = null
        var trackId: String? = null

        when (type) {
            TvTrackInfo.TYPE_AUDIO -> {
                trackList = mAudioTracks
                trackId = mSelectedAudioTrackId
            }
            TvTrackInfo.TYPE_SUBTITLE -> {
                trackList = mSubtitleTracks
                trackId = mSelectedSubtitleTrackId
            }
        }

        if (trackList == null || trackList.size == 0)
            return

        if (trackId == null) {
            val track = trackList[0]
            mView.selectTrack(type, track.id)
            return
        }

        for (i in 0 until trackList.size) {
            val track = trackList[i]

            if (track.id == trackId) {
                if (i == 0) {
                    Log.d(TAG, "Unselect track ${trackTypeToString(type)}")
                    mView.selectTrack(type, null)
                    return
                }

                val prevTrack = trackList[i - 1]
                if (prevTrack.id == track.id)
                    return

                mView.selectTrack(type, prevTrack.id)
                return
            }
        }

        Log.e(
            TAG,
            "Cannot select previous ${trackTypeToString(type)} track: track id '$trackId' not found"
        )
    }

    private fun printChannels() {
        Log.i(TAG, "Print channels")
        val manager: TvInputManager = this.getSystemService(TV_INPUT_SERVICE) as TvInputManager
        val tvInputList = manager.tvInputList

        for (tvInputInfo in tvInputList) {
            Log.i(TAG, tvInputInfo.toString())
            Log.i(TAG, tvInputInfo.loadLabel(this) as String)

            val channelList = TvContractUtils.buildChannelMap(contentResolver, inputId)
            val channelListSize = channelList.size()
            var i = 0

            Log.i(TAG, "Found $channelListSize channel(s)")
            tuneTo(channelList.valueAt(0).id.toInt())
            while (i < channelListSize) {
                val channel = channelList.valueAt(i)

                Log.i("KEKE", channel.toString())
                i++

            }
        }
    }

    private fun tuneToFirstChannel() {
        Log.i(TAG, "Tune to the first channel added")
        val manager: TvInputManager = this.getSystemService(TV_INPUT_SERVICE) as TvInputManager
        val tvInputList = manager.tvInputList

        for (tvInputInfo in tvInputList) {
            val channelList = TvContractUtils.buildChannelMap(contentResolver, inputId)
            val channelListSize = channelList.size()
            var i = 0
            Log.i(TAG, "Found $channelListSize channel(s)")
            val channelId = channelList.valueAt(0).id.toInt()
            tuneTo(channelId)
            Log.i(TAG, "Tuning channel $channelId on TvInput '$inputId'")
            while (i < channelListSize) {
                val channel = channelList.valueAt(i)

                Log.i(TAG, channel.toString())
                i++
            }
        }
    }

    private fun tuneTo(channelId: Int) {
        val channelUri = Uri.parse("content://android.media.tv/channel/$channelId")
        mView.tune(inputId, channelUri)
    }

    private fun deleteChannel() {
        contentResolver.delete(TvContract.Channels.CONTENT_URI, null, null)
    }

    private fun getChannelInfos() {
        var projection = arrayOf(
            TvContract.Channels._ID,
            TvContract.Channels.COLUMN_INPUT_ID,
            TvContract.Channels.COLUMN_DISPLAY_NAME
        )
        val cursor = contentResolver.query(
            Uri.parse("content://android.media.tv/channel"),
            projection,
            null,
            null,
            null
        )
        while (cursor != null && cursor.moveToNext()) {
            val rowId = cursor.getLong(0)
            val inputId = cursor.getString(1)
            val displayName = cursor.getString(2)
            arrayId.add(rowId.toString())
            Log.i("KEVIN", "rowId : $rowId")
            Log.i("KEVIN", "inputId : $inputId")
            Log.i("KEVIN", "displayName : $displayName")
        }
        Log.i("KEVIN", " ${arrayId.size} channels have been found")
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.i(TAG, "AIDL service connected!")

            mInputCommonAidl = ITVInputCommonAidl.Stub.asInterface(service)
            try {
                mInputCommonAidl?.registerCallback(mInputCommonCallbackAidl)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }


            /////C'est Juste un test
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            totalNotifications()
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                // you got the permission
                totalNotifications()
                Log.i(TAG, "NOTIFICATION GRANTED")
            } else {
                Log.i(TAG, "NOTIFICATION ABORTED")
            }
            if (ContextCompat.checkSelfPermission(
                    this@MulticastActivity,
                    Manifest.permission.ACCESS_NOTIFICATION_POLICY
                ) !==
                PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@MulticastActivity,
                        Manifest.permission.ACCESS_NOTIFICATION_POLICY
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        this@MulticastActivity,
                        arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY), 1
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this@MulticastActivity,
                        arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY), 1
                    )
                }
            }


            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            //TODO IL FAUT CHECKER SI IL Y A VRAIMENT UN CHANNELiD AVANT DE SE TUNER
            injectChannel()
            //addChannels()
            tuneToFirstChannel()
            //printChannels()
            //getChannelInfos()
            //tuneChannel()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.i(TAG, "AIDL service disconnected!")

            try {
                mInputCommonAidl?.unRegisterCallback(mInputCommonCallbackAidl)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            mInputCommonAidl = null
        }
    }

    private val mInputCommonCallbackAidl = object : ITVInputCommonCallbackAidl.Stub() {
        override fun notifyResultInfo(strType: String?, strJson: String?) {
            Log.i(TAG, "TVInput event [$strType]: strJson='$strJson'")

            if (strJson == null)
                return

            //val event = JSONObject(strJson)
            //if (strType == TVINPUTEVENTIPTVCHINFO && event.getInt("IsSucc") > 0) {
            //}
        }
    }

    fun setInfo(strJson: String) {
        mInputCommonAidl?.setInfo("IPTVCHInfo", strJson)
    }

    override fun onStart() {
        super.onStart()
        Intent().setClassName("com.tif.tvinput", "com.tif.tvinput.aidl.TVInputCommonAidl")
            .also { intent ->
                bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
            }
    }

    override fun onStop() {
        super.onStop()
        unbindService(mServiceConnection)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.i(TAG, "onKeyDown keyCode=$keyCode")

        when (keyCode) {

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                selectNextTrack(TvTrackInfo.TYPE_AUDIO)
                Log.i(TAG, "On a cliqué sur KEYCODE_DPAD_RIGHT")
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                selectPrevTrack(TvTrackInfo.TYPE_AUDIO)
                Log.i(TAG, "On a cliqué sur KEYCODE_DPAD_LEFT")
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                selectNextTrack(TvTrackInfo.TYPE_SUBTITLE)
                Log.i(TAG, "On a cliqué sur KEYCODE_DPAD_UP")
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                selectPrevTrack(TvTrackInfo.TYPE_SUBTITLE)
                Log.i(TAG, "On a cliqué sur KEYCODE_DPAD_DOWN")
                return true
            }

            KeyEvent.KEYCODE_1 -> {

                Log.i(
                    TAG,
                    "On a cliqué sur le KeyCode 1 et la previous value c'est $previousValues"
                )
                changeChannel(16, keyCode)
            }
            KeyEvent.KEYCODE_2 -> {
                Log.i(TAG, "On a cliqué sur la chaine 2 et la previous value c'est $previousValues")
                changeChannel(16, keyCode)
            }
            KeyEvent.KEYCODE_3 -> {
                Log.i(TAG, "On a cliqué sur la chaine 3 et la previous value c'est $previousValues")
                changeChannel(16, keyCode)
            }
            KeyEvent.KEYCODE_4 -> {
                Log.i(TAG, "On a cliqué sur la chaine 4 et la previous value c'est $previousValues")
                changeChannel(16, keyCode)
            }
            KeyEvent.KEYCODE_5 -> {
                Log.i(TAG, "On a cliqué sur la chaine 5 et la previous value c'est $previousValues")
                changeChannel(16, keyCode)
            }
            KeyEvent.KEYCODE_6 -> {
                Log.i(TAG, "On a cliqué sur la chaine 6 et la previous value c'est $previousValues")
                changeChannel(16, keyCode)
            }
            KeyEvent.KEYCODE_7 -> {
                Log.i(TAG, "On a cliqué sur la chaine 7 et la previous value c'est $previousValues")
                changeChannel(16, keyCode)
            }
            KeyEvent.KEYCODE_8 -> {
                Log.i(TAG, "On a cliqué sur la chaine 8 et la previous value c'est $previousValues")
                changeChannel(17, keyCode)
            }

        }

        return super.onKeyDown(keyCode, event)
    }

    /**
     *Take the name of a system property like ro.product.serialno, ro.boot.chipid, ro.product.mac ...
     *
     *@return the value of the chosen system property
     * */
    @SuppressLint("PrivateApi")
    private fun getProperty(key: String?): Any? {
        try {
            val c = Class.forName("android.os.SystemProperties")
            val set: Method = c.getMethod("get", String::class.java)
            return set.invoke(c, key)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun changeChannel(channelId: Int, keyCode: Int): Boolean {
        if (previousValues == keyCode) {
            return true
        }
        mView.reset()
        tuneTo(channelId)
        previousValues = keyCode
        return true
    }

    private fun addChannels() {
        val internalProviderData = InternalProviderData()
        internalProviderData.setVideoUrl("igmp://239.1.1.1:3000")
        internalProviderData.setVideoType(2)
        val channel = Channel.Builder()
            .setDisplayName("Kevin6 Created Channel")
            .setDescription("Channel description")
            .setType(TvContractCompat.Channels.TYPE_PREVIEW) // Set more attributes...
            .setInternalProviderData("igmp://239.1.1.1:3000")
            .setSystemChannelKey("igmp://239.1.1.1:3000")
            .setNetworkAffiliation("igmp://239.1.1.1:3000")
            .setDescription("igmp://239.1.1.1:3000")
            .setInputId(inputId)
            .setGlobalContentId("igmp://239.1.1.1:3000")
            .setOriginalNetworkId(0)
            .setAppLinkIntent(Intent())
            .build()

        val channel2 = Channel.Builder()
            .setDisplayName("Kevin6 Created Channel")
            .setDescription("Channel description")
            .setType(TvContractCompat.Channels.TYPE_PREVIEW) // Set more attributes...
            .setInternalProviderData("igmp://239.1.1.1:3000")
            .setSystemChannelKey("igmp://239.1.1.1:3000")
            .setNetworkAffiliation("igmp://239.1.1.1:3000")
            .setDescription("igmp://239.1.1.1:3000")
            .setInputId(inputId)
            .setGlobalContentId("igmp://239.1.1.1:3000")
            .setOriginalNetworkId(0)
            .build()

        val channelUri = contentResolver.insert(
            Uri.parse("content://android.media.tv/channel"),
            channel.toContentValues()
        )
        val channelUr2 = contentResolver.insert(
            Uri.parse("content://android.media.tv/channel"),
            channel2.toContentValues()
        )
    }
}