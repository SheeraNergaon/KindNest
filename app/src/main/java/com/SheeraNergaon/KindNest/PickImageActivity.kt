package com.SheeraNergaon.KindNest

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class PickImageActivity : AppCompatActivity() {

    private var selectedUri: Uri? = null
    private lateinit var captionInput: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var imagePreview: ImageView

    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            imagePreview.setImageURI(uri)
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_image)

        captionInput = findViewById(R.id.captionInput)
        progressBar = findViewById(R.id.progressBar)
        imagePreview = findViewById(R.id.imagePreview)

        findViewById<ImageButton>(R.id.pickImageFromGalleryButton).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        findViewById<Button>(R.id.confirmUploadButton).setOnClickListener {
            onUploadClicked()
        }

    }

    private fun onUploadClicked() {
        val uri = selectedUri
        val caption = captionInput.text.toString().trim()

        if (uri == null) {
            Toast.makeText(this, "Please select an image first.", Toast.LENGTH_SHORT).show()
            return
        }

        if (caption.isEmpty()) {
            Toast.makeText(this, "Please enter a caption.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
        progressBar.visibility = View.VISIBLE

        uploadImageToFirebase(uri, caption)
    }

    private fun uploadImageToFirebase(uri: Uri, caption: String) {
        val fileName = "${System.currentTimeMillis()}.jpg"
        val storage = FirebaseStorage.getInstance()
        val imageRef = storage.reference.child("gallery/$userId/$fileName")

        try {
            // Optional: check MIME type
            val mimeType = contentResolver.getType(uri)
            println("DEBUG >>> MIME Type: $mimeType | URI: $uri")

            val uploadTask = imageRef.putFile(uri)
            uploadTask
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    println("Upload is $progress% done")
                }
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveUploadToDatabase(downloadUri.toString(), caption)
                    }.addOnFailureListener {
                        showError("Failed to get download URL: ${it.message}")
                    }
                }
                .addOnFailureListener {
                    showError("Upload failed: ${it.message}")
                }

        } catch (e: Exception) {
            showError("Exception during upload: ${e.message}")
        }
    }


    private fun saveUploadToDatabase(downloadUrl: String, caption: String) {
        val db = Firebase.database.reference
        val deedId = db.child("deed_gallery").child(userId).push().key ?: return

        val data = mapOf(
            "url" to downloadUrl,
            "caption" to caption,
            "timestamp" to System.currentTimeMillis()
        )

        db.child("deed_gallery").child(userId).child(deedId).setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener {
                showError("Database error: ${it.message}")
            }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        progressBar.visibility = View.GONE
    }
}
