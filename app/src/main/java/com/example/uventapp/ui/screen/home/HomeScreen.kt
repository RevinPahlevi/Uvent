package com.example.uventapp.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.uventapp.R
import com.example.uventapp.ui.components.BottomNavBar
import com.example.uventapp.ui.components.SecondaryButton
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White

private val PLACEHOLDER_LOGO = R.drawable.uvent_logo
private val PLACEHOLDER_BACKGROUND = R.drawable.home_background_image

@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = PLACEHOLDER_BACKGROUND),
                contentDescription = "Background Kampus",
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
            )

            Image(
                painter = painterResource(id = PLACEHOLDER_LOGO),
                contentDescription = "Logo Kampus",
                modifier = Modifier
                    .padding(start = 24.dp, top = 16.dp)
                    .size(50.dp)
                    .align(Alignment.TopStart)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(White)
            ) {
                // --- PERUBAHAN: Ganti Teks dengan Gambar 'u.png' ---
                Image(
                    painter = painterResource(id = R.drawable.u),
                    contentDescription = "U Logo",
                    modifier = Modifier.size(60.dp) // Sesuaikan ukuran agar pas di lingkaran
                )
                // -----------------------------------------------
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-20).dp)
                    .clip(CustomGreenClipShape())
                    .background(PrimaryGreen),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Selamat Datang..",
                        color = White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ayo Lihat dan Daftarkan Diri ke\nEvent yang Tersedia!",
                        color = White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    SecondaryButton(
                        text = "Lihat Semua Event",
                        onClick = { navController.navigate(Screen.EventList.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    )

                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}

private class CustomGreenClipShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(
            path = Path().apply {
                moveTo(0f, size.height * 0.2f)
                cubicTo(
                    size.width / 4f, 0f,
                    size.width * 3f / 4f, 0f,
                    size.width, size.height * 0.2f
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
        )
    }
}
