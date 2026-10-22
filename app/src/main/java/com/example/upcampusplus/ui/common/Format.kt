package com.example.upcampusplus.ui.common

import androidx.annotation.StringRes
import com.example.upcampusplus.R
import com.example.upcampusplus.domain.model.Priority
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val PtBr: Locale = Locale.forLanguageTag("pt-BR")

private val shortDate = DateTimeFormatter.ofPattern("dd/MM", PtBr)
private val fullDate = DateTimeFormatter.ofPattern("dd/MM/yyyy", PtBr)
private val longDate = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PtBr)
private val time = DateTimeFormatter.ofPattern("HH:mm", PtBr)

/** Prazo no cartão: dd/MM no ano corrente, dd/MM/yyyy fora dele. */
fun formatDue(date: LocalDate, today: LocalDate): String =
    if (date.year == today.year) date.format(shortDate) else date.format(fullDate)

fun formatFullDate(date: LocalDate): String = date.format(fullDate)

/** "quinta-feira, 15 de outubro" */
fun formatLongDate(date: LocalDate): String = date.format(longDate)

fun formatTime(instant: Instant, zone: ZoneId): String = instant.atZone(zone).format(time)

/** Hora de hoje; data e hora para dias anteriores. */
fun formatMoment(instant: Instant, zone: ZoneId, today: LocalDate): String {
    val local = instant.atZone(zone)
    return if (local.toLocalDate() == today) local.format(time) else "${formatDue(local.toLocalDate(), today)} ${local.format(time)}"
}

@StringRes
fun Priority.labelRes(): Int = when (this) {
    Priority.HIGH -> R.string.priority_high
    Priority.MEDIUM -> R.string.priority_medium
    Priority.LOW -> R.string.priority_low
}

