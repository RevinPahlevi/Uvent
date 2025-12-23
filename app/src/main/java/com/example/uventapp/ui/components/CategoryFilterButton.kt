package com.example.uventapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White

/**
 * Shared component untuk filter kategori event
 * Digunakan di EventListScreen dan MyRegisteredEventScreen
 * 
 * Ukuran dan spacing sudah dioptimalkan agar semua kategori
 * (Semua, Seminar, Workshop, Talkshow, Skill Lab) terlihat penuh
 */
@Composable
fun CategoryFilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryGreen else White,
            contentColor = if (isSelected) White else Color.Gray
        ),
        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 1.dp
        ),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
