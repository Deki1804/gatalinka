package com.gatalinka.app.ui.design

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Standardni ekran:
 * - Top sadržaj ide gore (scroll-less za sad).
 * - CTA (zrno + label) pinamo pri dnu, svugdje identično.
 */
@Composable
fun ScreenWithBottomCTA(
    topContent: @Composable ColumnScope.() -> Unit,
    ctaLabel: String,
    onCtaClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = GataUI.ScreenPadding),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gornji dio
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            topContent()
        }

        // Donji CTA blok (uvijek isto)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BeanCTA(label = ctaLabel, onClick = onCtaClick)
        }
    }
}
