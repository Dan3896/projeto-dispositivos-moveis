package com.example.upcampusplus

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val AREAS = listOf("Faculdade", "Trabalho", "Projetos", "Pessoal")
val PRIORIDADES = listOf("Alta", "Média", "Baixa")

// Prazo no formato yyyy-MM-dd: comparar as strings já compara as datas.
data class Tarefa(
    val id: Long,
    val titulo: String,
    val area: String,
    val prioridade: Int, // índice em PRIORIDADES (0 = Alta)
    val prazo: String,
    val concluida: Boolean = false
)

fun hoje(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

fun formatarPrazo(prazo: String): String {
    val data = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(prazo) ?: return prazo
    return SimpleDateFormat("dd/MM/yyyy", Locale.US).format(data)
}

// Persistência local simples para o protótipo (SharedPreferences + JSON).
// Na versão final, trocar por Room, como previsto na proposta.
class TarefaRepository(context: Context) {
    private val prefs = context.getSharedPreferences("tarefas", Context.MODE_PRIVATE)

    fun listar(): List<Tarefa> {
        val json = JSONArray(prefs.getString("lista", "[]"))
        return (0 until json.length()).map { i ->
            val o = json.getJSONObject(i)
            Tarefa(
                id = o.getLong("id"),
                titulo = o.getString("titulo"),
                area = o.getString("area"),
                prioridade = o.getInt("prioridade"),
                prazo = o.getString("prazo"),
                concluida = o.getBoolean("concluida")
            )
        }
    }

    fun adicionar(tarefa: Tarefa) = salvar(listar() + tarefa)

    fun atualizar(tarefa: Tarefa) = salvar(listar().map { if (it.id == tarefa.id) tarefa else it })

    fun remover(id: Long) = salvar(listar().filter { it.id != id })

    private fun salvar(tarefas: List<Tarefa>) {
        val json = JSONArray()
        tarefas.forEach {
            json.put(
                JSONObject()
                    .put("id", it.id)
                    .put("titulo", it.titulo)
                    .put("area", it.area)
                    .put("prioridade", it.prioridade)
                    .put("prazo", it.prazo)
                    .put("concluida", it.concluida)
            )
        }
        prefs.edit().putString("lista", json.toString()).apply()
    }
}
