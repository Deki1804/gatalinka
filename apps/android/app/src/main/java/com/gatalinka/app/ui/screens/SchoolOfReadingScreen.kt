package com.gatalinka.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatalinka.app.ui.components.MysticBackground
import com.gatalinka.app.ui.design.GataUI

data class SchoolCard(
    val title: String,
    val content: String,
    val emoji: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolOfReadingScreen(
    onBack: () -> Unit
) {
    val cards = remember {
        listOf(
            SchoolCard(
                title = "Kako se kuha najbolja kava",
                emoji = "☕",
                content = """
                    ZA NAJBOLJU KAVU:
                    
                    1. KORISTI KVALITETNU KAVU
                    • Odaberi fino mljevenu tursku kavu
                    • Svježe mljevena kava daje najbolji talog
                    • Izbjegavaj instant kavu
                    
                    2. PRAVILNA KOLIČINA
                    • 1-2 žličice po šalici (po želji)
                    • Ne previše - talog će biti pregust
                    • Ne premalo - neće biti dovoljno simbola
                    
                    3. TEMPERATURA VODE
                    • Vruća voda, ali ne kipuća (90-95°C)
                    • Kipuća voda sprži kavu
                    • Hladna voda neće ekstrahirati dovoljno
                    
                    4. KUHANJE
                    • Kratko prokuvaj na niskoj vatri
                    • Ne miješaj dok se kuha
                    • Ostavi da se talog slegne 1-2 minute
                    
                    5. SERVIRANJE
                    • Ulij u šalicu, ostavi talog na dnu
                    • Ne pij sve - ostavi talog za čitanje!
                """.trimIndent()
            ),
            SchoolCard(
                title = "Okretanje šalice",
                emoji = "🔄",
                content = """
                    PRAVILNO OKRETANJE:
                    
                    1. POPIJ KAVU
                    • Popij kavu, ali ostavi talog na dnu
                    • Ne miješaj talog dok piješ
                    • Ostavi oko 1 cm taloga
                    
                    2. POKRI ŠALICU
                    • Pokrij šalicu tanjurčićem ili tanjurom
                    • Ovo osigurava da se talog ne rasprši
                    • Važno za dobar rezultat!
                    
                    3. OKRENI ŠALICU
                    • Okreni šalicu naglavačke (180°)
                    • Drži poklopac pritisnut
                    • Čekaj 2-3 minute da se talog slegne
                    
                    4. PAŽLJIVO OKRENI NATRAG
                    • Polako okreni šalicu natrag
                    • Ne tresi šalicu
                    • Talog će ostaviti oblike na stijenkama
                    
                    5. ANALIZIRAJ
                    • Gledaj oblike i simbole
                    • Svaki oblik ima značenje
                    • Fotkaj za AI analizu!
                """.trimIndent()
            ),
            SchoolCard(
                title = "Simboli i značenja - Osnove",
                emoji = "🔮",
                content = """
                    OSNOVNI SIMBOLI:
                    
                    LJUBAV I EMOCIJE:
                    • Srce - ljubav, romansa, emocionalna povezanost
                    • Cvijet - nova ljubav, rast, cvatnja
                    • Prsten - brak, obećanje, cjelovitost
                    • Dva srca - dublja veza, partnerstvo
                    • Ruža - strast, romansa, ljepota
                    
                    USPJEH I KARIJERA:
                    • Zvijezda - uspjeh, realizacija snova
                    • Krunica - dostignuće, priznanje
                    • Strelica - napredak, smjer, akcija
                    • Ljestve - napredak, uspon, ambicija
                    • Krunica - nagrada, uspjeh, priznanje
                    
                    PUTOVANJE I PROMJENE:
                    • Linija - putovanje, promjene u životu
                    • Most - prelazak, nova faza
                    • Put - životni put, putovanje
                    • Brod - putovanje, nova prilika
                    • Avion - brze promjene, daleka putovanja
                    
                    NOVAC I MATERIJALNO:
                    • Točkice - novac, materijalna dobra
                    • Krug - cjelovitost, ciklusi
                    • Kvadrat - stabilnost, sigurnost
                    • Novčić - financijski uspjeh
                    • Vrećica - materijalno blagostanje
                    
                    NEJASNOĆA:
                    • Oblak - nejasnoća, strpljenje
                    • Magla - neizvjesnost, čekanje
                """.trimIndent()
            ),
            SchoolCard(
                title = "Simboli i značenja - Napredno",
                emoji = "✨",
                content = """
                    NAPREDNI SIMBOLI:
                    
                    ŽIVOTINJE:
                    • Ptica - sloboda, poruka, novosti
                    • Mačka - neovisnost, intuicija
                    • Pas - vjernost, prijateljstvo
                    • Konj - snaga, energija, putovanje
                    • Zmija - transformacija, mudrost
                    • Orao - visoki ciljevi, snaga
                    
                    PRIRODA:
                    • Drvo - rast, stabilnost, život
                    • Planina - izazovi, postignuća
                    • Sunce - radost, energija, sreća
                    • Mjesec - emocije, intuicija, ciklusi
                    • Voda - emocije, čišćenje, promjene
                    • Vatra - strast, transformacija
                    
                    PREDMETI:
                    • Ključ - nova prilika, rješenje
                    • Vrata - nova faza, mogućnosti
                    • Stolica - stabilnost, odmor
                    • Stablo - porodica, korijeni
                    • Toranj - ambicija, visoki ciljevi
                    • Most - prelazak, povezivanje
                    
                    BROJEVI I OBLICI:
                    • Tri - trojstvo, balans
                    • Sedam - sreća, duhovnost
                    • Krug - cjelovitost, beskonačnost
                    • Trokut - stabilnost, snaga
                    • Spiral - rast, evolucija
                """.trimIndent()
            ),
            SchoolCard(
                title = "Primjeri šalica",
                emoji = "📸",
                content = """
                    KAKO PREPOZNATI SIMBOLE:
                    
                    DOBRA ŠALICA:
                    • Jasni, oštri oblici
                    • Dobar kontrast taloga i šalice
                    • Simboli su vidljivi i različiti
                    • Talog je ravnomjerno raspoređen
                    
                    LOŠA ŠALICA:
                    • Mutni, nejasni oblici
                    • Previše tamno ili svijetlo
                    • Talog je pregust ili prerijedak
                    • Simboli se preklapaju
                    
                    PRIMJERI SIMBOLA:
                    
                    SRCE:
                    • Jasno vidljiv oblik srca
                    • Obično na dnu ili stijenkama
                    • Može biti veliko ili malo
                    • Veliko srce = jaka ljubav
                    
                    ZVIJEZDA:
                    • Petokraka forma
                    • Često na vrhu šalice
                    • Znači uspjeh i realizaciju
                    • Više zvijezda = više sreće
                    
                    LINIJA:
                    • Duga, ravna ili zakrivljena
                    • Može ići preko cijele šalice
                    • Znači putovanje ili promjenu
                    • Prekinuta linija = prepreke
                    
                    TIPIČNI UZORCI:
                    • Oblaci na vrhu = nejasna budućnost
                    • Točkice na dnu = novac
                    • Strelica prema gore = napredak
                    • Krug = cjelovitost i balans
                    
                    SAVJET: Fotkaj šalicu u dobrom svjetlu i približi se za bolje detalje!
                """.trimIndent()
            ),
            SchoolCard(
                title = "Bapske priče - Povijest",
                emoji = "📖",
                content = """
                    TRADICIJA GATANJA IZ KAVE:
                    
                    Gatanje iz kave (turski: kahve falı) je stara tradicija koja potječe iz Osmanskog Carstva u 16. stoljeću. Babe su prenosile znanje kroz generacije, tumačeći oblike u talogu kave.
                    
                    KAKO JE SVE POČELO:
                    • Turska kava je stigla u Istanbul 1550-ih
                    • Gatanje se razvilo kao zabavna aktivnost
                    • Postalo je dio kulture i tradicije
                    
                    BABE I TRADICIJA:
                    • Babe su bile majstorice gatanja
                    • Prenosile su znanje kćerima i unukama
                    • Svaka baba imala je svoje tumačenje
                    
                    MODERNA DOBA:
                    • Tradicija se nastavlja i danas
                    • AI tehnologija pomaže u analizi
                    • Kombinacija stare mudrosti i moderne znanosti
                    
                    Svaka šalica priča jedinstvenu priču o sudbini, ljubavi i budućnosti!
                """.trimIndent()
            ),
            SchoolCard(
                title = "Bapske priče - Mudrost",
                emoji = "🧙",
                content = """
                    MUDROST BABE:
                    
                    "Šalica kave je kao knjiga - svaki oblik je stranica koja priča priču o tvom životu."
                    
                    ZLATNA PRAVILA:
                    • Čitaj šalicu u miru i tišini
                    • Ne forsiraj tumačenje - simboli će se pokazati
                    • Svaka šalica je jedinstvena
                    • Ne čitaj previše često - jednom dnevno je dovoljno
                    
                    KADA ČITATI:
                    • Ujutro - za dnevne savjete
                    • Nakon važnih događaja
                    • Kada tražiš odgovore
                    • Za zabavu s prijateljima
                    
                    ŠTO IZBJEGAVATI:
                    • Ne čitaj kada si uznemiren
                    • Ne traži samo loše znakove
                    • Ne očekuj točne datume
                    • Ne uzimaj sve doslovno
                    
                    "Gatanje nije predviđanje - to je razmišljanje o mogućnostima."
                """.trimIndent()
            ),
            SchoolCard(
                title = "Horoskopski savjeti",
                emoji = "⭐",
                content = """
                    ZNAKOVI I SIMBOLI:
                    
                    VATRENI ZNAKOVI (Ovan, Lav, Strijelac):
                    • Traži dinamične oblike, linije koje idu naprijed
                    • Zvijezde i strelice su tvoji znakovi
                    • Jaki, jasni simboli znače akciju
                    
                    ZEMLJANI ZNAKOVI (Bik, Djevica, Jarac):
                    • Fokusiraj se na stabilne oblike
                    • Kvadrati i krugovi su tvoji znakovi
                    • Simetrija znači balans
                    
                    ZRAČNI ZNAKOVI (Blizanci, Vaga, Vodenjak):
                    • Traži simetriju i balans u šalici
                    • Oblaci i magla mogu značiti promjene
                    • Linije znače komunikaciju
                    
                    VODENI ZNAKOVI (Rak, Škorpion, Ribe):
                    • Traži duboke, intenzivne oblike
                    • Srca i cvijetovi su tvoji znakovi
                    • Mekani, fluidni oblici su tvoji znakovi
                    
                    Svaki znak ima svoje karakteristične simbole!
                """.trimIndent()
            ),
            SchoolCard(
                title = "Napredni savjeti",
                emoji = "🎯",
                content = """
                    ZA NAJBOLJE REZULTATE:
                    
                    FOTOGRAFIJANJE:
                    • Fotkaj šalicu odozgo, direktno
                    • Dobra svjetlost je ključna
                    • Ukloni sve što ometa (tanjur, žličica)
                    • Šalica treba biti u centru okvira
                    
                    KVALITETA SLIKE:
                    • Jasna, oštra slika
                    • Dobar kontrast između taloga i šalice
                    • Ne previše svijetlo ili tamno
                    • Približi se šalici
                    
                    KADA FOTKATI:
                    • Nakon što se talog slegne (2-3 min)
                    • Prije nego što se talog počne raspadati
                    • U dobrom svjetlu
                    
                    TIPOVI ŠALICA:
                    • Bijele šalice su najbolje
                    • Široke šalice daju više prostora za simbole
                    • Duboke šalice daju više detalja
                    
                    AI će analizirati sve simbole i dati ti detaljno čitanje!
                """.trimIndent()
            ),
            // Novih 10 kartica s detaljnim simbolima
            SchoolCard(
                title = "Što znači ptica u šalici",
                emoji = "🕊️",
                content = """
                    PTICA - SIMBOL SLOBODE I PORUKA:
                    
                    OSNOVNO ZNAČENJE:
                    • Sloboda i neovisnost
                    • Poruka ili novosti koje dolaze
                    • Duhovna povezanost
                    • Putovanje ili promjena lokacije
                    
                    POZICIJA:
                    • Na vrhu šalice - dobre vijesti dolaze
                    • Na dnu - promjene u bliskoj budućnosti
                    • Na stijenkama - poruka od bliske osobe
                    
                    VELIČINA:
                    • Velika ptica - važna poruka
                    • Mala ptica - manje važne novosti
                    • Više ptica - više poruka ili putovanja
                    
                    "Ptica u šalici donosi poruke iz daljine."
                """.trimIndent()
            ),
            SchoolCard(
                title = "Što znači srce",
                emoji = "❤️",
                content = """
                    SRCE - SIMBOL LJUBAVI I EMOCIJA:
                    
                    OSNOVNO ZNAČENJE:
                    • Ljubav i romansa
                    • Emocionalna povezanost
                    • Bliska veza s nekim
                    • Srčani problemi (zdravlje)
                    
                    POZICIJA:
                    • Na vrhu - nova ljubav dolazi
                    • Na dnu - postojeća ljubav će se produbiti
                    • Na stijenkama - ljubav u bliskoj okolini
                    
                    VELIČINA:
                    • Veliko srce - jaka, duboka ljubav
                    • Malo srce - nova, rastuća ljubav
                    • Dva srca - partnerstvo ili brak
                    
                    "Srce u šalici govori o ljubavi koja te čeka."
                """.trimIndent()
            ),
            SchoolCard(
                title = "Što znači zvijezda",
                emoji = "⭐",
                content = """
                    ZVIJEZDA - SIMBOL USPJEHA I SREĆE:
                    
                    OSNOVNO ZNAČENJE:
                    • Uspeh i realizacija snova
                    • Sreća i blagostanje
                    • Priznanje i nagrade
                    • Duhovna svjetlost
                    
                    POZICIJA:
                    • Na vrhu - veliki uspjeh dolazi
                    • U centru - uspjeh u sadašnjosti
                    • Na stijenkama - uspjeh u određenom području
                    
                    BROJ ZVIJEZDA:
                    • Jedna - uspjeh u jednom području
                    • Više - višestruki uspjeh
                    • Petokraka - potpuna realizacija
                    
                    "Zvijezda u šalici svjetluca tvoju sudbinu."
                """.trimIndent()
            ),
            SchoolCard(
                title = "Što znači križ",
                emoji = "➕",
                content = """
                    KRIŽ - SIMBOL IZBORA I ODGOVORNOSTI:
                    
                    OSNOVNO ZNAČENJE:
                    • Važan izbor pred tobom
                    • Odgovornost i obaveze
                    • Duhovna snaga
                    • Zaštita i sigurnost
                    
                    POZICIJA:
                    • U centru - važan izbor u sadašnjosti
                    • Na stijenkama - izbor u određenom području
                    • Na vrhu - izbor koji dolazi
                    
                    VELIČINA:
                    • Velik križ - važan životni izbor
                    • Mali križ - manji izbori
                    • Više križeva - više izbora
                    
                    "Križ u šalici pokazuje put koji trebaš odabrati."
                """.trimIndent()
            ),
            SchoolCard(
                title = "Što znači put",
                emoji = "🛤️",
                content = """
                    PUT - SIMBOL PUTOVANJA I PROMJENA:
                    
                    OSNOVNO ZNAČENJE:
                    • Putovanje (fizičko ili duhovno)
                    • Promjene u životu
                    • Novi početak
                    • Životni put i smjer
                    
                    POZICIJA:
                    • Duga linija - daleko putovanje
                    • Kratka linija - kratko putovanje
                    • Zakrivljena - promjene u planovima
                    
                    SMJER:
                    • Prema gore - napredak
                    • Prema dolje - povratak
                    • Vodoravno - stabilan put
                    
                    "Put u šalici vodi te prema novim mogućnostima."
                """.trimIndent()
            ),
            SchoolCard(
                title = "Što znači grančica",
                emoji = "🌿",
                content = """
                    GRANČICA - SIMBOL RASTA I ŽIVOTA:
                    
                    OSNOVNO ZNAČENJE:
                    • Rast i razvoj
                    • Nova mogućnost
                    • Priroda i zdravlje
                    • Obnova i regeneracija
                    
                    POZICIJA:
                    • Na vrhu - rast u budućnosti
                    • U centru - rast u sadašnjosti
                    • Na stijenkama - rast u određenom području
                    
                    VELIČINA:
                    • Velika grančica - značajan rast
                    • Mala grančica - početak rasta
                    • Više grančica - rast u više područja
                    
                    "Grančica u šalici raste kao tvoja mogućnost."
                """.trimIndent()
            ),
            SchoolCard(
                title = "Što znači spirala",
                emoji = "🌀",
                content = """
                    SPIRALA - SIMBOL EVOLUCIJE I CYKLUSA:
                    
                    OSNOVNO ZNAČENJE:
                    • Evolucija i rast
                    • Ciklusi u životu
                    • Duhovna transformacija
                    • Ponavljajući obrasci
                    
                    POZICIJA:
                    • U centru - transformacija u sadašnjosti
                    • Na stijenkama - ciklusi u određenom području
                    • Na vrhu - nova faza dolazi
                    
                    SMJER:
                    • U smjeru kazaljke - pozitivan ciklus
                    • Suprotno - promjena ciklusa
                    
                    "Spirala u šalici pokazuje tvoj put evolucije."
                """.trimIndent()
            ),
            SchoolCard(
                title = "Što znači krug",
                emoji = "⭕",
                content = """
                    KRUG - SIMBOL CJELOVITOSTI I BALANSA:
                    
                    OSNOVNO ZNAČENJE:
                    • Cjelovitost i završetak
                    • Balans i harmonija
                    • Ciklusi i ponavljanje
                    • Zaštita i sigurnost
                    
                    POZICIJA:
                    • U centru - balans u sadašnjosti
                    • Na stijenkama - balans u određenom području
                    • Na vrhu - balans će doći
                    
                    VELIČINA:
                    • Velik krug - potpuna cjelovitost
                    • Mali krug - početak ciklusa
                    • Više krugova - više ciklusa
                    
                    "Krug u šalici simbolizira tvoju cjelovitost."
                """.trimIndent()
            ),
            SchoolCard(
                title = "Što znači mrlja u sredini",
                emoji = "🔴",
                content = """
                    MRLJA U SREDINI - SIMBOL FOKUSA I CENTRA:
                    
                    OSNOVNO ZNAČENJE:
                    • Fokus na centru života
                    • Važnost sadašnjeg trenutka
                    • Duhovna povezanost
                    • Snaga i stabilnost
                    
                    VELIČINA:
                    • Velika mrlja - jak fokus
                    • Mala mrlja - početak fokusa
                    • Više mrlja - više fokusa
                    
                    OBLIK:
                    • Okrugla - balans i harmonija
                    • Nepravilna - promjene u fokusu
                    • Razlivena - širenje utjecaja
                    
                    "Mrlja u sredini je tvoj centar snage."
                """.trimIndent()
            ),
            SchoolCard(
                title = "Što znači znak na rubu šalice",
                emoji = "🔲",
                content = """
                    ZNAK NA RUBU - SIMBOL GRANICA I MOGUĆNOSTI:
                    
                    OSNOVNO ZNAČENJE:
                    • Granice i ograničenja
                    • Mogućnosti na rubu
                    • Promjene koje dolaze
                    • Prelazak u novu fazu
                    
                    POZICIJA:
                    • Na gornjem rubu - promjene dolaze
                    • Na donjem rubu - promjene u prošlosti
                    • Na bočnim rubovima - promjene u okolini
                    
                    VELIČINA:
                    • Velik znak - važne promjene
                    • Mali znak - manje promjene
                    • Više znakova - više promjena
                    
                    "Znak na rubu šalice pokazuje granice i mogućnosti."
                """.trimIndent()
            )
        )
    }

    var currentPage by remember { mutableStateOf(0) }

    MysticBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        "Škola gatanja",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFFFD700)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Nazad",
                            tint = Color(0xFFEFE3D1)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
            // Card content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            if (dragAmount > 50 && currentPage > 0) {
                                currentPage--
                            } else if (dragAmount < -50 && currentPage < cards.size - 1) {
                                currentPage++
                            }
                        }
                    }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2D1B4E).copy(alpha = 0.9f)
                    )
                ) {
                    val scrollState = rememberScrollState()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = cards[currentPage].emoji,
                            fontSize = 64.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = cards[currentPage].title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        Text(
                            text = cards[currentPage].content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFEFE3D1),
                            textAlign = TextAlign.Justify,
                            lineHeight = 24.sp
                        )
                        
                        // Extra padding at bottom for better scrolling
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Page indicator and navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentPage > 0) {
                            currentPage--
                        }
                    },
                    enabled = currentPage > 0
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, 
                        "Prethodna",
                        tint = if (currentPage > 0) Color(0xFFFFD700) else Color(0xFFEFE3D1).copy(alpha = 0.3f)
                    )
                }

                // Page dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(cards.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (currentPage == index) 12.dp else 8.dp)
                                .background(
                                    color = if (currentPage == index)
                                        Color(0xFFFFD700)
                                    else
                                        Color(0xFFEFE3D1).copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(50)
                                )
                        )
                    }
                }

                IconButton(
                    onClick = {
                        if (currentPage < cards.size - 1) {
                            currentPage++
                        }
                    },
                    enabled = currentPage < cards.size - 1
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward, 
                        "Sljedeća",
                        tint = if (currentPage < cards.size - 1) Color(0xFFFFD700) else Color(0xFFEFE3D1).copy(alpha = 0.3f)
                    )
                }
            }
            }
        }
    }
}
