package top.yogiczy.mytv.tv.ui.screensold.main

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import top.yogiczy.mytv.core.data.entities.channel.ChannelGroupList
import top.yogiczy.mytv.tv.ui.material.CircularProgressIndicator
import top.yogiczy.mytv.tv.ui.material.Visible
import top.yogiczy.mytv.tv.ui.rememberChildPadding
import top.yogiczy.mytv.tv.ui.screen.main.MainUiState
import top.yogiczy.mytv.tv.ui.screensold.main.components.MainContent
import top.yogiczy.mytv.tv.ui.screensold.settings.SettingsScreen
import top.yogiczy.mytv.tv.ui.screensold.settings.SettingsViewModel
import top.yogiczy.mytv.tv.ui.theme.MyTvTheme
import top.yogiczy.mytv.tv.ui.tooling.PreviewWithLayoutGrids
import top.yogiczy.mytv.tv.ui.utils.backHandler
import top.yogiczy.mytv.tv.ui.utils.focusOnLaunched
import top.yogiczy.mytv.tv.ui.utils.handleKeyEvents

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    mainUiState: MainUiState.Ready,
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    MainContent(
        modifier = modifier,
        channelGroupListProvider = { mainUiState.channelGroupList },
        filteredChannelGroupListProvider = {
            ChannelGroupList(mainUiState.channelGroupList.filter { it.name !in settingsViewModel.iptvChannelGroupHiddenList })
        },
        epgListProvider = { mainUiState.epgList },
        onBackPressed = onBackPressed,
    )
}

@Composable
fun MainScreenMessage(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    messageProvider: () -> String? = { null }
) {
    val childPadding = rememberChildPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = childPadding.start, bottom = childPadding.bottom),
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.titleLarge
            ) {
                title()
            }

            val message = messageProvider()
            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalContentColor.current.copy(0.8f),
                    modifier = Modifier.sizeIn(maxWidth = 556.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MainScreenLoading(
    modifier: Modifier = Modifier,
    messageProvider: () -> String? = { null }
) {
    MainScreenMessage(
        modifier = modifier,
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("加载中")
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = LocalContentColor.current,
                    strokeWidth = 3.dp,
                )
            }
        },
        messageProvider = messageProvider
    )
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun MainScreenLoadingPreview() {
    MyTvTheme {
        PreviewWithLayoutGrids {
            MainScreenLoading(
                messageProvider = { "获取远程直播源(4/10)".repeat(10) }
            )
        }
    }
}

@Composable
private fun MainScreenError(
    modifier: Modifier = Modifier,
    messageProvider: () -> String? = { null }
) {
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.error
    ) {
        MainScreenMessage(
            modifier = modifier,
            title = { Text("加载失败") },
            messageProvider = messageProvider
        )
    }
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun MainScreenErrorPreview() {
    MyTvTheme {
        PreviewWithLayoutGrids {
            MainScreenError(
                messageProvider = {
                    "Caused by: androidx.media3.datasource.HttpDataSource\$HttpDataSourceException:" + " java.io.IOException: unexpected end of stream on com.android.okhttp.Address@2f10c24d"
                }
            )
        }
    }
}

@Composable
private fun MainScreenSettingsWrapper(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    var showSettings by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .focusOnLaunched()
            .focusable()
            .backHandler { onBackPressed() }
            .handleKeyEvents(
                onSettings = { showSettings = true },
                onLongSelect = { showSettings = true },
            ),
    ) {
        content()

        Visible({ showSettings }) { SettingsScreen() }
    }
}