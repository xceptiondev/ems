package com.ems.util

import com.vaadin.flow.component.UI
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Safely executes a block on the UI thread with proper coroutine cancellation support
 */
suspend fun <T> executeOnUiThread(block: () -> T): T {
    val ui = UI.getCurrent() ?: error("No UI context available")

    return suspendCancellableCoroutine { continuation ->
        // Check if component is still attached
        if (ui.element.node?.isAttached == false) {
            continuation.cancel()
            return@suspendCancellableCoroutine
        }

        ui.accessSynchronously {
            try {
                continuation.resume(block())
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

        // Handle coroutine cancellation
        continuation.invokeOnCancellation {
            ui.accessSynchronously {
                // Cleanup if needed
            }
        }
    }
}