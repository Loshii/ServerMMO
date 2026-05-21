package com.loshii.dndzerinx.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ImageUploader {
    private const val STORAGE_BUCKET = "gs://slyvee-2958c"

    private val TAG = "ImageUploader"

    // Do not hardcode a single storage instance. Prefer the default configured in
    // `google-services.json` and fall back to the more-typical `appspot.com` bucket
    // if the default attempt fails with a 404 during upload.
    private fun defaultStorage(): FirebaseStorage =
        FirebaseStorage.getInstance().also {
            Log.d(TAG, "FirebaseStorage default bucket=${it.reference.bucket}")
        }

    private fun fallbackStorageVariants(): List<FirebaseStorage> = listOf(
        FirebaseStorage.getInstance(STORAGE_BUCKET)
    ).map {
        it.also { s -> Log.d(TAG, "FirebaseStorage fallback candidate=${s.reference.bucket}") }
    }

    private fun getMimeType(context: Context, uri: Uri): String {
        val resolver = context.contentResolver
        val type = resolver.getType(uri)
        if (!type.isNullOrBlank()) {
            return type
        }

        val uriString = uri.toString()
        val extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(uriString)
            .lowercase().takeIf { it.isNotBlank() }
        if (!extension.isNullOrBlank()) {
            android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)?.let {
                return it
            }
        }

        uri.path?.substringAfterLast('.', "")?.lowercase()?.takeIf { it.isNotBlank() }?.let { ext ->
            android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)?.let {
                return it
            }
        }

        return "image/jpeg"
    }

    private fun getExtension(mimeType: String): String {
        val normalized = mimeType.substringBefore(';').trim().lowercase()
        return when (normalized) {
            "image/gif" -> "gif"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/jpeg", "image/jpg" -> "jpg"
            else -> "jpg"
        }
    }

    suspend fun uploadAvatar(context: Context, uri: Uri, userId: String): String? {
        return uploadImage(context, uri, userId, "avatars")
    }

    suspend fun uploadBanner(context: Context, uri: Uri, userId: String): String? {
        return uploadImage(context, uri, userId, "banners")
    }

    private suspend fun uploadImage(
        context: Context,
        uri: Uri,
        userId: String,
        folder: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting upload to folder: $folder, userId: $userId")

            val mimeType = getMimeType(context, uri)
            val ext = getExtension(mimeType)
            val filename = "$folder/$userId/${UUID.randomUUID()}.$ext"
            // Try using the default configured storage first, then multiple fallback candidates
            val storageCandidates = mutableListOf<FirebaseStorage>()
            storageCandidates.add(defaultStorage())
            storageCandidates.addAll(fallbackStorageVariants())

            val metadata = StorageMetadata.Builder()
                .setContentType(mimeType)
                .build()

            for ((index, st) in storageCandidates.withIndex()) {
                try {
                    val ref = st.reference.child(filename)
                    Log.d(TAG, "Attempt ${index + 1}: Uploading to: $filename, mimeType: $mimeType, bucket=${st.reference.bucket}, refPath=${ref.path}")

                    val uploadTask = ref.putFile(uri, metadata).await()
                    Log.d(TAG, "Attempt ${index + 1}: Upload complete, bytes: ${uploadTask.totalByteCount}")

                    val downloadUri = ref.downloadUrl.await()
                    val downloadUrl = downloadUri.toString()
                    Log.d(TAG, "Attempt ${index + 1}: Download URL: $downloadUrl")
                    return@withContext downloadUrl
                } catch (e: Exception) {
                    Log.e(TAG, "Attempt ${index + 1}: Upload failed: ${e.message}", e)
                    // If this is the last candidate, rethrow / return null below
                }
            }

            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed: ${e.message}", e)
            return@withContext null
        }
    }

    suspend fun deleteImage(url: String): Boolean {
        return try {
            // Try delete using default storage first, then fallback
            try {
                val ref = defaultStorage().getReferenceFromUrl(url)
                ref.delete().await()
                return true
            } catch (e: Exception) {
                Log.w(TAG, "Default storage delete failed, trying fallback: ${e.message}")
            }

            for (fb in fallbackStorageVariants()) {
                try {
                    val ref2 = fb.getReferenceFromUrl(url)
                    ref2.delete().await()
                    return true
                } catch (e: Exception) {
                    Log.w(TAG, "Fallback candidate delete failed: ${e.message}")
                }
            }

            false
        } catch (e: Exception) {
            Log.e(TAG, "Delete failed", e)
            false
        }
    }
}