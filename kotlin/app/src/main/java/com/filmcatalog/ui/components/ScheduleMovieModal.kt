package com.filmcatalog.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.filmcatalog.data.model.Movie
import com.filmcatalog.data.model.MovieSchedule
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleMovieModal(
    visible: Boolean,
    movie: Movie?,
    existingSchedule: MovieSchedule? = null,
    onDismiss: () -> Unit,
    onSchedule: (Date, String?, Boolean) -> Unit,
    onUpdate: ((String, Date, String?) -> Unit)? = null,
    onRemove: ((String) -> Unit)? = null,
    loading: Boolean = false
) {
    if (!visible || movie == null) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var notes by remember { mutableStateOf("") }
    var addToCalendar by remember { mutableStateOf(true) }
    val isEditing = existingSchedule != null

    // Inicializa dados quando o modal abre
    LaunchedEffect(visible, existingSchedule) {
        if (visible) {
            if (existingSchedule != null) {
                selectedDate = Calendar.getInstance().apply {
                    timeInMillis = existingSchedule.scheduledDate
                }
                notes = existingSchedule.notes ?: ""
            } else {
                // Define para amanhã às 20:00
                selectedDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 20)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                notes = ""
            }
            addToCalendar = true
        }
    }

    val formatDate = { date: Calendar ->
        val sdf = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
        sdf.format(date.time)
    }

    val formatTime = { date: Calendar ->
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(date.time)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1A))
                    .statusBarsPadding()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = if (isEditing) "Editar Agendamento" else "Agendar Filme",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (isEditing && onRemove != null && existingSchedule != null) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    onRemove(existingSchedule.id)
                                    onDismiss()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir",
                                tint = Color.Red
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    // Movie Info
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2A2A)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = movie.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            if (movie.releaseDate.isNotEmpty()) {
                                Text(
                                    text = movie.releaseDate.take(4),
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    // Date Selection
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2A2A)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Data",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                selectedDate.set(year, month, dayOfMonth)
                                            },
                                            selectedDate.get(Calendar.YEAR),
                                            selectedDate.get(Calendar.MONTH),
                                            selectedDate.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                                    .background(
                                        Color(0xFF3A3A3A),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Data",
                                    tint = Color(0xFFFF6B35),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = formatDate(selectedDate),
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Time Selection
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2A2A)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Horário",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        TimePickerDialog(
                                            context,
                                            { _, hourOfDay, minute ->
                                                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                                selectedDate.set(Calendar.MINUTE, minute)
                                            },
                                            selectedDate.get(Calendar.HOUR_OF_DAY),
                                            selectedDate.get(Calendar.MINUTE),
                                            true
                                        ).show()
                                    }
                                    .background(
                                        Color(0xFF3A3A3A),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Horário",
                                    tint = Color(0xFFFF6B35),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = formatTime(selectedDate),
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Notes
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2A2A)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Notas (opcional)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { if (it.length <= 200) notes = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        "Ex: Assistir com amigos, preparar pipoca...",
                                        color = Color.Gray
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFF6B35),
                                    unfocusedBorderColor = Color(0xFF3A3A3A)
                                ),
                                maxLines = 3,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                            )

                            Text(
                                text = "${notes.length}/200",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    // Calendar Option (only for new schedules)
                    if (!isEditing) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2A2A2A)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { addToCalendar = !addToCalendar }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = addToCalendar,
                                    onCheckedChange = { addToCalendar = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFFF6B35),
                                        uncheckedColor = Color.Gray
                                    )
                                )
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(
                                        text = "Adicionar ao Calendário",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Cria um lembrete no calendário do seu celular",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Button
                Button(
                    onClick = {
                        scope.launch {
                            if (isEditing && onUpdate != null && existingSchedule != null) {
                                onUpdate(existingSchedule.id, selectedDate.time, notes)
                            } else {
                                onSchedule(selectedDate.time, notes, addToCalendar)
                            }
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B35)
                    ),
                    enabled = !loading
                ) {
                    Text(
                        text = if (loading) {
                            "Salvando..."
                        } else if (isEditing) {
                            "Atualizar"
                        } else {
                            "Agendar"
                        },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}