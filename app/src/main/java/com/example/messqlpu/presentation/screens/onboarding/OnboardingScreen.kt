package com.example.messqlpu.presentation.screens.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messqlpu.R
import com.example.messqlpu.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch

private data class OnboardingSlideData(
    val title: String,
    val subtitle: String,
    @DrawableRes val illustrationRes: Int,
    val features: List<OnboardingFeatureItem>
)

private data class OnboardingFeatureItem(
    val icon: ImageVector,
    val text: String
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val slides = remember {
        listOf(
            OnboardingSlideData(
                title = "Skip the queue.\nSavour your time.",
                subtitle = "Live crowd updates, smart suggestions and easy food ordering — all in one app.",
                illustrationRes = R.drawable.onboarding_slide1,
                features = listOf(
                    OnboardingFeatureItem(Icons.Default.Bolt, "Live queue tracking"),
                    OnboardingFeatureItem(Icons.Default.AutoAwesome, "AI powered suggestions"),
                    OnboardingFeatureItem(Icons.Default.RestaurantMenu, "Pre-order your meals"),
                    OnboardingFeatureItem(Icons.Default.Explore, "Explore campus dining")
                )
            ),
            OnboardingSlideData(
                title = "Smart Dining.\nPowered by AI.",
                subtitle = "Get personalized meal recommendations to avoid peak hours and save up to 18 minutes.",
                illustrationRes = R.drawable.onboarding_slide2,
                features = listOf(
                    OnboardingFeatureItem(Icons.Default.Schedule, "Smart peak hour forecasting"),
                    OnboardingFeatureItem(Icons.Default.Psychology, "Personalized meal taste profile"),
                    OnboardingFeatureItem(Icons.Default.Timer, "Save 18+ minutes per dining"),
                    OnboardingFeatureItem(Icons.Default.CheckCircle, "Live dietary preference filter")
                )
            ),
            OnboardingSlideData(
                title = "Pre-order Meals.\nGrab & Go quickly.",
                subtitle = "Select your favourite dishes, schedule flexible pickup times, and pick up fresh meals instantly.",
                illustrationRes = R.drawable.onboarding_slide3,
                features = listOf(
                    OnboardingFeatureItem(Icons.Default.QrCode, "Instant digital queue tokens"),
                    OnboardingFeatureItem(Icons.Default.Fastfood, "Express kitchen pickup counters"),
                    OnboardingFeatureItem(Icons.Default.Payments, "Instant cashless UPI payments"),
                    OnboardingFeatureItem(Icons.Default.NotificationsActive, "Live order readiness alerts")
                )
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryPurple,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onFinishOnboarding() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // CTA Action Button ("Get Started" or "Continue")
                Button(
                    onClick = {
                        if (pagerState.currentPage < slides.lastIndex) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinishOnboarding()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (pagerState.currentPage == slides.lastIndex) "Get Started" else "Get Started",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3 Dots Pager Indicator (below button matching reference mockup)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(slides.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 6.dp,
                            label = "dot_width"
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(width)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isSelected) PrimaryPurple else MaterialTheme.colorScheme.outlineVariant
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            val slide = slides[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Headline & Subtitle
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = slide.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 28.sp,
                            lineHeight = 36.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = slide.subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Centered Vector Illustration with Circular Halo
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = slide.illustrationRes),
                        contentDescription = "Onboarding Illustration",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4 Feature Pills
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    slide.features.forEach { feature ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(PrimaryPurple, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = feature.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = feature.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
