package com.ems.util

import com.vaadin.flow.component.UI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// Launch coroutine in background scope
fun launchUiCoroutine(block: suspend CoroutineScope.() -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        block()
    }
}

// Suspend coroutine, then run block on Vaadin UI thread
suspend fun <T> UI.withUi(block: () -> T): T = suspendCancellableCoroutine { cont ->
    access {
        try {
            val result = block()
            cont.resume(result)
        } catch (e: Exception) {
            cont.cancel(e)
        }
    }
}