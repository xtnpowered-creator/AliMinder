package com.aliminder.app.presentation.components

import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat

/**
 * A Compose wrapper around TextView to render HTML content.
 * Essential for displaying rich text descriptions from MS Graph / Google APIs.
 */
@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.material3.MaterialTheme.typography.bodyMedium
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                this.movementMethod = LinkMovementMethod.getInstance() // Make links clickable
            }
        },
        update = { textView ->
            val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
            
            // Enforce uniformity: Strip Bold formatting
            if (spanned is android.text.Spannable) {
                val styleSpans = spanned.getSpans(0, spanned.length, android.text.style.StyleSpan::class.java)
                for (span in styleSpans) {
                    if (span.style == android.graphics.Typeface.BOLD || span.style == android.graphics.Typeface.BOLD_ITALIC) {
                        spanned.removeSpan(span)
                    }
                }
                // Also check for bold-specific spans if any (though HtmlCompat usually uses StyleSpan)
            }
            
            textView.text = spanned
            textView.setTextColor(color.toArgb())
            textView.textSize = style.fontSize.value
            // TODO: Apply font family/weight from style if needed, 
            // but standard TextView defaults are usually sufficient for body text
        }
    )
}
