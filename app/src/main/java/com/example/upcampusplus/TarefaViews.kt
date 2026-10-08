package com.example.upcampusplus

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Paint
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.Locale

private val CORES_PRIORIDADE = listOf(
    R.color.prioridade_alta,
    R.color.prioridade_media,
    R.color.prioridade_baixa
)

// Desenha as tarefas dentro do LinearLayout. Marcar o checkbox muda o status;
// segurar o dedo na tarefa remove.
fun Activity.mostrarTarefas(
    container: LinearLayout,
    tarefas: List<Tarefa>,
    repo: TarefaRepository,
    aoMudar: () -> Unit
) {
    container.removeAllViews()
    if (tarefas.isEmpty()) {
        container.addView(TextView(this).apply {
            text = "Nada por aqui. Aproveite!"
            setPadding(0, 32, 0, 0)
        })
        return
    }

    val dataHoje = hoje()
    for (tarefa in tarefas) {
        val item = layoutInflater.inflate(R.layout.item_tarefa, container, false)
        item.findViewById<android.view.View>(R.id.corPrioridade)
            .setBackgroundColor(ContextCompat.getColor(this, CORES_PRIORIDADE[tarefa.prioridade]))

        val titulo = item.findViewById<TextView>(R.id.txtTitulo)
        titulo.text = tarefa.titulo
        if (tarefa.concluida) {
            titulo.paintFlags = titulo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        val atrasada = !tarefa.concluida && tarefa.prazo < dataHoje
        item.findViewById<TextView>(R.id.txtDetalhes).text =
            "${tarefa.area} · ${PRIORIDADES[tarefa.prioridade]} · " +
                formatarPrazo(tarefa.prazo) + if (atrasada) " · ATRASADA" else ""

        val check = item.findViewById<CheckBox>(R.id.chkConcluida)
        check.isChecked = tarefa.concluida
        check.setOnCheckedChangeListener { _, marcada ->
            repo.atualizar(tarefa.copy(concluida = marcada))
            aoMudar()
        }

        item.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Remover tarefa?")
                .setMessage(tarefa.titulo)
                .setPositiveButton("Remover") { _, _ ->
                    repo.remover(tarefa.id)
                    aoMudar()
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }
        container.addView(item)
    }
}

fun Activity.abrirNovaTarefa(repo: TarefaRepository, aoSalvar: () -> Unit) {
    val view = layoutInflater.inflate(R.layout.dialog_nova_tarefa, null)
    val edtTitulo = view.findViewById<EditText>(R.id.edtTitulo)
    val spnArea = view.findViewById<Spinner>(R.id.spnArea)
    val spnPrioridade = view.findViewById<Spinner>(R.id.spnPrioridade)
    val btnPrazo = view.findViewById<Button>(R.id.btnPrazo)

    spnArea.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, AREAS)
    spnPrioridade.adapter =
        ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, PRIORIDADES)
    spnPrioridade.setSelection(1)

    var prazo = hoje()
    btnPrazo.text = "Prazo: ${formatarPrazo(prazo)}"
    btnPrazo.setOnClickListener {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, ano, mes, dia ->
            prazo = String.format(Locale.US, "%04d-%02d-%02d", ano, mes + 1, dia)
            btnPrazo.text = "Prazo: ${formatarPrazo(prazo)}"
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    AlertDialog.Builder(this)
        .setTitle("Nova tarefa")
        .setView(view)
        .setPositiveButton("Salvar") { _, _ ->
            val titulo = edtTitulo.text.toString().trim()
            if (titulo.isEmpty()) {
                Toast.makeText(this, "Digite um título", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            repo.adicionar(
                Tarefa(
                    id = System.currentTimeMillis(),
                    titulo = titulo,
                    area = AREAS[spnArea.selectedItemPosition],
                    prioridade = spnPrioridade.selectedItemPosition,
                    prazo = prazo
                )
            )
            aoSalvar()
        }
        .setNegativeButton("Cancelar", null)
        .show()
}
