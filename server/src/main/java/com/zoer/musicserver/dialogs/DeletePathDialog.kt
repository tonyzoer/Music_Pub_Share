package com.zoer.musicserver.dialogs

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.content.DialogInterface
import android.R.string.cancel
import android.support.v7.app.AlertDialog
import com.zoer.musicserver.R
import com.zoer.musicserver.dialogs.DeletePathDialog.NoticeDialogListener
import android.app.Activity




class DeletePathDialog : DialogFragment() {

    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }
    var mListener:NoticeDialogListener?=null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.delete_this_path)
                .setPositiveButton(R.string.delete, DialogInterface.OnClickListener { dialog, id ->
                    mListener?.onDialogPositiveClick(this)
                })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
                    mListener?.onDialogNegativeClick(this)
                })
        // Create the AlertDialog object and return it
        return builder.create()
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = activity as NoticeDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(activity.toString() + " must implement NoticeDialogListener")
        }

    }

}