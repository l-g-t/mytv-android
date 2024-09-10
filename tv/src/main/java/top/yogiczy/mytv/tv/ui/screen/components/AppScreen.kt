package top.yogiczy.mytv.tv.ui.screen.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import kotlinx.serialization.Serializable
import top.yogiczy.mytv.tv.ui.rememberChildPadding
import top.yogiczy.mytv.tv.ui.screensold.settings.LocalSettings
import top.yogiczy.mytv.tv.ui.theme.MyTvTheme
import top.yogiczy.mytv.tv.ui.utils.focusOnLaunched
import top.yogiczy.mytv.tv.ui.utils.handleKeyEvents
import kotlin.math.roundToInt

@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    header: @Composable (() -> Unit)? = null,
    headerExtra: @Composable (() -> Unit)? = null,
    canBack: Boolean = false,
    enableTopBarHidden: Boolean = false,
    onBackPressed: () -> Unit = {},
    content: @Composable BoxScope.(updateTopBarVisibility: (Boolean) -> Unit) -> Unit,
) {
    val density = LocalDensity.current
    var isTopBarVisible by remember { mutableStateOf(true) }
    var topBarHeightPx by rememberSaveable {
        mutableIntStateOf(with(density) { 64.dp.toPx().roundToInt() })
    }
    val topBarYOffsetPx by animateIntAsState(
        targetValue = if (isTopBarVisible) 0 else -topBarHeightPx,
        animationSpec = tween(),
        label = "",
    )
    val contentPaddingPx by animateIntAsState(
        targetValue = if (isTopBarVisible) topBarHeightPx else 0,
        animationSpec = tween(),
        label = "",
    )

    BackHandler { onBackPressed() }
    AppThemeWrapper {
        if (enableTopBarHidden) {
            AppScaffoldTopBar(
                modifier = Modifier
                    .height(64.dp)
                    .offset { IntOffset(x = 0, y = topBarYOffsetPx) }
                    .onSizeChanged { topBarHeightPx = it.height },
                header = header,
                headerExtra = headerExtra,
                canBack = canBack,
                onBackPressed = onBackPressed,
            )

            Box(
                modifier = modifier.offset { IntOffset(x = 0, y = contentPaddingPx) },
            ) {
                content { isTopBarVisible = it }
            }
        } else {
            Column {
                AppScaffoldTopBar(
                    modifier = Modifier.height(64.dp),
                    header = header,
                    headerExtra = headerExtra,
                    canBack = canBack,
                    onBackPressed = onBackPressed,
                )

                Box(modifier = modifier) {
                    content { }
                }
            }
        }
    }
}

@Composable
private fun AppScaffoldTopBar(
    modifier: Modifier = Modifier,
    header: @Composable (() -> Unit)? = null,
    headerExtra: @Composable (() -> Unit)? = null,
    canBack: Boolean = false,
    onBackPressed: () -> Unit = {},
) {
    val childPadding = rememberChildPadding()

    if (header != null || headerExtra != null) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(childPadding.copy(bottom = 0.dp).paddingValues),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (canBack) AppScaffoldBack(onBack = onBackPressed)
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.titleLarge
                ) { header?.let { nnHeader -> nnHeader() } }
            }
            Box { headerExtra?.let { nnHeaderExtra -> nnHeaderExtra() } }
        }
    }
}

@Composable
private fun AppScaffoldBack(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    Button(
        modifier = modifier
            .focusOnLaunched()
            .handleKeyEvents(onSelect = onBack),
        colors = ButtonDefaults.colors(
            containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        ),
        onClick = {},
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text("返回")
        }
    }
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun AppScaffoldPreview() {
    MyTvTheme {
        AppScreen(
            header = { Text("Header头部") },
            headerExtra = { Text("Header Extra") },
            canBack = true,
        ) {
            Text("Content".repeat(10))
        }
//        PreviewWithLayoutGrids { }
    }
}

@Serializable
data class AppThemeDef(
    val name: String,
    val base64: String,
    val color: Long,
    val image: String? = null,
)

/**
 * 地海蔚蓝
 */
@Composable
fun AppThemeWrapper(
    appThemeDef: AppThemeDef? = LocalSettings.current.themeAppCurrent,
    content: @Composable () -> Unit,
) {
    if (appThemeDef == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF141E30),
                            Color(0xFF243B55),
                            Color(0xFF141E30)
                        ),
                    )
                )
        )
    } else {
        val imageBytes = Base64.decode(appThemeDef.base64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val imageBitmap = bitmap.asImageBitmap()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(appThemeDef.color))
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            appThemeDef.image?.let { nnImage ->
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(nnImage)
                        .decoderFactory(SvgDecoder.Factory())
                        .build(),
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (nnImage.endsWith(".svg")) 0.12f else 0.8f),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x4D000000))
        ) {}
    }

    content()
}