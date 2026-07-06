package com.example.passedpath.feature.care.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.care.presentation.component.ProtectedPersonBottomSheet
import com.example.passedpath.feature.care.presentation.model.ProtectedPersonBottomSheetTab
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonRouteDetailUiState
import com.example.passedpath.feature.route.presentation.screen.RouteMapContent
import com.example.passedpath.feature.route.presentation.state.PlaceMarkerUiState
import com.example.passedpath.ui.component.bottomsheet.BaseAnchoredBottomSheetScaffold
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetDefaults
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetValue
import com.example.passedpath.ui.component.feedback.MapOverlayNetworkFailureBanner
import com.example.passedpath.ui.component.loading.BaseLoadingIndicator
import com.example.passedpath.ui.state.CoordinateUiState
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.White
import com.example.passedpath.feature.care.presentation.viewmodel.ProtectedPersonRouteDetailViewModel
import com.example.passedpath.feature.care.presentation.viewmodel.ProtectedPersonRouteDetailViewModelFactory
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun ProtectedPersonRouteDetailRoute(
    dependentUserId: Long,
    dependentNickname: String,
    dateKey: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProtectedPersonRouteDetailViewModel = viewModel(
        factory = ProtectedPersonRouteDetailViewModelFactory(
            appContainer = LocalContext.current.appContainer,
            dependentUserId = dependentUserId,
            dependentNickname = dependentNickname,
            dateKey = dateKey
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    ProtectedPersonRouteDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRouteRetryClick = viewModel::retryRoute,
        onSheetValueChanged = viewModel::onSheetValueChanged,
        onSheetCommandConsumed = viewModel::onSheetCommandConsumed,
        onTabSelected = viewModel::selectBottomSheetTab,
        onPlaceMarkerClick = viewModel::onPlaceMarkerClick,
        onPlaceCardClick = viewModel::onPlaceCardClick,
        onSelectedPlaceHandled = viewModel::onSelectedPlaceHandled,
        onFocusedPlaceHandled = viewModel::onFocusedPlaceHandled,
        onMapClick = viewModel::onMapClick,
        onPlaceRetryClick = viewModel::retryPlaces,
        onPlaceGuideBannerClose = viewModel::dismissPlaceGuideBanner,
        onSummaryRetryClick = viewModel::retrySummary,
        modifier = modifier
    )
}

@Composable
internal fun ProtectedPersonRouteDetailScreen(
    uiState: ProtectedPersonRouteDetailUiState,
    onBackClick: () -> Unit,
    onRouteRetryClick: () -> Unit,
    onSheetValueChanged: (BaseBottomSheetValue) -> Unit,
    onSheetCommandConsumed: (BaseBottomSheetValue) -> Unit,
    onTabSelected: (ProtectedPersonBottomSheetTab) -> Unit,
    onPlaceMarkerClick: (Long) -> Unit,
    onPlaceCardClick: (Long) -> Unit,
    onSelectedPlaceHandled: () -> Unit,
    onFocusedPlaceHandled: () -> Unit,
    onMapClick: () -> Unit,
    onPlaceRetryClick: () -> Unit,
    onPlaceGuideBannerClose: () -> Unit,
    onSummaryRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseAnchoredBottomSheetScaffold(
        modifier = modifier.fillMaxSize(),
        initialSheetValue = uiState.bottomSheetValue,
        requestedSheetValue = uiState.requestedSheetValue,
        onSheetValueChanged = onSheetValueChanged,
        onSheetCommandConsumed = onSheetCommandConsumed,
        content = {
            ProtectedPersonRouteDetailMapContent(
                uiState = uiState,
                onBackClick = onBackClick,
                onRouteRetryClick = onRouteRetryClick,
                onPlaceMarkerClick = onPlaceMarkerClick,
                onFocusedPlaceHandled = onFocusedPlaceHandled,
                onMapClick = onMapClick
            )
        },
        sheet = { sheetModifier ->
            ProtectedPersonBottomSheet(
                selectedTab = uiState.selectedBottomSheetTab,
                onTabSelected = onTabSelected,
                placeListUiState = uiState.placeListUiState,
                summaryUiState = uiState.summaryUiState,
                selectedPlaceId = uiState.selectedPlaceId,
                onSelectedPlaceHandled = onSelectedPlaceHandled,
                onPlaceClick = onPlaceCardClick,
                onPlaceGuideBannerClose = onPlaceGuideBannerClose,
                onPlaceRetryClick = onPlaceRetryClick,
                onSummaryRetryClick = onSummaryRetryClick,
                modifier = sheetModifier
            )
        }
    )
}

@Composable
private fun ProtectedPersonRouteDetailMapContent(
    uiState: ProtectedPersonRouteDetailUiState,
    onBackClick: () -> Unit,
    onRouteRetryClick: () -> Unit,
    onPlaceMarkerClick: (Long) -> Unit,
    onFocusedPlaceHandled: () -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapStyleOptions = remember(context) {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.main_map_style)
    }
    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false
        )
    }
    val routePoints = remember(uiState.route.mapPolylinePoints) {
        uiState.route.mapPolylinePoints.map(CoordinateUiState::toLatLng)
    }
    val markerPlaces = uiState.markerPlaces
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            routePoints.firstOrNull() ?: SeoulFallbackLatLng,
            InitialRouteDetailMapZoom
        )
    }
    var isMapLoaded by remember { mutableStateOf(false) }
    var fittedRouteKey by rememberSaveable { mutableStateOf<String?>(null) }
    val routeFitKey = routePoints.toRouteFitKey(uiState.dateKey)

    LaunchedEffect(isMapLoaded, routeFitKey, uiState.isRouteLoading) {
        if (!isMapLoaded || uiState.isRouteLoading) return@LaunchedEffect
        if (routePoints.isEmpty() || fittedRouteKey == routeFitKey) return@LaunchedEffect

        cameraPositionState.move(createRouteDetailCameraUpdate(routePoints))
        fittedRouteKey = routeFitKey
    }

    LaunchedEffect(isMapLoaded, uiState.focusedPlaceId, markerPlaces) {
        val placeId = uiState.focusedPlaceId ?: return@LaunchedEffect
        if (!isMapLoaded) return@LaunchedEffect

        val target = markerPlaces.firstOrNull { marker -> marker.placeId == placeId }
            ?.toLatLng()
        if (target != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(target, FocusedPlaceMapZoom)
            )
        }
        onFocusedPlaceHandled()
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            contentPadding = PaddingValues(
                top = RouteDetailHeaderMapPadding,
                bottom = BaseBottomSheetDefaults.middleVisibleHeight * 0.3f
            ),
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapStyleOptions = mapStyleOptions
            ),
            uiSettings = mapUiSettings,
            onMapLoaded = { isMapLoaded = true },
            onMapClick = { onMapClick() }
        ) {
            RouteMapContent(
                routePoints = routePoints,
                markerPlaces = markerPlaces,
                routeAccentColor = Green500,
                onPlaceMarkerClick = { placeId ->
                    onPlaceMarkerClick(placeId)
                }
            )
        }

        ProtectedPersonRouteDetailHeader(
            dateText = uiState.route.dateText.ifBlank { uiState.dateKey },
            totalDistanceText = uiState.route.totalDistanceText,
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        ProtectedPersonRouteMapStatusOverlay(
            uiState = uiState,
            onRouteRetryClick = onRouteRetryClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun ProtectedPersonRouteDetailHeader(
    dateText: String,
    totalDistanceText: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 16.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_left),
                        contentDescription = stringResource(R.string.calendar_back),
                        tint = Gray400,
                        modifier = Modifier.size(width = 7.dp, height = 12.dp)
                    )
                }
                Text(
                    text = dateText,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge,
                    color = Gray900,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = Gray200)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(
                        R.string.main_total_distance_value,
                        totalDistanceText.ifBlank { "0.0km" }
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray500,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProtectedPersonRouteMapStatusOverlay(
    uiState: ProtectedPersonRouteDetailUiState,
    onRouteRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                top = RouteDetailStatusTopPadding,
                start = 16.dp,
                end = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            uiState.routeErrorMessage != null -> {
                MapOverlayNetworkFailureBanner(
                    retryText = stringResource(R.string.route_retry),
                    onRetryClick = onRouteRetryClick
                )
            }

            uiState.isRouteLoading -> {
                ProtectedPersonRouteStatusCard(
                    text = stringResource(R.string.care_route_detail_loading)
                ) {
                    BaseLoadingIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            uiState.isRouteEmpty -> {
                ProtectedPersonRouteStatusCard(
                    text = stringResource(R.string.care_route_detail_empty)
                )
            }
        }
    }
}

@Composable
private fun ProtectedPersonRouteStatusCard(
    text: String,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading?.invoke()
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun CoordinateUiState.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

private fun PlaceMarkerUiState.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

private fun List<LatLng>.toRouteFitKey(dateKey: String): String {
    val first = firstOrNull()
    val last = lastOrNull()
    return "$dateKey:$size:${first?.latitude}:${first?.longitude}:${last?.latitude}:${last?.longitude}"
}

private fun createRouteDetailCameraUpdate(routePoints: List<LatLng>): CameraUpdate {
    return when {
        routePoints.isEmpty() -> CameraUpdateFactory.zoomTo(InitialRouteDetailMapZoom)
        routePoints.size == 1 -> {
            CameraUpdateFactory.newLatLngZoom(routePoints.first(), SinglePointMapZoom)
        }

        else -> {
            val boundsBuilder = LatLngBounds.builder()
            routePoints.forEach(boundsBuilder::include)
            CameraUpdateFactory.newLatLngBounds(
                boundsBuilder.build(),
                RouteBoundsPaddingPx
            )
        }
    }
}

private val SeoulFallbackLatLng = LatLng(37.5662952, 126.9779451)
private val RouteDetailHeaderMapPadding = 104.dp
private val RouteDetailStatusTopPadding = 116.dp
private const val InitialRouteDetailMapZoom = 13f
private const val SinglePointMapZoom = 16f
private const val FocusedPlaceMapZoom = 17f
private const val RouteBoundsPaddingPx = 160
