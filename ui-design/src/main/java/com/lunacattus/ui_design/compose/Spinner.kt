package com.lunacattus.ui_design.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.lunacattus.ui_design.R

@Composable
fun Spinner(modifier: Modifier = Modifier) {
    AsyncImage(
        model = spinnerRequest(),
        imageLoader = rememberGifImageLoader(),
        contentDescription = "",
        modifier = modifier
    )
}

@Composable
fun rememberGifImageLoader(): ImageLoader {
    val context = LocalContext.current
    return remember {
        ImageLoader.Builder(context)
            .components {
                add(ImageDecoderDecoder.Factory())
            }
            .build()
    }
}

@Composable
fun spinnerRequest(): Any {
    val context = LocalContext.current
    return remember {
        ImageRequest.Builder(context)
            .data(R.drawable.spiner)
            .crossfade(true)
            .build()
    }
}