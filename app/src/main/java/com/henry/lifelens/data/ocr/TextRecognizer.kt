package com.henry.lifelens.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

fun recognizeTextFromImage(
    context: Context,
    imageUri: Uri,
    onSuccess: (String) -> Unit,
    onError: (Exception) -> Unit
) {

    try {

        val image = InputImage.fromFilePath(
            context,
            imageUri
        )

        val recognizer = TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )

        recognizer
            .process(image)
            .addOnSuccessListener { result ->

                onSuccess(result.text)

                recognizer.close()
            }
            .addOnFailureListener { exception ->

                onError(exception)

                recognizer.close()
            }

    } catch (exception: Exception) {

        onError(exception)
    }
}