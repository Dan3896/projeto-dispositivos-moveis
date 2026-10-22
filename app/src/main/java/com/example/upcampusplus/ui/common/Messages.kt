package com.example.upcampusplus.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource
import android.content.res.Resources

/** Texto a resolver na interface; o ViewModel não conhece o Context. */
data class UiText(@StringRes val id: Int, val args: List<Any> = emptyList()) {
    fun resolve(resources: Resources): String = resources.getString(id, *args.toTypedArray())
}

@Composable
fun UiText.asString(): String = stringResource(id, *args.toTypedArray())

/** Mensagem para a snackbar (RF-018), com ação opcional (RF-016: Desfazer). */
data class UserMessage(
    val text: UiText,
    val actionLabel: UiText? = null,
    val onAction: (() -> Unit)? = null,
)

/** Mostra snackbars no nível da raiz, para sobreviver à troca de tela (ex.: Salvar no formulário). */
fun interface Messenger {
    fun show(message: UserMessage)
}

val LocalMessenger = staticCompositionLocalOf<Messenger> { Messenger { } }

/** pt-BR trata 0 como singular; aqui só 1 é singular (RF-012). */
@StringRes
fun countRes(count: Int, @StringRes one: Int, @StringRes other: Int): Int = if (count == 1) one else other
