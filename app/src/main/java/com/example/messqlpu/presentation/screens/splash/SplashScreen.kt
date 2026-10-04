package com.example.messqlpu.presentation.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messqlpu.R
import com.example.messqlpu.ui.theme.PrimaryPurple
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2500) // Auto navigate after 2.5 seconds as specified
        onNavigateNext()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC))
            .clickable { onNavigateNext() }
    ) {
        // High Definition LPU Campus Illustration Background
        Image(
            painter = painterResource(id = R.drawable.splash_campus_bg),
            contentDescription = "LPU Campus Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )

        // Soft gradient overlay at top to melt sky softly into the white background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.48f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8F9FC),
                            Color(0xFFF8F9FC).copy(alpha = 0.92f),
                            Color(0xFFF8F9FC).copy(alpha = 0.55f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Top Foreground Branding (Icon, Title, Subtitle)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 36.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 3D Mascot Logo Image
            Image(
                painter = painterResource(id = R.drawable.messq_mascot),
                contentDescription = "MessQ Mascot",
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(26.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App Name: MessQ LPU
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    ) {
                        append("MessQ ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    ) {
                        append("LPU")
                    }
                },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline: "Your LPU dining, without the queue."
            Text(
                text = "Your LPU dining, without the queue.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                textAlign = TextAlign.Center
            )
        }

        // Campus Slogan printed on the Walkway: "Good Food \n Brighter Days \n Happier You"
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 58.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Good Food\nBrighter Days\nHappier You",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF6C4CF1).copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.3.sp
                )
            )
        }
    }
}

/**
 * Custom Fork and Spoon logo matching the reference design:
 * Left side: 4-tine fork with handle
 * Right side: Oval spoon with handle
 */
@Composable
fun SplashCutleryLogo(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Fork on the left half (x approx 0.08w to 0.42w)
        val forkCenterX = w * 0.25f
        val forkTineTop = h * 0.12f
        val forkTineBottom = h * 0.46f
        val forkHandleBottom = h * 0.88f
        val forkWidth = w * 0.26f
        val tineCount = 4
        val tineSpacing = forkWidth / (tineCount - 1)
        val tineStroke = 2.4.dp.toPx()

        // 4 Tines
        for (i in 0 until tineCount) {
            val tineX = (forkCenterX - forkWidth / 2) + (i * tineSpacing)
            drawLine(
                color = tint,
                start = Offset(tineX, forkTineTop),
                end = Offset(tineX, forkTineBottom),
                strokeWidth = tineStroke,
                cap = StrokeCap.Round
            )
        }

        // Fork curved base bridge connecting tines
        val forkBasePath = Path().apply {
            moveTo(forkCenterX - forkWidth / 2, forkTineBottom)
            quadraticTo(
                forkCenterX, forkTineBottom + (h * 0.08f),
                forkCenterX + forkWidth / 2, forkTineBottom
            )
        }
        drawPath(
            path = forkBasePath,
            color = tint,
            style = Stroke(width = tineStroke, cap = StrokeCap.Round)
        )

        // Fork stem / handle
        drawLine(
            color = tint,
            start = Offset(forkCenterX, forkTineBottom + (h * 0.04f)),
            end = Offset(forkCenterX, forkHandleBottom),
            strokeWidth = 3.2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Spoon on the right half (x approx 0.58w to 0.92w)
        val spoonCenterX = w * 0.73f
        val spoonBowlTop = h * 0.12f
        val spoonBowlWidth = w * 0.28f
        val spoonBowlHeight = h * 0.40f
        val spoonHandleBottom = h * 0.88f

        // Spoon bowl (smooth oval)
        drawOval(
            color = tint,
            topLeft = Offset(spoonCenterX - spoonBowlWidth / 2, spoonBowlTop),
            size = Size(spoonBowlWidth, spoonBowlHeight),
            style = Fill
        )

        // Spoon stem / handle
        drawLine(
            color = tint,
            start = Offset(spoonCenterX, spoonBowlTop + spoonBowlHeight - (h * 0.05f)),
            end = Offset(spoonCenterX, spoonHandleBottom),
            strokeWidth = 3.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
