package com.SheeraNergaon.KindNest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.SheeraNergaon.KindNest.databinding.ActivityUploadBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class UploadActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.activity_upload

    private lateinit var binding: ActivityUploadBinding

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            fetchGalleryImages()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.bind(findViewById(android.R.id.content))

        binding.pickImageButton.setOnClickListener {
            val intent = Intent(this, PickImageActivity::class.java)
            pickImageLauncher.launch(intent)
        }

        fetchGalleryImages()
    }

    private fun fetchGalleryImages() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val galleryRef = Firebase.database.reference.child("deed_gallery").child(userId)
        binding.progressBar.visibility = View.VISIBLE

        galleryRef.get().addOnSuccessListener { snapshot ->
            val images = mutableListOf<GalleryImage>()
            for (child in snapshot.children) {
                val item = child.getValue(GalleryImage::class.java)
                item?.let { images.add(it) }
            }

            val recyclerView = binding.mainRecyclerDone
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = GalleryAdapter(images) { image ->
                showImageOptions(image)
            }

            recyclerView.scrollToPosition(0)
            binding.progressBar.visibility = View.GONE
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Failed to load gallery", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImageOptions(image: GalleryImage) {
        val options = arrayOf("Edit Caption", "Delete Image")

        AlertDialog.Builder(this)
            .setTitle("Choose an action")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditCaptionDialog(image)
                    1 -> deleteImage(image)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditCaptionDialog(image: GalleryImage) {
        val editText = EditText(this)
        editText.setText(image.caption)

        AlertDialog.Builder(this)
            .setTitle("Edit Caption")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newCaption = editText.text.toString().trim()
                if (newCaption.isNotEmpty()) {
                    updateImageCaption(image, newCaption)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateImageCaption(image: GalleryImage, newCaption: String) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val galleryRef = Firebase.database.reference.child("deed_gallery").child(userId)

        galleryRef.orderByChild("url").equalTo(image.url)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.child("caption").setValue(newCaption)
                    }
                    fetchGalleryImages()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UploadActivity, "Update failed", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun deleteImage(image: GalleryImage) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val galleryRef = Firebase.database.reference.child("deed_gallery").child(userId)

        galleryRef.orderByChild("url").equalTo(image.url)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }

                    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(image.url)
                    storageRef.delete().addOnSuccessListener {
                        Toast.makeText(this@UploadActivity, "Image deleted", Toast.LENGTH_SHORT).show()
                        fetchGalleryImages()
                    }.addOnFailureListener {
                        Toast.makeText(this@UploadActivity, "Failed to delete from storage", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UploadActivity, "Database error", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
