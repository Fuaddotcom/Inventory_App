package com.TI23B1.inventoryapp.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.TI23B1.inventoryapp.R

class ChangePasswordDialog : DialogFragment() {

    interface OnPasswordChangeListener {
        fun onChangePassword(newPassword: String)
    }

    private var listener: OnPasswordChangeListener? = null

    fun setOnPasswordChangeListener(listener: OnPasswordChangeListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etNewPassword = view.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = view.findViewById<EditText>(R.id.etConfirmPassword)
        val btnSavePassword = view.findViewById<Button>(R.id.btnSavePassword)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelPassword)

        btnSavePassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Kata sandi tidak boleh kosong.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "Kata sandi tidak cocok.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(requireContext(), "Kata sandi minimal 6 karakter.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            listener?.onChangePassword(newPassword)
            dismiss()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        fun newInstance(): ChangePasswordDialog {
            return ChangePasswordDialog()
        }
    }
}