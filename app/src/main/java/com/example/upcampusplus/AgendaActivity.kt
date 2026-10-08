package com.example.upcampusplus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// Tela "Hoje": tarefas pendentes com prazo até hoje (inclui atrasadas),
// ordenadas por prioridade.
class AgendaActivity : AppCompatActivity() {
    private lateinit var repo: TarefaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agenda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left + 24.dp, systemBars.top + 24.dp,
                systemBars.right + 24.dp, systemBars.bottom + 24.dp
            )
            insets
        }

        repo = TarefaRepository(this)

        findViewById<Button>(R.id.btnNovoEvento).setOnClickListener {
            abrirNovaTarefa(repo) { atualizar() }
        }
        findViewById<Button>(R.id.btnTodasTarefas).setOnClickListener {
            startActivity(Intent(this, TarefasActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        atualizar()
    }

    private fun atualizar() {
        val dataHoje = hoje()
        val tarefasHoje = repo.listar()
            .filter { !it.concluida && it.prazo <= dataHoje }
            .sortedWith(compareBy({ it.prioridade }, { it.prazo }))

        findViewById<TextView>(R.id.txtResumo).text = when (tarefasHoje.size) {
            0 -> "Tudo em dia."
            1 -> "1 tarefa para hoje"
            else -> "${tarefasHoje.size} tarefas para hoje"
        }
        mostrarTarefas(findViewById<LinearLayout>(R.id.listaTarefas), tarefasHoje, repo) { atualizar() }
    }

    private val Int.dp get() = (this * resources.displayMetrics.density).toInt()
}
