package com.filmcatalog.kmp.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.filmcatalog.kmp.data.model.Movie
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleMovieModal(
    movie: Movie,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSchedule: (LocalDateTime) -> Unit,
    onDelete: (() -> Unit)? = null,
    initialDateTime: LocalDateTime? = null,
    isEditMode: Boolean = false
) {
    if (!isVisible) return
    
    val currentDateTime = initialDateTime ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    var selectedDate by remember(initialDateTime) { mutableStateOf(currentDateTime.date) }
    var selectedHour by remember(initialDateTime) { mutableStateOf(currentDateTime.hour) }
    var selectedMinute by remember(initialDateTime) { mutableStateOf(currentDateTime.minute) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(24.dp)) // Para equilibrar o layout
                    
                    Text(
                        text = if (isEditMode) "Editar Agendamento" else "Agendar Filme",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    if (isEditMode && onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir agendamento",
                                tint = Color(0xFFFF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(24.dp))
                    }
                }
                
                Text(
                    text = movie.title,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Date Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Data",
                        tint = Color(0xFFE50914),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Data",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                                containerColor = Color.Transparent
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2A2A2A))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = formatDate(selectedDate),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Column {
                        TextButton(
                            onClick = {
                                selectedDate = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .date
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFE50914)
                            )
                        ) {
                            Text("Hoje", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = {
                                selectedDate = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .date
                                    .plus(1, DateTimeUnit.DAY)
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFE50914)
                            )
                        ) {
                            Text("Amanhã", fontSize = 12.sp)
                        }
                    }
                }
                
                Divider(color = Color(0xFF2A2A2A))
                
                // Time Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Horário",
                        tint = Color(0xFFE50914),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Horário",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Hour picker
                            TimePickerField(
                                value = selectedHour,
                                onValueChange = { selectedHour = it.coerceIn(0, 23) },
                                label = "Hora",
                                maxDigits = 2
                            )
                            Text(
                                text = ":",
                                color = Color.White,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            // Minute picker
                            TimePickerField(
                                value = selectedMinute,
                                onValueChange = { selectedMinute = it.coerceIn(0, 59) },
                                label = "Min",
                                maxDigits = 2
                            )
                        }
                    }
                    
                    // Quick time buttons
                    Column {
                        TextButton(
                            onClick = { selectedHour = 20; selectedMinute = 0 },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFE50914)
                            )
                        ) {
                            Text("20:00", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = { selectedHour = 21; selectedMinute = 30 },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFE50914)
                            )
                        ) {
                            Text("21:30", fontSize = 12.sp)
                        }
                    }
                }
                
                Divider(color = Color(0xFF2A2A2A))
                
                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            val dateTime = LocalDateTime(
                                selectedDate,
                                LocalTime(selectedHour, selectedMinute)
                            )
                            onSchedule(dateTime)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE50914)
                        )
                    ) {
                        Text(if (isEditMode) "Atualizar" else "Agendar")
                    }
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun TimePickerField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    maxDigits: Int
) {
    OutlinedTextField(
        value = value.toString().padStart(maxDigits, '0'),
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() }
            if (filtered.length <= maxDigits) {
                onValueChange(filtered.toIntOrNull() ?: 0)
            }
        },
        modifier = Modifier.width(60.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFFE50914),
            unfocusedBorderColor = Color(0xFF2A2A2A)
        ),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        ),
        singleLine = true
    )
}

@Composable
private fun DatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Selecionar Data",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Simple date selection grid
                DateSelectionGrid(
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = { onDateSelected(selectedDate) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE50914)
                        )
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSelectionGrid(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    Column {
        repeat(7) { weekOffset ->
            val date = today.plus(weekOffset, DateTimeUnit.DAY)
            val isSelected = date == selectedDate
            
            OutlinedButton(
                onClick = { onDateSelected(date) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isSelected) Color.White else Color.Gray,
                    containerColor = if (isSelected) Color(0xFFE50914) else Color.Transparent
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isSelected) Color(0xFFE50914) else Color(0xFF2A2A2A)
                    )
                )
            ) {
                Text(
                    text = formatDateWithDay(date),
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun formatDate(date: LocalDate): String {
    val months = arrayOf(
        "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
        "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    )
    
    return "${date.dayOfMonth} ${months[date.monthNumber - 1]}, ${date.year}"
}

private fun formatDateWithDay(date: LocalDate): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val months = arrayOf(
        "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
        "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    )
    
    val dayOfWeek = when (date.dayOfWeek.value) {
        1 -> "Seg"
        2 -> "Ter" 
        3 -> "Qua"
        4 -> "Qui"
        5 -> "Sex"
        6 -> "Sáb"
        7 -> "Dom"
        else -> "???"
    }
    
    return when {
        date == today -> "Hoje - ${date.dayOfMonth} ${months[date.monthNumber - 1]}"
        date == today.plus(1, DateTimeUnit.DAY) -> "Amanhã - ${date.dayOfMonth} ${months[date.monthNumber - 1]}"
        else -> "$dayOfWeek - ${date.dayOfMonth} ${months[date.monthNumber - 1]}"
    }
}