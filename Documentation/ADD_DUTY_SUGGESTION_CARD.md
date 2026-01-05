How to Add New Suggestion Cards
Architecture (Single Source of Truth)
Each suggestion card owns its own:

Display condition(s) - When it should appear
Content - What it looks like
Actions - What buttons/callbacks it has
No central gate needed! Just add your card to 
DutyAttentionCards.kt
 and it automatically works.

Step-by-Step Guide
1. Create Your Card Component
Create a new file: YourNewCard.kt

@Composable
fun YourNewCard(
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C5F7F) // Match theme
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your Card Title",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Description of what user should do",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Button(onClick = onAction) {
                Text("Action")
            }
        }
    }
}
2. Add to DutyCardEligibility.kt
Open 
DutyCardEligibility.kt
 and add your card's eligibility:

data class CardEligibility(
    val hasPendingCard: Boolean,
    val hasLocationCard: Boolean,
    val hasYourNewCard: Boolean  // ← Add your property
) {
    val hasAnyCards: Boolean
        get() = hasPendingCard || hasLocationCard || hasYourNewCard  // ← Include it
}
fun Duty.getCardEligibility(): CardEligibility {
    return CardEligibility(
        hasPendingCard = category == "Pending",
        hasLocationCard = location.isNullOrBlank() && ...,
        hasYourNewCard = YOUR_CONDITION_HERE  // ← Define condition once
    )
}
3. Add to DutyAttentionCards
Open 
DutyAttentionCards.kt
 and render your card:

@Composable
fun DutyAttentionCards(
    duty: Duty,
    // ... params
) {
    val eligibility = duty.getCardEligibility()  // ← Get eligibility
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Existing cards...
        
        if (eligibility.hasYourNewCard) {  // ← Use the property
            YourNewCard(
                onAction = { /* handle */ }
            )
        }
    }
}
That's it! The modal automatically shows/hides AttentionSection based on hasAnyCards.

Display Condition Examples
Examples:

// Show for tasks without deadline
if (duty.category == "Task" && duty.deadline == null) { ... }
// Show for events happening today
if (duty.startTime.toLocalDate() == LocalDate.now()) { ... }
// Show for duties with no description
if (duty.description.isNullOrBlank()) { ... }
// Show for high-priority items
if (duty.priority == Priority.HIGH) { ... }
// Combine multiple conditions
if (duty.location.isNullOrBlank() && 
    duty.startTime.isBefore(LocalDateTime.now().plusHours(2))) { ... }
Current Cards (Examples)
PendingInviteCard
Condition: duty.category == "Pending"
Purpose: Accept/deny event invites
Actions: Accept, Deny

LocationSuggestionCard
Condition: duty.location.isNullOrBlank() && (duty.category == "Event" || duty.title.mightContainLocation())
Purpose: Add location for PoNR calculation
Actions: Set Home, Set Work, Set Other

How AttentionSection Works
DutyCardEligibility.kt  ← Single source of truth
  └─ duty.getCardEligibility()
       ├─ hasPendingCard
       ├─ hasLocationCard  
       └─ hasAnyCards
DutyDetailModal
  └─ if (eligibility.hasAnyCards) {  ← Uses helper
       AttentionSection
         └─ DutyAttentionCards
              └─ if (eligibility.hasLocationCard) { ... }  ← Uses helper
     }
Single Source of Truth: All card display logic lives in 
DutyCardEligibility.kt
. Both the modal (to show/hide section) and the cards component (to render cards) use the same eligibility object.

If no cards render:

eligibility.hasAnyCards returns false
AttentionSection completely hidden
Full modal available for duty details
If any cards render:

eligibility.hasAnyCards returns true
AttentionSection appears at bottom
Grows to fit cards, max 50% height
Best Practices
✅ DO:

Keep card conditions simple and readable
Make card self-contained (own logic, own UI)
Use descriptive card names (DeadlineReminderCard, not Card3)
Handle all states in the card itself
❌ DON'T:

Add logic to 
needsAttention()
 function (it's deprecated)
Duplicate display conditions elsewhere
Make cards depend on each other
Assume card order matters (it doesn't)
