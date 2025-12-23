package com.example.uventapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uventapp.ui.theme.DisabledBackground
import com.example.uventapp.ui.theme.DisabledText
import com.example.uventapp.ui.theme.GradientEnd
import com.example.uventapp.ui.theme.GradientStart
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val buttonShape = RoundedCornerShape(12.dp)
    
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = buttonShape,
                ambientColor = PrimaryGreen.copy(alpha = 0.3f),
                spotColor = PrimaryGreen.copy(alpha = 0.3f)
            ),
        enabled = enabled,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = White,
            disabledContainerColor = DisabledBackground,
            disabledContentColor = DisabledText
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(
                    brush = if (enabled) {
                        Brush.horizontalGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(DisabledBackground, DisabledBackground)
                        )
                    },
                    shape = buttonShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val buttonShape = RoundedCornerShape(25.dp)
    
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = buttonShape,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        enabled = enabled,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = White,
            contentColor = PrimaryGreen
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 6.dp
        ),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 18.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}
