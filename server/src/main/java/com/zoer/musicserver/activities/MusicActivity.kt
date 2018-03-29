/*
 * This is the source code of DMAudioStreaming for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry(dibakar.ece@gmail.com), 2017.
 */
package com.zoer.musicserver.activities

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.nightonke.boommenu.BoomButtons.OnBMClickListener
import com.nightonke.boommenu.ButtonEnum
import com.nightonke.boommenu.Piece.PiecePlaceEnum

import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener

import java.util.ArrayList
import java.util.Collections
import java.util.LinkedList

import dm.audiostreamer.AudioStreamingManager
import dm.audiostreamer.CurrentSessionCallback
import dm.audiostreamer.Logger
import dm.audiostreamer.MediaMetaData

import com.zoer.musicserver.R
import com.zoer.musicserver.adapter.AdapterMusic
import com.zoer.musicserver.builders.BMBBuilderManager
import com.zoer.musicserver.network.MusicBrowser
import com.zoer.musicserver.network.MusicLoaderListener
import com.zoer.musicserver.slidinguppanel.SlidingUpPanelLayout
import com.zoer.musicserver.widgets.LineProgress
import com.zoer.musicserver.widgets.PlayPauseView
import com.zoer.musicserver.widgets.Slider
import kotlinx.android.synthetic.main.activity_music.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.include_slidepanelchildtwo_bottomview.*
import kotlinx.android.synthetic.main.include_slidepanelchildtwo_topviewtwo.*
import kotlinx.android.synthetic.main.include_slidingpanel_childtwo.*

class MusicActivity : AppCompatActivity(), CurrentSessionCallback, View.OnClickListener, Slider.OnValueChangedListener {
    private var context: Context? = null
    private var musicList: ListView? = null
    private var adapterMusic: AdapterMusic? = null

    private var isExpand = false

    private var options: DisplayImageOptions? = null
    private val imageLoader = ImageLoader.getInstance()
    private val animateFirstListener = AnimateFirstDisplayListener()

    //For  Implementation
    private var streamingManager: AudioStreamingManager? = null
    private var currentSong: MediaMetaData? = null
    private var listOfSongs: List<MediaMetaData> = ArrayList()

    private val notificationPendingIntent: PendingIntent
        get() {
            val intent = Intent(context, MusicActivity::class.java)
            intent.action = "openplayer"
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            return PendingIntent.getActivity(context, 0, intent, 0)
        }


    internal var nextSongBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            streamingManager!!.onSkipToNext()
        }
    }

    internal var previousSongBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            streamingManager!!.onSkipToPrevious()
        }
    }

    internal var pauseSongBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            streamingManager!!.onPause()
        }
    }

    internal var playSongBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            streamingManager!!.onPlay(streamingManager!!.currentAudio)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        this.context = this@MusicActivity
        configAudioStreamer()
        uiInitialization()
        loadMusicData()


    }

    override fun onBackPressed() {
        if (isExpand) {
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        } else {
            super.onBackPressed()
            overridePendingTransition(0, 0)
            finish()
        }
    }

    public override fun onResume() {
        super.onResume()
        registerBroadcastRecivers()
    }

    override fun onPause() {
        super.onPause()
        unregisterBroadcastRecivers()
    }


    public override fun onStart() {
        super.onStart()
        try {
            if (streamingManager != null) {
                streamingManager!!.subscribesCallBack(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    public override fun onStop() {
        try {
            if (streamingManager != null) {
                streamingManager!!.unSubscribeCallBack()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onStop()
    }

    override fun onDestroy() {
        try {
            if (streamingManager != null) {
                streamingManager!!.unSubscribeCallBack()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onDestroy()
    }

    override fun updatePlaybackState(state: Int) {
        Logger.e("updatePlaybackState: ", "" + state)
        when (state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                pgPlayPauseLayout!!.visibility = View.INVISIBLE
                btn_play!!.Play()
                if (currentSong != null) {
                    currentSong!!.playState = PlaybackStateCompat.STATE_PLAYING
                    notifyAdapter(currentSong!!)
                }
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                pgPlayPauseLayout!!.visibility = View.INVISIBLE
                btn_play!!.Pause()
                if (currentSong != null) {
                    currentSong!!.playState = PlaybackStateCompat.STATE_PAUSED
                    notifyAdapter(currentSong!!)
                }
            }
            PlaybackStateCompat.STATE_NONE -> {
                currentSong!!.playState = PlaybackStateCompat.STATE_NONE
                notifyAdapter(currentSong!!)
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                pgPlayPauseLayout!!.visibility = View.INVISIBLE
                btn_play!!.Pause()
                audio_progress_control!!.value = 0
                if (currentSong != null) {
                    currentSong!!.playState = PlaybackStateCompat.STATE_NONE
                    notifyAdapter(currentSong!!)
                }
            }
            PlaybackStateCompat.STATE_BUFFERING -> {
                pgPlayPauseLayout!!.visibility = View.VISIBLE
                if (currentSong != null) {
                    currentSong!!.playState = PlaybackStateCompat.STATE_NONE
                    notifyAdapter(currentSong!!)
                }
            }
        }
    }

    override fun playSongComplete() {
        val timeString = "00.00"
        slidepanel_time_total_bottom!!.text = timeString
        slidepanel_time_total!!.text = timeString
        slidepanel_time_progress_bottom!!.text = timeString
        slidepanel_time_progress!!.text = timeString
        lineProgress!!.setLineProgress(0)
        audio_progress_control!!.value = 0
    }

    override fun currentSeekBarPosition(progress: Int) {
        audio_progress_control!!.value = progress
        setPGTime(progress)
    }

    override fun playCurrent(indexP: Int, currentAudio: MediaMetaData) {
        showMediaInfo(currentAudio)
        notifyAdapter(currentAudio)
    }

    override fun playNext(indexP: Int, CurrentAudio: MediaMetaData) {
        showMediaInfo(CurrentAudio)
    }

    override fun playPrevious(indexP: Int, currentAudio: MediaMetaData) {
        showMediaInfo(currentAudio)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_forward -> streamingManager!!.onSkipToNext()
            R.id.btn_backward -> streamingManager!!.onSkipToPrevious()
            R.id.btn_play -> if (currentSong != null) {
                playPauseEvent(view)
            }
        }
    }

    override fun onValueChanged(value: Int) {
        streamingManager!!.onSeekTo(value.toLong())
        streamingManager!!.scheduleSeekBarUpdate()
    }

    private fun notifyAdapter(media: MediaMetaData) {
        adapterMusic!!.notifyPlayState(media)
    }

    private fun playPauseEvent(v: View) {
        if (streamingManager!!.isPlaying) {
            streamingManager!!.onPause()
            (v as PlayPauseView).Pause()
        } else {
            streamingManager!!.onPlay(currentSong)
            (v as PlayPauseView).Play()
        }
    }

    private fun playSong(media: MediaMetaData) {
        if (streamingManager != null) {
            streamingManager!!.onPlay(media)
            showMediaInfo(media)
        }
    }

    private fun showMediaInfo(media: MediaMetaData) {
        currentSong = media
        audio_progress_control!!.value = 0
        audio_progress_control!!.min = 0
        audio_progress_control!!.max = Integer.valueOf(media.mediaDuration)!! * 1000
        setPGTime(0)
        setMaxTime()
        loadSongDetails(media)
    }

    private fun configAudioStreamer() {
        streamingManager = AudioStreamingManager.getInstance(context)
        //Set PlayMultiple 'true' if want to playing sequentially one by one songs
        // and provide the list of songs else set it 'false'
        streamingManager!!.isPlayMultiple = true
        streamingManager!!.setMediaList(listOfSongs)
        //If you want to show the Player Notification then set ShowPlayerNotification as true
        //and provide the pending intent so that after click on notification it will redirect to an activity
        streamingManager!!.setShowPlayerNotification(true)
        streamingManager!!.setPendingIntentAct(notificationPendingIntent)
    }

    private fun uiInitialization() {

        setSupportActionBar(toolbar)
        var toolb=toolbar
        toolbar.title = getString(R.string.music)
        supportActionBar?.title=getString(R.string.music)
        toolbar.setTitleTextColor(Color.BLACK)

        btn_backward!!.setOnClickListener(this)
        btn_forward!!.setOnClickListener(this)
        btn_play!!.setOnClickListener(this)
        pgPlayPauseLayout!!.setOnClickListener(View.OnClickListener { return@OnClickListener })

        btn_play!!.Pause()

        changeButtonColor(btn_backward!!)
        changeButtonColor(btn_forward!!)



        slideBottomView!!.visibility = View.VISIBLE
        slideBottomView!!.setOnClickListener { sliding_layout!!.panelState = SlidingUpPanelLayout.PanelState.EXPANDED }

        audio_progress_control!!.max = 0
        audio_progress_control!!.onValueChangedListener = this

        sliding_layout!!.setPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                if (slideOffset == 0.0f) {
                    isExpand = false
                    slideBottomView!!.visibility = View.VISIBLE
                    //slideBottomView.getBackground().setAlpha(0);
                } else if (slideOffset > 0.0f && slideOffset < 1.0f) {
                    //slideBottomView.getBackground().setAlpha((int) slideOffset * 255);
                } else {
                    //slideBottomView.getBackground().setAlpha(100);
                    isExpand = true
                    slideBottomView!!.visibility = View.GONE
                }
            }

            override fun onPanelExpanded(panel: View) {
                isExpand = true
            }

            override fun onPanelCollapsed(panel: View) {
                isExpand = false
            }

            override fun onPanelAnchored(panel: View) {}

            override fun onPanelHidden(panel: View) {}
        })

        musicList = findViewById(R.id.musicList)
        adapterMusic = AdapterMusic(context, ArrayList())
        adapterMusic!!.setListItemListener { media -> playSong(media) }
        musicList!!.adapter = adapterMusic

        this.options = DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.bg_default_album_art)
                .showImageForEmptyUri(R.drawable.bg_default_album_art)
                .showImageOnFail(R.drawable.bg_default_album_art).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build()

        //Set up Bomb Menu
        menu_bmb.setButtonEnum(ButtonEnum.Ham)
        menu_bmb.piecePlaceEnum = PiecePlaceEnum.HAM_2
        menu_bmb.addBuilder(BMBBuilderManager.getServerSettingsHAMButtonBuilder().listener(OnBMClickListener {
            startActivity(Intent(this, ServerActivity::class.java))
        }))
        menu_bmb.addBuilder(BMBBuilderManager.getSettingsHAMButtonBuilder().listener(OnBMClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }))

    }

    private fun loadMusicData() {
        MusicBrowser.loadMusic(context?:baseContext, object : MusicLoaderListener {
            override fun onLoadSuccess(listMusic: List<MediaMetaData>) {
                listOfSongs = listMusic
                adapterMusic!!.refresh(listMusic)

                configAudioStreamer()
                checkAlreadyPlaying()
            }

            override fun onLoadFailed() {
                //TODO SHOW FAILED REASON
            }

            override fun onLoadError() {
                //TODO SHOW ERROR
            }
        })
    }

    private fun checkAlreadyPlaying() {
        if (streamingManager!!.isPlaying) {
            currentSong = streamingManager!!.currentAudio
            if (currentSong != null) {
                currentSong!!.playState = streamingManager!!.mLastPlaybackState
                showMediaInfo(currentSong!!)
                notifyAdapter(currentSong!!)
            }
        }
    }

    private fun loadSongDetails(metaData: MediaMetaData) {
        text_songName!!.text = metaData.mediaTitle
        text_songAlb!!.text = metaData.mediaArtist
        txt_bottom_SongName!!.text = metaData.mediaTitle
        txt_bottom_SongAlb!!.text = metaData.mediaArtist

        imageLoader.displayImage(metaData.mediaArt, image_songAlbumArtBlur, options, animateFirstListener)
        imageLoader.displayImage(metaData.mediaArt, image_songAlbumArt, options, animateFirstListener)
        imageLoader.displayImage(metaData.mediaArt, img_bottom_albArt, options, animateFirstListener)
    }

    private class AnimateFirstDisplayListener : SimpleImageLoadingListener() {

        override fun onLoadingStarted(imageUri: String?, view: View?) {
            progressEvent(view, false)
        }

        override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
            progressEvent(view, true)
        }

        override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
            if (loadedImage != null) {
                val imageView = view as ImageView?
                val firstDisplay = !displayedImages.contains(imageUri)
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 1000)
                    if (imageUri != null) {
                        displayedImages.add(imageUri)
                    }
                }
            }
            progressEvent(view, true)
        }

        companion object {

            internal val displayedImages: MutableList<String> = Collections.synchronizedList(LinkedList())
        }

    }

    private fun setPGTime(progress: Int) {
        try {
            var timeString = "00.00"
            var linePG = 0
            currentSong = streamingManager!!.currentAudio
            if (currentSong != null && progress.toLong() != java.lang.Long.parseLong(currentSong!!.mediaDuration)) {
                timeString = DateUtils.formatElapsedTime((progress / 1000).toLong())
                val audioDuration = java.lang.Long.parseLong(currentSong!!.mediaDuration)
                linePG = (progress / 1000 * 100 / audioDuration).toInt()
            }
            slidepanel_time_progress_bottom!!.text = timeString
            slidepanel_time_progress!!.text = timeString
            lineProgress!!.setLineProgress(linePG)
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }

    }

    private fun setMaxTime() {
        try {
            val timeString = DateUtils.formatElapsedTime(java.lang.Long.parseLong(currentSong!!.mediaDuration))
            slidepanel_time_total_bottom!!.text = timeString
            slidepanel_time_total!!.text = timeString
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }

    }

    private fun changeButtonColor(imageView: ImageView) {
        try {
            val color = Color.BLACK
            imageView.setColorFilter(color)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun registerBroadcastRecivers() {
        //NEXT song
        val nextSongfilter = IntentFilter()
        nextSongfilter.addAction(getString(R.string.playnext))
        registerReceiver(nextSongBroadcastReceiver, nextSongfilter)


        val prevSongfilter = IntentFilter()
        prevSongfilter.addAction(getString(R.string.playPrev))
        registerReceiver(previousSongBroadcastReceiver, prevSongfilter)

        val pauseSongfilter = IntentFilter()
        pauseSongfilter.addAction(getString(R.string.pause))
        registerReceiver(pauseSongBroadcastReceiver, pauseSongfilter)

    }

    private fun unregisterBroadcastRecivers() {
        try {
            unregisterReceiver(nextSongBroadcastReceiver)
            unregisterReceiver(previousSongBroadcastReceiver)
            unregisterReceiver(pauseSongBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            if (e.message!!.contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
                Log.w(TAG, "Tried to unregister the reciver when it's not registered")
            } else {
                throw e
            }
        }

    }

    companion object {

        private val TAG = MusicActivity::class.java.simpleName

        private fun progressEvent(v: View?, isShowing: Boolean) {
            try {
                val parent = v!!.parent as View
                val pg = parent.findViewById<ProgressBar>(R.id.pg)
                if (pg != null)
                    pg.visibility = if (isShowing) View.GONE else View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

}