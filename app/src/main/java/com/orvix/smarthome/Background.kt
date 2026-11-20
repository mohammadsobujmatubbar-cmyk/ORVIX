package com.orvix.smarthome

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BgState {
    var uri by mutableStateOf("")
    var dim by mutableStateOf(1)

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences("orvix_settings", Context.MODE_PRIVATE)

    fun load(ctx: Context) {
        val p = prefs(ctx)
        uri = p.getString("bg_uri", "") ?: ""
        dim = p.getInt("bg_dim", 1)
    }

    fun setUri(ctx: Context, v: String) {
        uri = v
        prefs(ctx).edit().putString("bg_uri", v).apply()
    }

    fun setDim(ctx: Context, v: Int) {
        dim = v
        prefs(ctx).edit().putInt("bg_dim", v).apply()
    }
}

fun decodeBg(ctx: Context, uriStr: String): ImageBitmap? {
    if (uriStr.isBlank()) return null
    return try {
        val uri = Uri.parse(uriStr)
        val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, o) }
        var sample = 1
        while (o.outWidth / sample > 1600 || o.outHeight / sample > 2400) sample *= 2
        val o2 = BitmapFactory.Options().apply { inSampleSize = sample }
        ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, o2) }
            ?.asImageBitmap()
    } catch (e: Throwable) {
        null
    }
}

@Composable
fun BackgroundLayer() {
    val ctx = LocalContext.current
    val uriStr = BgState.uri
    val bmp by produceState<ImageBitmap?>(null, uriStr) {
        value = withContext(Dispatchers.IO) { decodeBg(ctx, uriStr) }
    }
    val img = bmp ?: return
    val top = WindowInsets.statusBars.getTop(LocalDensity.current)
    val a = when (BgState.dim) { 0 -> 0.25f; 1 -> 0.45f; else -> 0.65f }
    Box(
        Modifier.layout { m, c ->
            val p = m.measure(Constraints.fixed(c.maxWidth, c.maxHeight + top))
            layout(c.maxWidth, c.maxHeight) { p.place(0, -top) }
        }
    ) {
        Image(
            bitmap = img, contentDescription = null,
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = a)))
    }
}

@Composable
fun BackgroundSettingsCard(s: Strings) {
    val g = LocalGlass.current
    val ctx = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                ctx.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) { }
            BgState.setUri(ctx, uri.toString())
        }
    }
    GlassCard(Modifier.fillMaxWidth()) {
        Text(s.bgTitle, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassChip(s.bgChoose, false) { picker.launch(arrayOf("image/*")) }
            if (BgState.uri.isNotBlank()) {
                GlassChip(s.bgRemove, false) { BgState.setUri(ctx, "") }
            }
        }
        if (BgState.uri.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassChip(s.bgDimLow, BgState.dim == 0) { BgState.setDim(ctx, 0) }
                GlassChip(s.bgDimMid, BgState.dim == 1) { BgState.setDim(ctx, 1) }
                GlassChip(s.bgDimHigh, BgState.dim == 2) { BgState.setDim(ctx, 2) }
            }
        }
    }
}
