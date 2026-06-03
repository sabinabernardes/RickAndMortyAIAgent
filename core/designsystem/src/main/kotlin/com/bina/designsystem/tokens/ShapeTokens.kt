package com.bina.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object ShapeTokens {
    val Small = RoundedCornerShape(4.dp)
    val Medium = RoundedCornerShape(8.dp)
    val Large = RoundedCornerShape(16.dp)

    val DefaultShapes = Shapes(
        small = Small,
        medium = Medium,
        large = Large
    )
}
