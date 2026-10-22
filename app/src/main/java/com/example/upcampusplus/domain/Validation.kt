package com.example.upcampusplus.domain

const val TITLE_MAX_LENGTH = 100

/** O contador de caracteres aparece a partir deste tamanho (RF-004). */
const val TITLE_COUNTER_THRESHOLD = 80

/** Limita o que o campo aceita: colar 150 caracteres deixa 100 (T-F2). Vale para o formulário e a captura. */
fun limitTitleInput(raw: String): String = raw.take(TITLE_MAX_LENGTH)

/** RN-01: título válido tem 1 a 100 caracteres após remover espaços nas pontas. Nulo se inválido. */
fun normalizeTitle(raw: String): String? =
    raw.trim().takeIf { it.isNotEmpty() && it.length <= TITLE_MAX_LENGTH }
