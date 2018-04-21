package com.zoer.musicserver.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.DialogFragment
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.nightonke.boommenu.BoomMenuButton
import com.nightonke.boommenu.ButtonEnum
import com.nightonke.boommenu.Piece.PiecePlaceEnum
import com.zoer.musicserver.helpers.DBHelper
import com.zoer.musicserver.R
import com.zoer.musicserver.Utils.SongsManager
import com.zoer.musicserver.builders.BMBBuilderManager
import com.zoer.musicserver.data.Path
import com.zoer.musicserver.dialogs.DeletePathDialog
import com.zoer.musicserver.tasks.SaveMusicDataToJsonFileTask
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.content_settings.*
import net.rdrei.android.dirchooser.DirectoryChooserActivity
import net.rdrei.android.dirchooser.DirectoryChooserConfig
import net.rdrei.android.dirchooser.DirectoryChooserFragment
import java.io.File


class SettingsActivity : AppCompatActivity(), DirectoryChooserFragment.OnFragmentInteractionListener, DeletePathDialog.NoticeDialogListener {

    private val CHOOSE_DIRECTORY_REQUEST_CODE: Int = 4007
    var dbHelper: DBHelper? = null
    private var mDialog: DirectoryChooserFragment? = null
    private var pathes: ArrayList<Path> = ArrayList()
    private var delete_id: Int? = null

    companion object {
        val TAG = "SettingsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        layoutInflater.inflate(R.layout.content_settings, null)
        UIinit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CHOOSE_DIRECTORY_REQUEST_CODE) {
            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                Toast.makeText(this, data?.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR)
                        ?: "Somthing went wrong", Toast.LENGTH_SHORT).show()
            } else {
                // Nothing selected
            }
        }

    }


    fun UIinit() {
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.settings)
        toolbar.setTitleTextColor(Color.BLACK)

        dbHelper = DBHelper(this)
        //init BombMenu
        val bmb = toolbar.findViewById<BoomMenuButton>(R.id.menu_bmb)
        bmb.buttonEnum = ButtonEnum.Ham
        bmb.piecePlaceEnum = PiecePlaceEnum.HAM_2
        bmb.addBuilder(BMBBuilderManager.getMusicHAMButtonBuilder().listener({ startActivity(Intent(this, MusicActivity::class.java)) }))
        bmb.addBuilder(BMBBuilderManager.getServerSettingsHAMButtonBuilder().listener({ startActivity(Intent(this, ServerActivity::class.java)) }))

        //init Choose Dialog fragment
        val config = DirectoryChooserConfig.builder()
                .newDirectoryName("DialogSample")
                .initialDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath())
                .allowNewDirectoryNameModification(true)
                .allowReadOnlyDirectory(false)
                .build()
        mDialog = DirectoryChooserFragment.newInstance(config);

        //init choose folder button
        chooseFolder.setOnClickListener({
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(this)) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 2909)
                } else {
                    mDialog?.show(fragmentManager, null)
                }
            } else {
                Toast.makeText(this, "Please give access", Toast.LENGTH_LONG)
            }
            mDialog?.show(fragmentManager, null)
        })

        //gettig pathes
        pathes= dbHelper?.getMusicPathesFromDb()!!

        //init list of pathes
        folder_pathes.adapter = ArrayAdapter<Path>(this, android.R.layout.simple_list_item_1, pathes)
        folder_pathes.isTextFilterEnabled = true

        //delete path listener
        folder_pathes.setOnItemLongClickListener({ adapterView, view, i, l ->
            showDeleteDialog()
            delete_id = i
            true
        })

        //update music btn
        update_music_btn.setOnClickListener({
            SaveMusicDataToJsonFileTask(applicationContext).execute()
        })
    }

    fun showDeleteDialog() {
        val dialog = DeletePathDialog()
        dialog.show(fragmentManager, "DeletePathDialog")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        try {
            var db = dbHelper?.writableDatabase
            var id=pathes[delete_id ?: -1].id
            db?.delete("pathes", "id=?", arrayOf(id.toString()))
        } catch (e: Throwable) {
            Log.d(TAG, "Delete went wrong")
        }
        try {
            pathes.removeAt(delete_id ?: -1)
            (folder_pathes.adapter as ArrayAdapter<Path>).notifyDataSetChanged()
        } catch (e: Throwable) {
            Log.d(TAG, "Delete went wrong")
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onCancelChooser() {
        Toast.makeText(this, "Canceled", Toast.LENGTH_LONG).show()
        mDialog?.dismiss()
    }

    override fun onSelectDirectory(path: String) {
        Toast.makeText(this, path, Toast.LENGTH_LONG).show()
        var cv = ContentValues()
        mDialog?.dismiss()
        val db = dbHelper?.getWritableDatabase()
        cv.put("path", path)
        cv.put("datetime_modified", File(path).lastModified())
        try {
            val rowID = db?.insertOrThrow("pathes", null, cv)
            pathes.add(Path(rowID!!.toInt(),path, File(path).lastModified().toInt()))
        }catch (e: SQLiteConstraintException){
            Log.d(TAG,e.toString())
            Toast.makeText(this,"Upps add went wrong",Toast.LENGTH_LONG).show()
        }
        (folder_pathes.adapter as ArrayAdapter<*>).notifyDataSetChanged()
    }

}


