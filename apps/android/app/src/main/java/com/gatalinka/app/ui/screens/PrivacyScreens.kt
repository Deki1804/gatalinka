package com.gatalinka.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import com.gatalinka.app.ui.components.MysticBackground
import com.gatalinka.app.ui.design.GataUI

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val privacyPolicyUrl = "https://deki1804.github.io/gatalinka/privacy-policy.html"
    
    LegalTextScaffold(
        title = "Politika privatnosti",
        onBack = onBack,
        webUrl = privacyPolicyUrl
    ) {
        Text(
            text =
                "Ova aplikacija koristi tvoje podatke (e‑mail, datum rođenja, spol) " +
                    "isključivo za personalizaciju gatanja i čuvanje povijesti čitanja na tvom računu.\n\n" +
                    "Podaci se sigurno pohranjuju putem Google/Firebase usluga. " +
                    "Ne prodajemo i ne dijelimo tvoje osobne podatke trećim stranama u marketinške svrhe.\n\n" +
                    "Sliku šalice šalješ isključivo za trenutnu AI analizu; slike se ne koriste za treniranje " +
                    "modela niti za dodatne svrhe izvan prikazivanja čitanja u aplikaciji.\n\n" +
                    "Svoj račun možeš obrisati u postavkama profila (ili nas kontaktirati putem " +
                    "store kontakt e‑mail adrese) i trajno ćemo ukloniti tvoja spremljena čitanja.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFEFE3D1),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun TermsOfUseScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val termsOfUseUrl = "https://deki1804.github.io/gatalinka/terms-of-use.html"
    
    LegalTextScaffold(
        title = "Uvjeti korištenja",
        onBack = onBack,
        webUrl = termsOfUseUrl
    ) {
        Text(
            text =
                "Aplikacija Gatalinka služi isključivo za zabavu i osobni uvid. " +
                    "Sva tumačenja šalice kave, poruke i savjeti imaju simboličan karakter " +
                    "i ne predstavljaju profesionalni psihološki, medicinski ili financijski savjet.\n\n" +
                    "Korištenjem aplikacije potvrđuješ da imaš najmanje 16 godina i da nećeš " +
                    "zloupotrebljavati aplikaciju, uključujući učitavanje neprimjerenih ili ilegalnih sadržaja.\n\n" +
                    "Zadržavamo pravo izmjene funkcionalnosti i ovih uvjeta. Promjene ćemo objaviti " +
                    "u ažuriranoj verziji aplikacije. Nastavkom korištenja prihvaćaš nove uvjete.\n\n" +
                    "Ako se ne slažeš s ovim pravilima, nemoj koristiti aplikaciju i obriši svoj račun.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFEFE3D1),
            textAlign = TextAlign.Start
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalTextScaffold(
    title: String,
    onBack: () -> Unit,
    webUrl: String? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    MysticBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFFFD700)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Nazad",
                                tint = Color(0xFFFFD700)
                            )
                        }
                    },
                    actions = {
                        if (webUrl != null) {
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = "Otvori u browseru",
                                    tint = Color(0xFFFFD700)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = GataUI.ScreenPadding, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                content()
                
                // Link na web verziju na dnu
                if (webUrl != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Otvori u browseru",
                            color = Color(0xFFFFD700),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}


