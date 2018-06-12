/*
 * This is the source code of DMAudioStreaming for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry(dibakar.ece@gmail.com), 2017.
 */
package com.zoer.musicserver.network


import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.text.TextUtils
import android.util.Log

import com.google.gson.JsonSyntaxException

import org.apache.http.conn.ConnectTimeoutException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList

import javax.net.ssl.HttpsURLConnection

import dm.audiostreamer.MediaMetaData
import java.nio.charset.Charset

object MusicBrowser {

    private val music = "https://firebasestorage.googleapis.com/v0/b/dmaudiostreamer.appspot.com/o/music.json?alt=media&token=64ac05a8-2f23-4cef-b25c-b488519b0650"

    var RESPONSE_CODE_SUCCESS = "200"
    var RESPONSE_CODE_CONNECTION_TIMEOUT = "9001"
    var RESPONSE_CODE_SOCKET_TIMEOUT = "903"
    private val TAG ="Music Browser"


    // always check HTTP response code first
    // Get Response
    val dataResponse: Array<String>
        get() {
            val result = arrayOf("", "")
            try {
                val url = URL(music)
                val urlConnection = url.openConnection() as HttpsURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection.connectTimeout = 20000
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                if (Build.VERSION.SDK_INT > 13) {
                    urlConnection.setRequestProperty("Connection", "close")
                }

                urlConnection.useCaches = false
                urlConnection.doInput = true
                urlConnection.doOutput = true
                val responseCode = urlConnection.responseCode
                result[0] = responseCode.toString() + ""

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val `is` = urlConnection.inputStream
                    val rd = BufferedReader(InputStreamReader(`is`))
                    val response = StringBuffer()
                    var line = ""
                    while (true) {
                        line = rd.readLine()
                        if (line != null) {
                            response.append(line)
                            response.append('\r')
                        } else break
                    }
                    rd.close()

                    if (!TextUtils.isEmpty(response)) {
                        result[0] = RESPONSE_CODE_SUCCESS
                        result[1] = response.toString()
                    }
                }

            } catch (e: UnsupportedEncodingException) {
                result[0] = RESPONSE_CODE_CONNECTION_TIMEOUT
                e.printStackTrace()
            } catch (e: ConnectTimeoutException) {
                result[0] = RESPONSE_CODE_CONNECTION_TIMEOUT
                e.printStackTrace()
            } catch (e: IOException) {
                result[0] = RESPONSE_CODE_CONNECTION_TIMEOUT
                e.printStackTrace()
            } catch (e: Exception) {
                result[0] = RESPONSE_CODE_CONNECTION_TIMEOUT
                e.printStackTrace()
            }

            return result
        }

    fun loadMusic(context: Context, loaderListener: MusicLoaderListener?) {

        val loadTask = object : AsyncTask<Unit, Unit, Unit>() {
            internal var resp = arrayOf("", "")
            internal var listMusic: MutableList<MediaMetaData>? = ArrayList()

            override fun doInBackground(vararg units: Unit) {
                //resp = getDataResponse();
//                val response = loadJSONFromAsset(context)
//                listMusic = getMusicList(response, "music",lastId)
//                lastId= listMusic!!.last().mediaId.toInt()

                val savedJson = loadJSONFromInternalStorage(context)
                listMusic!!.addAll(getMusicList(savedJson, null))

            }

            override fun onPostExecute(unit: Unit) {
                super.onPostExecute(unit)

                if (loaderListener != null && listMusic != null && listMusic!!.size >= 1) {
                    loaderListener.onLoadSuccess(listMusic)
                } else {
                    loaderListener!!.onLoadFailed()
                }
            }
        }
        loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun loadJSONFromAsset(context: Context): String? {
        var json: String? = null
        try {
            val `is` = context.assets.open("music.json")
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            json = String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }

        return json
    }


    fun loadJSONFromInternalStorage(context: Context): String? {
        try {
            val fis = context.openFileInput("localMusic.json")
            return fis.bufferedReader().use(BufferedReader::readText)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }



    fun getMusicList(response: String?, name: String?): MutableList<MediaMetaData> {
        val listArticle = ArrayList<MediaMetaData>()
        var array: JSONArray? = null
        try {
            if (name != null)
                array = JSONObject(response).getJSONArray(name)
            else
                array = JSONArray(response)

            for (i in 0 until array!!.length()) {
                val infoData = MediaMetaData()
                val musicObj = array.getJSONObject(i)
                infoData.mediaId = musicObj.optString("id")
                infoData.mediaUrl = musicObj.optString("site") + musicObj.optString("source")
                infoData.mediaTitle = musicObj.optString("title")
                infoData.mediaArtist = musicObj.optString("artist")
                infoData.mediaAlbum = musicObj.optString("album")
                infoData.mediaComposer = musicObj.optString("")
                infoData.mediaDuration = musicObj.optString("duration")
                infoData.mediaArt = musicObj.optString("site") + musicObj.optString("image")
                listArticle.add(infoData)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
        } catch(e: NullPointerException){
            e.printStackTrace()
            Log.d(TAG, "Initial start of get Music List")
        }

        return listArticle
    }
}
