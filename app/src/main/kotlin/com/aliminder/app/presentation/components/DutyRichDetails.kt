package com.aliminder.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliminder.app.domain.model.Attendee
import com.aliminder.app.domain.model.ChecklistItem

/**
 * Section displaying the Organizer of an event.
 */
@Composable
fun OrganizerSection(organizer: Attendee) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ORGANIZER",
            style = MaterialTheme.typography.labelSmall,
            color = androidx.compose.ui.graphics.Color(0xFF81D4FA), // Light Blue
            fontWeight = FontWeight.Bold,
            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = organizer.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal // Was Medium
            )
        }
    }
}

/**
 * Section displaying the list of Attendees.
 */
@Composable
fun AttendeesSection(attendees: List<Attendee>) {
    if (attendees.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ATTENDEES (${attendees.size})",
            style = MaterialTheme.typography.labelSmall,
            color = androidx.compose.ui.graphics.Color(0xFF81D4FA), // Light Blue
            fontWeight = FontWeight.Bold,
            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        attendees.forEach { attendee ->
           Row(
               verticalAlignment = Alignment.CenterVertically,
               modifier = Modifier.padding(vertical = 2.dp)
           ) {
               Icon(
                   imageVector = Icons.Outlined.Group, // Or utilize distinct icons for status?
                   contentDescription = null,
                   tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                   modifier = Modifier.size(16.dp)
               )
               Spacer(modifier = Modifier.width(8.dp))
               Text(
                   text = attendee.name + (if (attendee.isOrganizer) " (Organizer)" else ""),
                   style = MaterialTheme.typography.bodyMedium, // Was bodySmall, bumped to match others
                   color = MaterialTheme.colorScheme.onSurface
               )
           }
        }
    }
}

/**
 * Section displaying a Checklist/Subtasks.
 */
@Composable
fun ChecklistSection(
    checklist: List<ChecklistItem>,
    onToggleItem: (String) -> Unit // New callback
) {
    if (checklist.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "CHECKLIST",
            style = MaterialTheme.typography.labelSmall,
            color = androidx.compose.ui.graphics.Color(0xFF81D4FA), // Light Blue
            fontWeight = FontWeight.Bold,
            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        checklist.forEach { item ->
            Row(
                verticalAlignment = Alignment.Top, // Align top for multi-line text
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleItem(item.id) } // Make row Interactive
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = if (item.isCompleted) "Completed" else "Pending",
                    tint = if (item.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (item.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null // Visual feedback
                )
            }
        }
    }
}
