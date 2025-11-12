package com.example.uventapp.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uventapp.ui.components.BottomNavBar
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White

@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel() // Ambil ViewModel
) {
    val profile by profileViewModel.profile

    Scaffold(
        topBar = {
            // Gunakan CustomAppBar tapi beri aksi 'onBack'
            CustomAppBar(title = "Profil", onBack = { navController.popBackStack() })
        },
        bottomBar = { BottomNavBar(navController = navController) },
        containerColor = LightBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Kartu Profil Hijau (Nama)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Icon",
                        tint = White,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = profile?.name ?: "Memuat...",
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kartu Info Putih (Email & Telepon)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp) // Efek tumpuk
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 40.dp), // Padding atas lebih besar
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileInfoRow(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = profile?.email ?: "..."
                    )
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    ProfileInfoRow(
                        icon = Icons.Default.Phone,
                        label = "No Telepon",
                        value = profile?.phone ?: "..."
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 12.sp
            )
            Text(
                text = value,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
