package com.TI23B1.inventoryapp.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.TI23B1.inventoryapp.LoginActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.TI23B1.inventoryapp.MainActivity
import com.TI23B1.inventoryapp.R
import com.TI23B1.inventoryapp.UserControl
import com.TI23B1.inventoryapp.User
import com.TI23B1.inventoryapp.dialogs.ChangePasswordDialog
import com.TI23B1.inventoryapp.databinding.FragmentUserBinding

class UserFragment : Fragment(), ChangePasswordDialog.OnPasswordChangeListener {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    private val TAG_USER = "UserFragment"
    private lateinit var userControl: UserControl
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private var imageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imageUri = data?.data
            imageUri?.let {
                uploadProfileImage(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userControl = UserControl()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        loadUserProfile()
    }

    private fun setupListeners() {
        binding.buttonSignOut.setOnClickListener {
            showSignOutConfirmationDialog()
        }

        binding.buttonEditProfilePic.setOnClickListener {
            openImageChooser()
        }

        binding.buttonUpdateProfile.setOnClickListener {
            updateUserProfile()
        }

        binding.buttonChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun loadUserProfile() {
        val firebaseUser = userControl.getCurrentUser()

        if (firebaseUser != null) {
            userControl.readUserData(firebaseUser.uid) { userFromDb ->
                _binding?.let { viewBinding ->
                    if (userFromDb != null) {
                        if (userFromDb.profileImageUrl.isNotEmpty()) {
                            Glide.with(this)
                                .load(userFromDb.profileImageUrl)
                                .placeholder(R.drawable.ic_default_profile)
                                .error(R.drawable.ic_default_profile)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(viewBinding.imageViewProfile)
                            Log.d(TAG_USER, "Loading profile image from: ${userFromDb.profileImageUrl}")
                        } else {
                            Glide.with(this)
                                .load(R.drawable.ic_default_profile)
                                .into(viewBinding.imageViewProfile)
                            Log.d(TAG_USER, "No profile image URL, using default.")
                        }

                        viewBinding.tvUsernameDisplay.text = userFromDb.username
                        viewBinding.tvFullNameDisplay.text = userFromDb.namaLengkapUser
                        viewBinding.editTextUsername.setText(userFromDb.username)
                        viewBinding.editTextFullName.setText(userFromDb.namaLengkapUser)
                        viewBinding.editTextEmail.setText(userFromDb.emailUser)

                    } else {
                        Log.w(TAG_USER, "User data not found in database for UID: ${firebaseUser.uid}")
                        viewBinding.tvUsernameDisplay.text = "User ID Tidak Tersedia (DB)"
                        viewBinding.tvFullNameDisplay.text = "Nama Lengkap Tidak Tersedia (DB)"
                        viewBinding.editTextUsername.setText("")
                        viewBinding.editTextFullName.setText("")
                        viewBinding.editTextEmail.setText("Email Tidak Tersedia (DB)")
                        Glide.with(this).load(R.drawable.ic_default_profile).into(viewBinding.imageViewProfile)
                    }
                }
            }
        } else {
            _binding?.let { viewBinding ->
                Log.d(TAG_USER, "No Firebase user currently signed in.")
                viewBinding.tvUsernameDisplay.text = "Tidak Ada Pengguna"
                viewBinding.tvFullNameDisplay.text = "Silakan Masuk"
                viewBinding.editTextUsername.setText("")
                viewBinding.editTextFullName.setText("")
                viewBinding.editTextEmail.setText("")
                Glide.with(this).load(R.drawable.ic_default_profile).into(viewBinding.imageViewProfile)
            }
        }
    }

    private fun showSignOutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari akun?")
            .setPositiveButton("Ya") { dialog, which ->
                userControl.signOut()
                Toast.makeText(requireContext(), "Berhasil Logout", Toast.LENGTH_SHORT).show()
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun uploadProfileImage(uri: Uri) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Pengguna tidak masuk.", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = storage.reference.child("profile_images/$userId.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    updateUserProfileImageUrl(userId, imageUrl)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal mengunggah gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG_USER, "Image upload failed", e)
            }
    }

    private fun updateUserProfileImageUrl(userId: String, imageUrl: String) {
        val updates = mapOf("profileImageUrl" to imageUrl)
        userControl.updateUserProfile(userId, updates) { success, exception ->
            if (success) {
                _binding?.let { viewBinding ->
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_default_profile)
                        .error(R.drawable.ic_default_profile)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(viewBinding.imageViewProfile)
                }
                Toast.makeText(requireContext(), "Foto profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                Log.d(TAG_USER, "Profile image URL updated in DB: $imageUrl")
            } else {
                Toast.makeText(requireContext(), "Gagal memperbarui URL foto profil: ${exception?.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG_USER, "Failed to update profile image URL", exception)
            }
        }
    }

    private fun updateUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Pengguna tidak masuk.", Toast.LENGTH_SHORT).show()
            return
        }

        val username = binding.editTextUsername.text.toString().trim()
        val fullName = binding.editTextFullName.text.toString().trim()

        if (username.isEmpty() || fullName.isEmpty()) {
            Toast.makeText(requireContext(), "Username dan Nama Lengkap tidak boleh kosong.", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mutableMapOf<String, Any>()
        updates["username"] = username
        updates["namaLengkapUser"] = fullName

        userControl.updateUserProfile(userId, updates) { success, exception ->
            if (success) {
                Toast.makeText(requireContext(), "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                loadUserProfile() // Muat ulang data untuk memastikan tampilan terbaru
            } else {
                Toast.makeText(requireContext(), "Gagal memperbarui profil: ${exception?.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG_USER, "Profile update failed", exception)
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialog = ChangePasswordDialog.newInstance()
        dialog.setOnPasswordChangeListener(this)
        dialog.show(parentFragmentManager, "ChangePasswordDialog")
    }

    override fun onChangePassword(newPassword: String) {
        userControl.updatePassword(newPassword) { success, exception ->
            if (success) {
                Toast.makeText(requireContext(), "Kata sandi berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Gagal memperbarui kata sandi: ${exception?.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG_USER, "Password update failed", exception)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}