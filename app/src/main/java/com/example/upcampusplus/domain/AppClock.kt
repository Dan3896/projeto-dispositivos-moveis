package com.example.upcampusplus.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Relógio injetável: testes trocam por um relógio falso (RN-11, R-03). */
interface AppClock {
    fun now(): Instant
    fun zone(): ZoneId
}

object SystemAppClock : AppClock {
    override fun now(): Instant = Instant.now()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}

/** Hoje = data de calendário no fuso atual do aparelho (RN-11). */
fun AppClock.today(): LocalDate = now().atZone(zone()).toLocalDate()

const val DAY_CHECK_INTERVAL_MS = 30_000L

/**
 * Emite a data de hoje ao ser coletado e de novo a cada [intervalMs], só quando ela muda.
 * Como as telas coletam só em primeiro plano, a data é relida ao voltar ao app
 * e a virada do dia com o app aberto aparece em menos de 1 minuto (RF-020).
 */
fun AppClock.todayFlow(intervalMs: Long = DAY_CHECK_INTERVAL_MS): Flow<LocalDate> = flow {
    while (true) {
        emit(today())
        delay(intervalMs)
    }
}.distinctUntilChanged()
