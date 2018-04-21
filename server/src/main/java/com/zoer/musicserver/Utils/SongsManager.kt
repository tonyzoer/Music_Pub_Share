package com.zoer.musicserver.Utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import com.fasterxml.jackson.core.JsonGenerationException
import com.fasterxml.jackson.databind.ObjectMapper
import com.zoer.musicserver.data.Path
import com.zoer.musicserver.data.Song
import com.zoer.musicserver.helpers.DBHelper
import java.io.*


class SongsManager(context: Context) {
    var list: ArrayList<Song> = ArrayList()


    private var dbHelper:DBHelper= DBHelper(context)
    private val ctx=context
    private var i: Int = 1




    fun initPlayList(pathes:ArrayList<Path> = dbHelper.getMusicPathesFromDb()): ArrayList<String> {

        val songs: ArrayList<String> = ArrayList()
        for (path in pathes) {
            val directories: ArrayList<Path> = ArrayList()
            val home = File(path.path)
            if (home.listFiles({ file -> file.isDirectory }).isNotEmpty()) {
                for (file in home.listFiles({ file -> file.isDirectory }))
                    directories.add(Path(0,file.path,0))
            }

            songs.addAll(initPlayList(directories))
            var imgSrc = ""
            if (home.listFiles({ file ->
                        file.extension == "jpg"
                                || file.extension == "jpeg"
                                || file.extension == "j"
                                || file.extension == "JPEG"
                                || file.extension == "JPG"
                    }).isNotEmpty()) {
                for (fileImage in home.listFiles({ file ->
                    file.extension.equals("jpg")
                            || file.extension == "jpeg"
                            || file.extension == "j"
                            || file.extension == "JPEG"
                            || file.extension == "JPG"
                })) {
                    imgSrc = fileImage.path
                    break
                    //Todo make it as List and swypeble imgs
                }
            }

            if (home.listFiles( { file -> file.extension == "MP3" || file.extension == "mp3" }).isNotEmpty()) {
                val countInDirectory = home.listFiles({ file -> file.extension == "MP3" || file.extension == "mp3" }).size
                var ii = 1
                for (file in home.listFiles({ file -> file.extension == "MP3" || file.extension == "mp3" })) {
                    val filePath = file.path
                    songs.add(filePath)
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
                    list.add(song)


                }
            }
        }
        return songs
    }


    fun saveToJsonString(songs: ArrayList<Song>): String {
        val objMaper = ObjectMapper()
        var json: String = ""
        try {
            json = objMaper.writeValueAsString(songs)
        } catch (je: JsonGenerationException) {
            Log.e(TAG, "Json generetaion went wrong")
        }
        return json
    }

    fun saveToJsonFile(): File {
        val fileName = "localMusic.json"
        val objMaper = ObjectMapper()

        val file = File(ctx.filesDir, fileName)
        val json: String
        try {
                json = objMaper.writeValueAsString(list)
                ctx.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                    it.write(json.toByteArray())
                    it.close()
                }

            } catch (je: JsonGenerationException) {
                val errStr = "Json generetaion went wrong ${je.message}"
                Log.e(TAG, errStr)
            }

            val fis = ctx.openFileInput(fileName)

            val reader = BufferedReader(InputStreamReader(DataInputStream(fis)))

            var line: String?
            while (true) {
                line = reader.readLine()
                if (line == null) break
                Log.d(TAG, line)
            }

            return file

    }

    companion object {
        const val TAG = "SongsManager"
    }


}