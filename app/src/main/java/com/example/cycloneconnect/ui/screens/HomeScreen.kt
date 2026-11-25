package com.example.cycloneconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cycloneconnect.R
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import com.example.cycloneconnect.data.CoastalCity
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cycloneconnect.AppRoutes
import com.example.cycloneconnect.ui.theme.emergencyRed
import com.example.cycloneconnect.ui.theme.onEmergencyRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {

    // Gets the current Android context for simple feedback messages.
    val context = LocalContext.current

    // Controls whether the navigation drawer is open or closed.
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    // Allows us to open and close the drawer using coroutines.
    val scope = rememberCoroutineScope()

    // Main navigation drawer containing the app menu.
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "CycloneConnect",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(20.dp)
                )

                HorizontalDivider()

                // Menu items are reusable composables.

                DrawerItem("Home", Icons.Default.Home) {
                    scope.launch {
                        drawerState.close()
                        onNavigate(AppRoutes.HOME)
                    }
                }

                DrawerItem("Notifications", Icons.Default.Notifications) {
                    scope.launch {
                        drawerState.close()
                        onNavigate(AppRoutes.NOTIFICATIONS)
                    }
                }

                DrawerItem("Volunteer", Icons.Default.VolunteerActivism) {
                    scope.launch {
                        drawerState.close()
                        onNavigate(AppRoutes.VOLUNTEER)
                    }
                }

                DrawerItem("Donate to verified charities", Icons.Default.Favorite) {
                    scope.launch {
                        drawerState.close()
                        onNavigate(AppRoutes.DONATE)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                DrawerItem("Privacy", Icons.Default.PrivacyTip) {
                    scope.launch {
                        drawerState.close()
                        onNavigate(AppRoutes.PRIVACY)
                    }
                }

                DrawerItem("Help & Support", Icons.AutoMirrored.Filled.Help) {
                    scope.launch {
                        drawerState.close()
                        onNavigate(AppRoutes.HELP)
                    }
                }

                DrawerItem("Settings", Icons.Default.Settings) {
                    scope.launch {
                        drawerState.close()
                        onNavigate(AppRoutes.SETTINGS)
                    }
                }

                // Theme switcher.
                // Separates the theme setting from the other drawer options.
                HorizontalDivider(
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Dark mode setting.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Dark mode icon.
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )

                    // Theme setting label.
                    Text(
                        text = "Dark Mode",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    )

                    // Theme switch.
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onThemeChange
                    )
                }

                DrawerItem("About", Icons.Default.Info) {
                    scope.launch {
                        drawerState.close()
                        onNavigate(AppRoutes.ABOUT)
                    }
                }
            }
        }
    ) {

        // Scaffold provides the main app layout.
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "CycloneConnect",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        // Hamburger icon opens the navigation drawer.
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open menu"
                            )
                        }
                    },
                    actions = {
                        // Your original logo is displayed without modification.
                        Image(
                            painter = painterResource(R.drawable.cyclone_connect_logo),
                            contentDescription = "CycloneConnect logo",
                            modifier = Modifier
                                .size(44.dp)
                                .padding(end = 8.dp)
                        )
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                // Only the content below the SOS button scrolls.
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    item{
                        SOSSection(
                            onClick = { onNavigate(AppRoutes.SOS) },
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                        )
                    }

                    // LOCATION
                    item {
                        LocationSection()
                    }

                    // CURRENT AND PREDICTED CYCLONE CONDITIONS
                    item {
                        CycloneForecastCard()
                    }

                    // DEMO-ONLY: remove after the demo.
                    item {
                        NewsCard(
                            title = "Cyclone Intelligence (Demo)",
                            description = "On-device CNN wind prediction on a sample image",
                            icon = Icons.Default.Science,
                            onClick = { onNavigate(AppRoutes.CNN_DEMO) }
                        )
                    }

                    // SEARCH OTHER LOCATIONS
                    item {
                        CoastalCitySearch(
                            onCitySelected = { city: CoastalCity ->
                                onNavigate(AppRoutes.searchRoute(city.name))
                            }
                        )
                    }

                    // EMERGENCY RESOURCES
                    item {
                        SectionHeading("Emergency Assistance")
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FeatureCard(
                                title = "Emergency Information",
                                description = "Safety instructions and preparedness",
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigate(AppRoutes.EMERGENCY) }
                            )

                            FeatureCard(
                                title = "Family & Friends",
                                description = "Your important contacts",
                                icon = Icons.Default.People,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigate(AppRoutes.FAMILY) }
                            )
                        }
                    }

                    // NEWS
                    item {
                        SectionHeading("News & Updates")
                    }

                    item {
                        NewsCard(
                            title = "Official Cyclone Alerts",
                            description = "Updates from meteorological authorities",
                            icon = Icons.Default.Campaign,
                            onClick = { onNavigate(AppRoutes.OFFICIAL_ALERTS) }
                        )
                    }

                    item {
                        NewsCard(
                            title = "Regional News Feed",
                            description = "News and announcements from your area",
                            icon = Icons.Default.Newspaper,
                            onClick = { onNavigate(AppRoutes.REGIONAL_NEWS) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}


// --------------------------------------------------
// LOCATION SECTION
// Displays the user's current location.
// Actual GPS integration will be added later.
// --------------------------------------------------

@Composable
fun LocationSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = MaterialTheme.colorScheme.primary
        )

        Column {
            Text(
                text = "YOUR LOCATION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Location not detected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


// --------------------------------------------------
// CYCLONE FORECAST CARD
// Displays sample current and predicted conditions.
// --------------------------------------------------

@Composable
fun CycloneForecastCard() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Local Cyclone Updates",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "Weather"
                )
            }

            // Current conditions.
            Text(
                text = "CURRENT CONDITIONS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Cyclonic Storm",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ForecastMetric(
                    title = "Wind speed",
                    value = "75 km/h",
                    modifier = Modifier.weight(1f)
                )

                ForecastMetric(
                    title = "Intensity",
                    value = "CS",
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()

            // Predicted conditions.
            Text(
                text = "PREDICTED CONDITIONS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ForecastMetric(
                    title = "Predicted wind",
                    value = "85 km/h",
                    modifier = Modifier.weight(1f)
                )

                ForecastMetric(
                    title = "Predicted intensity",
                    value = "SCS",
                    modifier = Modifier.weight(1f)
                )
            }

            // Placeholder for the future interactive cyclone map.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Storm map",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Text(
                        text = "Cyclone tracking map",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Text(
                        text = "Map integration coming later",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Text(
                text = "Illustrative sample data – not an official warning.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// --------------------------------------------------
// REUSABLE FORECAST METRIC
// Used for wind speed and intensity values.
// --------------------------------------------------

@Composable
fun ForecastMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}


// --------------------------------------------------
// SECTION HEADING
// Reusable heading for different home page sections.
// --------------------------------------------------

@Composable
fun SectionHeading(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}


// --------------------------------------------------
// ACTION CARD
// Shared look for all tappable cards.
// --------------------------------------------------

// Shared look for everything that can be tapped: solid primary colour,
// a drop shadow, and a small "press in" animation, so it never looks like a plain info card.
@Composable
fun ActionCard(
    shape: Shape,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        label = "pressScale"
    )

    Card(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = shape,
        interactionSource = interactionSource,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        ),
        content = content
    )
}

// Round badge behind the icon on tappable cards.
@Composable
private fun IconBadge(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(26.dp)
        )
    }
}

// --------------------------------------------------
// FEATURE CARD
// Used for Emergency Information and Family & Friends.
// --------------------------------------------------

@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ActionCard(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.heightIn(min = 150.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconBadge(icon)

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open"
                )
            }
        }
    }
}

// --------------------------------------------------
// NEWS CARD
// Separates official alerts from general regional news.
// --------------------------------------------------

@Composable
fun NewsCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ActionCard(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconBadge(icon)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open"
            )
        }
    }
}


// --------------------------------------------------
// SOS SECTION
// A prominent emergency action.
// --------------------------------------------------

@Composable
fun SOSSection(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
                pressedElevation = 2.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = emergencyRed,
                contentColor = onEmergencyRed
            )
        ) {

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "SOS - Emergency Help",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// --------------------------------------------------
// DRAWER ITEM
// Reusable navigation drawer menu item.
// --------------------------------------------------

@Composable
fun DrawerItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(title)
        },
        selected = false,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = title
            )
        },
        modifier = Modifier.padding(
            NavigationDrawerItemDefaults.ItemPadding
        )
    )
}
