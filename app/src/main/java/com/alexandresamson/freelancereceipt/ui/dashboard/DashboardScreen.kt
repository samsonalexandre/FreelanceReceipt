package com.alexandresamson.freelancereceipt.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// WICHTIG: Ersetze das durch deinen echten Paketnamen, falls er abweicht, damit das "R" gefunden wird!
import com.alexandresamson.freelancereceipt.R

@Composable
fun DashboardScreen() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Hier kommt später die Scanner-Logik rein */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    // Hier greifen wir auf die XML zu:
                    contentDescription = stringResource(id = R.string.fab_scan_content_desc)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                // Hier greifen wir auf die XML zu:
                text = stringResource(id = R.string.dashboard_empty_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                // Hier greifen wir auf die XML zu:
                text = stringResource(id = R.string.dashboard_empty_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, locale = "de") // Preview auf Deutsch zwingen
@Composable
fun DashboardScreenPreviewDe() {
    MaterialTheme {
        DashboardScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true, locale = "en") // Preview auf Englisch zwingen
@Composable
fun DashboardScreenPreviewEn() {
    MaterialTheme {
        DashboardScreen()
    }
}