package com.denicks21.speechandtext.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.denicks21.speechandtext.R

/**
 * AppTopBar composable:
 * - Displays a top app bar with a horizontal gradient background.
 * - Contains an app logo, centered title, larger notification icon, and a subtitle below, all centered.
 * - Relies on MaterialTheme for colors, typography, and dimensions to ensure consistency.
 */
@Composable
fun AppTopBar() {
    TopAppBar(
        backgroundColor = Color.Transparent,             // Transparent to let gradient show
        contentColor    = MaterialTheme.colors.onPrimary, // Tint for child content
        elevation       = 0.dp,                           // No shadow over gradient
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)                              // Increased height for two lines
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colors.primary,
                        MaterialTheme.colors.primaryVariant
                    )
                )
            ),
        contentPadding = PaddingValues(0.dp)             // Remove default padding
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),               // Outer horizontal padding
            verticalArrangement = Arrangement.Center,       // Center content vertically
            horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
        ) {
            // Main row: logo, title, and notification icon centered as a group
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1) App logo
                Image(
                    painter            = painterResource(id = R.drawable.logo_app_blanco),
                    contentDescription = "App logo",
                    modifier           = Modifier.size(58.dp)    // Logo size
                        .padding(top = 8.dp) // Center vertically in row
                )

                Spacer(modifier = Modifier.width(18.dp))         // Space between logo and title

                // 2) Title text
                Image(
                    painter            = painterResource(id = R.drawable.titulo),
                    contentDescription = "Titulo de la app",
                    modifier           = Modifier.size(188.dp)    // Logo size
                        .padding(top = 8.dp) // Center vertically in row
                )

                Spacer(modifier = Modifier.width(18.dp))         // Space between title and icon

                // 3) Notification icon button
                IconButton(
                    onClick = { /* TODO: Handle notification click */ },
                    modifier = Modifier.size(46.dp)              // Increase touch target
                ) {
                    Icon(
                        painter            = painterResource(id = R.drawable.ic_micro),
                        contentDescription = "Notifications",
                        tint               = MaterialTheme.colors.onPrimary,
                        modifier           = Modifier.size(58.dp) // Icon size inside button
                            .padding(top = 8.dp) // Center vertically in button
                    )
                }
            }

            Spacer(modifier = Modifier.height(0.dp))            // Tight spacing before subtitle

            // Subtitle text below, centered
            Text(
                text     = "Seguridad desde las Nubes",        // Subtitle text
                style    = MaterialTheme.typography.caption,    // Smaller caption
                color    = MaterialTheme.colors.onPrimary.copy(alpha = 0.8f)
                // Already centered by Column's horizontalAlignment
            )
        }
    }
}
