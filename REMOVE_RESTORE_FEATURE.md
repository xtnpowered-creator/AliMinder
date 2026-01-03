Remove/Restore and Auto-Hide Duties features…
PHASE 1

Duties get a column in their Room Database table called ‘dismissal_reason’ (is nullable). Allowable values are:
(null)
COMPLETED
CANCELLED
USER-HIDDEN
AUTO-HIDDEN
As is done in some apps, such as email clients, users can slide duty cards off screen to the right to remove them, which triggers a context sensitive popup modal as follows:
While modal is visible, rest of screen behind it blurs
If duty is an event, modal has buttons for:
I made it
Not going
Just hide it
Nevermind
If duty is a task, modal has buttons for:
I did it
Not doing it
Just hide it
Nevermind
User’s button choice results in:
Setting corresponding value in database dismissal_reason column if not ‘Nevermind’, as follows:
COMPLETED for "I made it" / "I did it"
CANCELLED for "Not going" / "Not doing it"
USER-HIDDEN for "Just hide it"
If ‘Nevermind’ then do nothing…just abandon the Remove function, dismiss the modal and snap duty card back where it was pre-slide. A tap outside the modal has the same result.
Settings app tab gets a new control for auto-hide overdue duties after X time overdue
Allowable values are:
30 minutes
1 hour
2 hours (default)
3 hours
Duties overdue by X time automatically set database dismissal_reason to AUTO-HIDEN
Removed duties aren’t really deleted. Their duty cards instead show up in a Settings tab called ‘Restore’ where users can slide duty cards off screen to the left, get a popup modal asking “Restore this duty?”, which sets the database value back to null again.
‘Restore’ tab basically just filters for any duty where database dismissal_reason value is not null, while other pages basically filter for duties that do have a null value there.
