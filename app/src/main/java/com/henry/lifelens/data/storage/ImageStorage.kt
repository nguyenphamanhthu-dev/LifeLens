package com.henry.lifelens.data.storage

import android.content.Context
import android.net.Uri
import java.io.File

object ImageStorage {

    fun saveImage(
        context: Context,
        sourceUri: Uri
    ): Uri {

        val scansDirectory = File(
            context.filesDir,
            "scans"
        )

        if (!scansDirectory.exists()) {
            scansDirectory.mkdirs()
        }

        val destinationFile = File(
            scansDirectory,
            "scan_${System.currentTimeMillis()}.jpg"
        )

        context.contentResolver
            .openInputStream(sourceUri)
            ?.use { inputStream ->

                destinationFile
                    .outputStream()
                    .use { outputStream ->

                        inputStream.copyTo(outputStream)
                    }
            }
            ?: throw IllegalStateException(
                "Không thể đọc ảnh cần lưu"
            )

        return Uri.fromFile(
            destinationFile
        )
    }

    fun deleteImage(
        imageUri: String
    ): Boolean {

        if (imageUri.isBlank()) {
            return true
        }

        return try {

            val uri = Uri.parse(imageUri)

            val file = File(
                uri.path ?: return false
            )

            if (file.exists()) {
                file.delete()
            } else {
                true
            }

        } catch (exception: Exception) {
            false
        }
    }
}