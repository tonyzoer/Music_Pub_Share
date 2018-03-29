package com.zoer.musicserver.Utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import com.fasterxml.jackson.core.JsonGenerationException
import com.fasterxml.jackson.databind.ObjectMapper
import com.zoer.musicserver.data.Song
import java.io.*


class SongsManager {
    var list: ArrayList<Song>? = ArrayList()
    private var i: Int = 1

    companion object {
        val TAG = "SongsManager"
    }

    fun initPlayList(pathes: ArrayList<String>): ArrayList<String> {
        var songPathes: ArrayList<String> = ArrayList()
        for (s in pathes) {
            var derictories: ArrayList<String> = ArrayList()
            val home = File(s)
            if (home.listFiles(FileFilter { file -> file.isDirectory }).size > 0) {
                for (file in home.listFiles(FileFilter { file -> file.isDirectory }))
                    derictories.add(file.path)
            }

            songPathes.addAll(initPlayList(derictories))
            var imgSrc = ""
            if (home.listFiles(FileFilter { file ->
                        file.extension.equals("jpg")
                                || file.extension.equals("jpeg")
                                || file.extension.equals("j")
                                || file.extension.equals("JPEG")
                                || file.extension.equals("JPG")
                    }).size > 0) {
                for (fileImage in home.listFiles({ file ->
                    file.extension.equals("jpg")
                            || file.extension.equals("jpeg")
                            || file.extension.equals("j")
                            || file.extension.equals("JPEG")
                            || file.extension.equals("JPG") })) {
                    imgSrc = fileImage.path
                    break
                    //Todo make it as List and swypeble imgs
                }
            }

            if (home.listFiles(FileFilter { file -> file.extension.equals("MP3") || file.extension.equals("mp3") }).size > 0) {
                val countInDirectory = home.listFiles(FileFilter { file -> file.extension.equals("MP3") || file.extension.equals("mp3") }).size
                var ii = 1
                for (file in home.listFiles(FileFilter { file -> file.extension.equals("MP3") || file.extension.equals("mp3") })) {
                    val filePath = file.path
                    songPathes.add(filePath)
                    val mmr = MediaMetadataRetriever()

                    mmr.setDataSource(filePath)
                    val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                    val genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                    val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val song = Song(i++,
                            artist ?: "UNKNOWN",
                            title ?: file.absolutePath.replace(home.path, ""),
                            album ?: "UNKNOWN",
                            genre ?: "UNKNOWN",
                            "file://$filePath",
                            "file://$imgSrc",
                            ii++,
                            countInDirectory,
                            duration.toInt()/1000
                    )
                    list?.add(song)


                }
            }
        }
        return songPathes
    }


    fun saveToJsonString(songs: ArrayList<Song>): String {
        var objMaper = ObjectMapper()
        var json: String = ""
        try {
            json = objMaper.writeValueAsString(songs)
        } catch (je: JsonGenerationException) {
            Log.e(TAG, "Json generetaion went wrong")
        }
        return json
    }

    fun saveToJsonFile(context: Context?): File {
        val fileName = "localMusic.json"
        var objMaper = ObjectMapper()
        if (context != null) {
            var file = File(context.filesDir, fileName)
            var json: String = ""
            try {
                json = objMaper.writeValueAsString(list)
                context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                    it.write(json.toByteArray())
                    it.close()
                }

            } catch (je: JsonGenerationException) {
                var errStr = "Json generetaion went wrong ${je.message}"
                Log.e(TAG, errStr)
            }

            var fis = context.openFileInput(fileName)

            var reader = BufferedReader(InputStreamReader(DataInputStream(fis)))

            var line: String?
            while (true) {
                line = reader.readLine()
                if (line == null) break
                Log.d(TAG, line)
            }

            return file
        }
        return File("EMPTY", "EMPTY")
    }
}