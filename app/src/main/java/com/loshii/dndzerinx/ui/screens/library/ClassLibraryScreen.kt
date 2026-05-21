package com.loshii.dndzerinx.ui.screens.library

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class LibraryTab { GuildClasses, Species }

private data class GuildClassItem(
    val name: String,
    val key: String,
    val role: String,
    val summary: String,
    val color: Color
)

private data class SpeciesItem(
    val name: String,
    val variants: List<String>,
    val size: String,
    val summary: String,
    val color: Color
)

private val guildClasses = listOf(
    GuildClassItem("Artifice", "ART", "Inventor arcano", "Crea herramientas, armas y soluciones magicas para sostener al grupo.", Color(0xFF607D8B)),
    GuildClassItem("Barbaro", "BAR", "Vanguardia", "Resiste castigo y rompe la linea enemiga con fuerza bruta.", Color(0xFFD84315)),
    GuildClassItem("Bardo", "BRD", "Apoyo social", "Mezcla inspiracion, magia y habilidades para cubrir huecos del equipo.", Color(0xFF8E24AA)),
    GuildClassItem("Clerigo", "CLR", "Canal divino", "Protege, sana y castiga con poder concedido por su dominio.", Color(0xFFF9A825)),
    GuildClassItem("Druida", "DRU", "Naturaleza", "Controla terreno, invoca fuerzas naturales y cambia de forma.", Color(0xFF43A047)),
    GuildClassItem("Combatiente", "COM", "Maestria marcial", "Domina armas, tacticas y estilos de combate consistentes.", Color(0xFF546E7A)),
    GuildClassItem("Monje", "MON", "Disciplina", "Usa movilidad, ki y golpes precisos para presionar al enemigo.", Color(0xFF00897B)),
    GuildClassItem("Paladin", "PAL", "Juramento", "Combina defensa, presencia divina y golpes explosivos.", Color(0xFF3949AB)),
    GuildClassItem("Guardabosque", "GDB", "Explorador", "Rastrea, embosca y sobrevive en territorios hostiles.", Color(0xFF689F38)),
    GuildClassItem("Picaro", "PIC", "Especialista", "Aprovecha sigilo, precision y recursos para resolver problemas.", Color(0xFF5D4037)),
    GuildClassItem("Hechicero", "HEC", "Magia innata", "Moldea conjuros desde una fuente de poder interior.", Color(0xFFE53935)),
    GuildClassItem("Brujo", "BRU", "Pacto", "Recibe secretos y poder a traves de un patron sobrenatural.", Color(0xFF6A1B9A)),
    GuildClassItem("Mago", "MAG", "Erudito arcano", "Aprende, prepara y adapta magia a casi cualquier situacion.", Color(0xFF1E88E5))
)

private val species = listOf(
    SpeciesItem("Aarakocra", listOf("Aarakocra"), "Mediana", "Ave humanoide veloz, marcada por vuelo y vida en alturas.", Color(0xFF90A4AE)),
    SpeciesItem("Aasimar", listOf("Celestial", "Protector", "Scourge", "Fallen"), "Mediana", "Linaje tocado por planos superiores, con luz interior y dones radiantes.", Color(0xFFFFB300)),
    SpeciesItem("Bugbear", listOf("Bugbear"), "Mediana", "Goblinoide alto, sigiloso y de alcance peligroso.", Color(0xFF795548)),
    SpeciesItem("Centauro", listOf("Centauro"), "Mediana", "Cuerpo equino, gran movilidad y presencia de carga.", Color(0xFF8D6E63)),
    SpeciesItem("Cambiante", listOf("Cambiante"), "Mediana", "Maestro de rostros y apariencias variables.", Color(0xFF7E57C2)),
    SpeciesItem("Dhampir", listOf("Dhampir"), "Pequena/Mediana", "Herencia vampirica, hambre sobrenatural y movilidad inquietante.", Color(0xFF6D4C41)),
    SpeciesItem("Draconido", listOf("Cromatico", "Metalico", "Gema", "Dragonborn PHB"), "Mediana", "Sangre draconica, aliento elemental y porte imponente.", Color(0xFFE53935)),
    SpeciesItem("Enano", listOf("Colina", "Montana", "Duergar"), "Mediana", "Tenaz, resistente y ligado a piedra, clan y forja.", Color(0xFF6D4C41)),
    SpeciesItem("Elfo", listOf("Alto", "Bosque", "Drow", "Eladrin", "Marino", "Shadar-kai", "Astral"), "Mediana", "Gracia longeva, sentidos finos y afinidad magica o feerica.", Color(0xFF43A047)),
    SpeciesItem("Hada", listOf("Hada"), "Pequena", "Criatura feerica ligera, curiosa y naturalmente magica.", Color(0xFFEC407A)),
    SpeciesItem("Firbolg", listOf("Firbolg"), "Mediana", "Gigante gentil, reservado y conectado con bosques antiguos.", Color(0xFF66BB6A)),
    SpeciesItem("Genasi", listOf("Aire", "Agua", "Fuego", "Tierra"), "Mediana", "Linaje elemental que expresa uno de los cuatro elementos.", Color(0xFF26A69A)),
    SpeciesItem("Gith", listOf("Githyanki", "Githzerai"), "Mediana", "Pueblo psionico dividido por guerra, disciplina y planos lejanos.", Color(0xFFFF7043)),
    SpeciesItem("Gnomo", listOf("Bosque", "Roca", "Profundo"), "Pequena", "Ingenioso, curioso y tocado por magia menor o astucia tecnica.", Color(0xFFAB47BC)),
    SpeciesItem("Goblin", listOf("Goblin"), "Pequena", "Rapido, oportunista y dificil de acorralar.", Color(0xFF7CB342)),
    SpeciesItem("Goliat", listOf("Goliat", "Nube", "Fuego", "Escarcha", "Colina", "Piedra", "Tormenta"), "Mediana", "Descendiente de gigantes, fuerte y adaptado a retos extremos.", Color(0xFF78909C)),
    SpeciesItem("Semielfo", listOf("Semielfo", "Variante"), "Mediana", "Puente entre mundos humanos y elficos, flexible y social.", Color(0xFF26C6DA)),
    SpeciesItem("Semiorco", listOf("Semiorco"), "Mediana", "Fuerte, directo y dificil de derribar.", Color(0xFF8BC34A)),
    SpeciesItem("Mediano", listOf("Piesligeros", "Robusto", "Fantasma"), "Pequena", "Pequeno, afortunado y sorprendentemente valiente.", Color(0xFFFFA726)),
    SpeciesItem("Hobgoblin", listOf("Hobgoblin"), "Mediana", "Goblinoide disciplinado, tactico y orientado al grupo.", Color(0xFFD84315)),
    SpeciesItem("Humano", listOf("Humano", "Variante", "Marca"), "Mediana", "Adaptable, ambicioso y comun en casi cualquier cultura.", Color(0xFF42A5F5)),
    SpeciesItem("Kalashtar", listOf("Kalashtar"), "Mediana", "Humanoide ligado a espiritus oniricos y fortaleza mental.", Color(0xFF5C6BC0)),
    SpeciesItem("Kenku", listOf("Kenku"), "Mediana", "Ave sin alas, imitador experto y superviviente urbano.", Color(0xFF455A64)),
    SpeciesItem("Kobold", listOf("Kobold"), "Pequena", "Draconico pequeno, astuto y eficaz en grupo.", Color(0xFFE64A19)),
    SpeciesItem("Leonin", listOf("Leonin"), "Mediana", "Felino orgulloso, fisico y de presencia intimidante.", Color(0xFFFFB74D)),
    SpeciesItem("Lizardfolk", listOf("Lizardfolk"), "Mediana", "Reptiliano practico, resistente y adaptado a pantanos.", Color(0xFF558B2F)),
    SpeciesItem("Locathah", listOf("Locathah"), "Mediana", "Pueblo acuatico resistente, hecho para vida marina.", Color(0xFF039BE5)),
    SpeciesItem("Loxodon", listOf("Loxodon"), "Mediana", "Elefantino sereno, fuerte y de memoria profunda.", Color(0xFF9E9E9E)),
    SpeciesItem("Minotauro", listOf("Minotauro"), "Mediana", "Cornamenta, embestida y presencia de laberinto viviente.", Color(0xFF795548)),
    SpeciesItem("Orco", listOf("Orco"), "Mediana", "Impulso feroz, poder fisico y cultura guerrera.", Color(0xFF689F38)),
    SpeciesItem("Owlin", listOf("Owlin"), "Pequena/Mediana", "Ave nocturna, silenciosa y con vuelo natural.", Color(0xFF5E35B1)),
    SpeciesItem("Plasmoide", listOf("Plasmoide"), "Pequena/Mediana", "Cuerpo amorfo, flexible y extrano para anatomias comunes.", Color(0xFF26C6DA)),
    SpeciesItem("Renacido", listOf("Renacido"), "Pequena/Mediana", "Existencia entre vida y muerte, con recuerdos fragmentados.", Color(0xFF757575)),
    SpeciesItem("Satiro", listOf("Satiro"), "Mediana", "Feerico festivo, agil y resistente a encantamientos.", Color(0xFFD81B60)),
    SpeciesItem("Tabaxi", listOf("Tabaxi"), "Mediana", "Felino curioso, veloz y naturalmente trepador.", Color(0xFFFBC02D)),
    SpeciesItem("Thri-kreen", listOf("Thri-kreen"), "Pequena/Mediana", "Insectoide telepatico, de multiples brazos y mente alienigena.", Color(0xFFAFB42B)),
    SpeciesItem("Tiefling", listOf("Infernal", "Abisal", "Ctonico", "Feral", "Levistus", "Zariel", "Dispater", "Glasya", "Mammon", "Mephistopheles"), "Mediana", "Herencia de planos inferiores, cuernos y magia oscura o elemental.", Color(0xFFC62828)),
    SpeciesItem("Tortle", listOf("Tortle"), "Mediana", "Caparazon natural, vida nomada y paciencia defensiva.", Color(0xFF8D6E63)),
    SpeciesItem("Triton", listOf("Triton"), "Mediana", "Guerrero marino, adaptado a profundidades y magia acuosa.", Color(0xFF0288D1)),
    SpeciesItem("Vedalken", listOf("Vedalken"), "Mediana", "Racional, preciso y orientado a mejora constante.", Color(0xFF29B6F6)),
    SpeciesItem("Verdan", listOf("Verdan"), "Pequena/Mediana", "Goblinoide cambiante, adaptable y marcado por energia caotica.", Color(0xFF9CCC65)),
    SpeciesItem("Forjado", listOf("Warforged"), "Mediana", "Constructo viviente, durable y creado para proposito.", Color(0xFF607D8B)),
    SpeciesItem("Yuan-ti", listOf("Yuan-ti"), "Mediana", "Linaje serpentino, frio y resistente a magia venenosa.", Color(0xFF33691E))
)

@Composable
fun ClassLibraryScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(LibraryTab.GuildClasses) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Biblioteca", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Clases de gremio y especies jugables", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LibraryTabButton("Clases", selectedTab == LibraryTab.GuildClasses, Modifier.weight(1f)) {
                selectedTab = LibraryTab.GuildClasses
            }
            LibraryTabButton("Especies", selectedTab == LibraryTab.Species, Modifier.weight(1f)) {
                selectedTab = LibraryTab.Species
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (selectedTab) {
                LibraryTab.GuildClasses -> items(guildClasses) { item -> GuildClassCard(item) }
                LibraryTab.Species -> items(species) { item -> SpeciesCard(item) }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

@Composable
private fun LibraryTabButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text,
            modifier = Modifier.padding(vertical = 10.dp),
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun GuildClassCard(item: GuildClassItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Sigil(item.key, item.color)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.role, color = item.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text(item.summary, color = Color.Gray, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SpeciesCard(item: SpeciesItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            SpeciesPortrait(item.name, item.color)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.size, color = item.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text(item.summary, color = Color.Gray, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("Variantes: ${item.variants.joinToString(", ")}", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun Sigil(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(9.dp)) {
            drawCircle(color, radius = size.minDimension * 0.42f, center = center, style = Stroke(width = size.minDimension * 0.08f))
            drawLine(color, Offset(size.width * 0.5f, size.height * 0.16f), Offset(size.width * 0.5f, size.height * 0.84f), strokeWidth = size.minDimension * 0.08f)
            drawLine(color, Offset(size.width * 0.16f, size.height * 0.5f), Offset(size.width * 0.84f, size.height * 0.5f), strokeWidth = size.minDimension * 0.08f)
        }
        Text(text, color = color, fontWeight = FontWeight.Black, fontSize = 11.sp)
    }
}

@Composable
private fun SpeciesPortrait(name: String, color: Color) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(color.copy(alpha = 0.42f), color.copy(alpha = 0.12f)))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            drawCircle(color.copy(alpha = 0.9f), radius = size.minDimension * 0.24f, center = Offset(size.width * 0.5f, size.height * 0.34f), style = Stroke(width = size.minDimension * 0.08f))
            drawLine(color, Offset(size.width * 0.5f, size.height * 0.56f), Offset(size.width * 0.28f, size.height * 0.86f), strokeWidth = size.minDimension * 0.08f)
            drawLine(color, Offset(size.width * 0.5f, size.height * 0.56f), Offset(size.width * 0.72f, size.height * 0.86f), strokeWidth = size.minDimension * 0.08f)
            drawLine(color, Offset(size.width * 0.34f, size.height * 0.62f), Offset(size.width * 0.66f, size.height * 0.62f), strokeWidth = size.minDimension * 0.08f)
        }
        Text(name.take(1).uppercase(), color = color, fontWeight = FontWeight.Black, fontSize = 18.sp)
    }
}
