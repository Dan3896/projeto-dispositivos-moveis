package com.example.upcampusplus.domain.model

import java.time.Instant
import java.time.LocalDate

/** Áreas fixas do MVP (RN-10). Os ids são estáveis e gravados no banco. */
data class Area(val id: Int, val name: String, val sortOrder: Int) {
    companion object {
        const val FACULDADE = 1
        const val TRABALHO = 2
        const val PROJETOS = 3
        const val PESSOAL = 4

        val DEFAULTS = listOf(
            Area(FACULDADE, "Faculdade", 0),
            Area(TRABALHO, "Trabalho", 1),
            Area(PROJETOS, "Projetos", 2),
            Area(PESSOAL, "Pessoal", 3),
        )
    }
}

/** Prioridade gravada como inteiro: 0 Alta, 1 Média, 2 Baixa. */
enum class Priority(val code: Int) {
    HIGH(0), MEDIUM(1), LOW(2);

    companion object {
        fun fromCode(code: Int): Priority = entries.first { it.code == code }
    }
}

/**
 * Tarefa única para Hoje, Todas e Inbox (PRD do Inbox, seção 7). A organização é independente
 * da conclusão: não organizada = está no Inbox; só tarefa organizada pode ser concluída (RN-20).
 */
data class Task(
    val id: Long,
    val title: String,
    /** Nulo só em tarefa não organizada (RN-21). */
    val areaId: Int?,
    val priority: Priority,
    /** Data de execução; opcional. Sem data, a tarefa não entra em Hoje (RN-08). */
    val dueDate: LocalDate?,
    val organized: Boolean,
    val completedAt: Instant?,
    val completedOn: LocalDate?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val isCompleted: Boolean get() = completedAt != null
    val isInInbox: Boolean get() = !organized
}

/** Campos editáveis de uma tarefa (formulário). Para criar ou organizar, a área é obrigatória. */
data class TaskDraft(
    val title: String,
    val areaId: Int?,
    val priority: Priority,
    val dueDate: LocalDate?,
)

/** Resultado de concluir: se a conclusão foi gravada agora e se rendeu XP. */
data class CompletionResult(val completed: Boolean, val xpGained: Int)

const val XP_PER_TASK = 10
