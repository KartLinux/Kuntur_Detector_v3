package com.denicks21.speechandtext.ui.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.guava.await

suspend fun bindCameraPreview(context: Context, previewView: PreviewView) {
    val cameraProvider = ProcessCameraProvider.getInstance(context).await()
    val preview = Preview.Builder().build().also {
        it.setSurfaceProvider(previewView.surfaceProvider)
    }

    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    cameraProvider.unbindAll()
    cameraProvider.bindToLifecycle(
        /* lifecycleOwner= */ context as androidx.lifecycle.LifecycleOwner,
        cameraSelector,
        preview
    )
}