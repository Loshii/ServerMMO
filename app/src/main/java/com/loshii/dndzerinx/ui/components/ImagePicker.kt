package com.loshii.dndzerinx.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.loshii.dndzerinx.util.CoilGifImage

@Composable
fun ImagePicker(
    currentImageUrl: String?,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    isLoading: Boolean = false,
    placeholder: @Composable () -> Unit = {}
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (currentImageUrl != null) {
            CoilGifImage(
                model = currentImageUrl,
                contentDescription = "Imagen",
                modifier = Modifier
                    .size(size)
                    .clip(if (size > 80.dp) RoundedCornerShape(16.dp) else CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            placeholder()
        }

        Box(
            modifier = Modifier
                .size(size)
                .clip(if (size > 80.dp) RoundedCornerShape(16.dp) else CircleShape)
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Cambiar imagen",
                    tint = Color.White,
                    modifier = Modifier.size(size / 3)
                )
            }
        }
    }
}

@Composable
fun EditableAvatar(
    currentImageUrl: String?,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    isLoading: Boolean = false
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (currentImageUrl != null) {
            CoilGifImage(
                model = currentImageUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(size / 2)
                )
            }
        }

        IconButton(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(size / 3)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Cambiar avatar",
                tint = Color.White,
                modifier = Modifier.size(size / 5)
            )
        }
    }
}

@Composable
fun EditableBanner(
    currentImageUrl: String?,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 150.dp,
    isLoading: Boolean = false
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .clickable { launcher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        if (currentImageUrl != null) {
            CoilGifImage(
                model = currentImageUrl,
                contentDescription = "Banner",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}