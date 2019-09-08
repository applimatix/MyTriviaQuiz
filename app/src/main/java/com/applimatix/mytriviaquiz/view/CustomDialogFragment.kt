package com.applimatix.mytriviaquiz.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

class CustomDialogFragment : DialogFragment() {

    companion object {
        private val TAG = CustomDialogFragment::class.java.simpleName
        private const val ARG_MESSAGE = "ARG_MESSAGE"
        private const val ARG_TITLE = "ARG_TITLE"
        private const val ARG_POSITIVE_BUTTON = "ARG_POSITIVE_BUTTON"
        private const val ARG_NEGATIVE_BUTTON = "ARG_NEGATIVE_BUTTON"

        fun show(
            title: CharSequence,
            message: CharSequence,
            positiveButton: CharSequence,
            negativeButton: CharSequence?,
            fragmentManager: FragmentManager,
            listener: ((Int) -> Unit)?
        ) = newInstance(title, message, positiveButton, negativeButton, listener).show(fragmentManager, TAG)

        private fun newInstance(
            title: CharSequence,
            message: CharSequence,
            positiveButton: CharSequence,
            negativeButton: CharSequence?,
            listener: ((Int) -> Unit)?
        ): CustomDialogFragment = CustomDialogFragment().apply {
            setButtonListener(listener)
            arguments = bundleOf(
                ARG_TITLE to title,
                ARG_MESSAGE to message,
                ARG_POSITIVE_BUTTON to positiveButton,
                ARG_NEGATIVE_BUTTON to negativeButton
            )
        }
    }

    private var listener: ((Int) -> Unit)? = null
    private val title by lazy { arguments?.getCharSequence(ARG_TITLE, null) ?: "" }
    private val message by lazy { arguments?.getCharSequence(ARG_MESSAGE, null) ?: "" }
    private val positiveButton by lazy { arguments?.getCharSequence(ARG_POSITIVE_BUTTON, null) ?: "" }
    private val negativeButton by lazy { arguments?.getCharSequence(ARG_NEGATIVE_BUTTON, null) ?: "" }

    fun setButtonListener(listener: ((Int) -> Unit)?) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { dialog, index ->
                listener?.invoke(index)
                dialog.dismiss()
            }
            .apply {
                if (negativeButton.isNotEmpty()) setNegativeButton(negativeButton) { dialog, index ->
                    listener?.invoke(index)
                    dialog.dismiss()
                }
            }
            .setOnDismissListener { dialog ->
                listener?.invoke(if (negativeButton.isNotEmpty()) {
                    DialogInterface.BUTTON_NEGATIVE
                } else {
                    DialogInterface.BUTTON_POSITIVE
                })
                dialog.dismiss()
            }
            .create()
}
