package com.example.upcampusplus

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// Tela "Tarefas": todas as tarefas, com filtro por área.
// Pendentes primeiro, depois por prazo e prioridade.
class TarefasActivity : AppCompatActivity() {
    private lateinit var repo: TarefaRepository
    private lateinit var spnFiltro: Spinner
    private val filtros = listOf("Todas as áreas") + AREAS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tarefas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left + 24.dp, systemBars.top + 24.dp,
                systemBars.right + 24.dp, systemBars.bottom + 24.dp
            )
            insets
        }

        repo = TarefaRepository(this)

        spnFiltro = findViewById(R.id.spnFiltroArea)
        spnFiltro.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filtros)
        spnFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) = atualizar()
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        findViewById<Button>(R.id.btnNovaTarefa).setOnClickListener {
            abrirNovaTarefa(repo) { atualizar() }
        }
    }

    override fun onResume() {
        super.onResume()
        atualizar()
    }

    private fun atualizar() {
        val area = filtros[spnFiltro.selectedItemPosition]
        val tarefas = repo.listar()
            .filter { area == filtros[0] || it.area == area }
            .sortedWith(compareBy({ it.concluida }, { it.prazo }, { it.prioridade }))
        mostrarTarefas(findViewById<LinearLayout>(R.id.listaTarefas), tarefas, repo) { atualizar() }
    }

    private val Int.dp get() = (this * resources.displayMetrics.density).toInt()
}
