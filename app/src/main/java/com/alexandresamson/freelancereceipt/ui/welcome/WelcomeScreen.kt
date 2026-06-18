package com.alexandresamson.freelancereceipt.ui.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexandresamson.freelancereceipt.R
import com.alexandresamson.freelancereceipt.ui.theme.BrandBlue
import com.alexandresamson.freelancereceipt.ui.theme.BrandBlueDark
import com.alexandresamson.freelancereceipt.ui.theme.BrandSky
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    val logoScale = remember { Animatable(0.6f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, tween(durationMillis = 500))
        delay(100)
        contentAlpha.animateTo(1f, tween(durationMillis = 400))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(BrandBlue, BrandBlueDark))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(96.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Surface(
                    modifier = Modifier.size(140.dp).scale(logoScale.value),
                    shape = RoundedCornerShape(32.dp),
                    color = BrandSky,
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_splash_logo),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(110.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.app_name),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    modifier = Modifier.alpha(contentAlpha.value)
                )

                Text(
                    text = stringResource(R.string.welcome_tagline),
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(contentAlpha.value).padding(horizontal = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(contentAlpha.value)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureBullet(stringResource(R.string.welcome_feature_scan))
                FeatureBullet(stringResource(R.string.welcome_feature_organize))
                FeatureBullet(stringResource(R.string.welcome_feature_export))

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = BrandBlueDark
                    )
                ) {
                    Text(
                        stringResource(R.string.welcome_get_started),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun FeatureBullet(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "✓",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(text, color = Color.White, fontSize = 15.sp)
    }
}
