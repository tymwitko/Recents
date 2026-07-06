package com.tymwitko.recents.settings.whitelist

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.tymwitko.recents.R
import com.tymwitko.recents.common.ui.GrantPermissionScreen
import com.tymwitko.recents.common.ui.clearFocusOnKeyboardDismiss
import com.tymwitko.recents.settings.menu.WhitelistAppList
import com.tymwitko.recents.settings.navi.NavigationItem
import com.tymwitko.recents.settings.whitelist.ui.WhitelistItemData
import org.koin.androidx.compose.koinViewModel

@Composable
fun WhitelistSettingsScreen(
  viewModel: WhitelistViewModel = koinViewModel(),
  thisPackageName: String,
  navController: NavHostController
) {
  BackHandler {
    navController.navigate(NavigationItem.Menu.route)
  }
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(Unit) {
    viewModel.refreshPackages(
      thisPackageName
    )
  }
  val context = LocalContext.current
  val imageLoader = ImageLoader.Builder(context)
    .components {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) add(ImageDecoderDecoder.Factory())
      else add(GifDecoder.Factory())
    }
    .build()
  when(val state = uiState) {
    is WhitelistUiState.Loading -> {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context).data(data = R.drawable.loading)
              .apply(
                block = {
                  size(Size.ORIGINAL)
                }
              ).build(),
            imageLoader = imageLoader
          ),
          contentDescription = null
        )
      }
    }
    is WhitelistUiState.Success -> {
      val fieldState: TextFieldState = rememberTextFieldState()
      Column(
        modifier = Modifier
          .statusBarsPadding()
          .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        TextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clearFocusOnKeyboardDismiss(),
          state = fieldState,
          placeholder = { Text(stringResource(R.string.search_apps)) }
        )
        WhitelistAppList(
          modifier = Modifier
            .fillMaxHeight()
            .weight(1f),
          appList = state.list
            .filter {
              it.name.lowercase().contains(fieldState.text.toString().lowercase())
                || it.packageName.contains(fieldState.text.toString().lowercase())
            }.map {
              WhitelistItemData(it, viewModel.getSettingsForApp(it.getId()))
            },
          fontSize = viewModel.getFontSize(),
          whitelistLaunch = { pack, isChecked ->
            viewModel.whitelistAppLaunch(pack, isChecked)
          },
          whitelistKill = { pack, isChecked ->
            viewModel.whitelistAppKill(pack, isChecked)
          },
          whitelistShow = { pack, isChecked ->
            viewModel.whitelistAppShow(pack, isChecked)
          },
          showKillCheck = state.hasPrivileges,
          iconSize =
            viewModel.getIconSize(dimensionResource(R.dimen.icon_dimension).value.toInt())
        )
      }
    }
    is WhitelistUiState.MissingPermissions -> GrantPermissionScreen()
  }
}
