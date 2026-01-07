External Data Schema Report
This report details the "buckets" of data available from Microsoft 365 and Google Workspace APIs, mapping them to AliMinder's potential needs.

1. Microsoft 365 (Graph API)
A. Outlook Calendar Events
API Resource: event Primary Use: Critical for "Event" duties with fixed times and locations.

Field Name	Type	Description	Relevance to AliMinder
subject	String	Event title	Duty.title
body	ItemBody	HTML/Text content	Duty.description
start
 / 
end
DateTimeTimeZone	Time & Zone	Duty.startTime, Duty.endTime
location	Location	DisplayName, Address, GeoCoordinates	Critical for PoNR. Includes address & coordinates.
attendees	List	Status, Type, Email	Critical for "Pending" invites (Accept/Deny).
isAllDay	Boolean	True/False	Duty.isAllDay
showAs	String	Free, Busy, Tentative, OOF	Filtering availability
importance	String	Low, Normal, High	Duty.priority mapping
onlineMeeting	Object	Join URL (Teams/Skype)	"Join Meeting" action button
responseStatus	Object	response: Accepted, Declined, None	Determines "Pending" vs "Event" category.
categories	List	User-defined tags	Filtering/Tagging
B. Microsoft To Do
API Resource: todoTask Primary Use: "Task" duties with deadlines.

Field Name	Type	Description	Relevance to AliMinder
title	String	Task name	Duty.title
body	ItemBody	Notes/Description	Duty.description
dueDateTime	DateTimeTimeZone	Deadline	Duty.endTime / Duty.deadline
importance	String	Low, Normal, High	Prioritization
status	String	notStarted, inProgress, completed	Filtering completed
isReminderOn	Boolean	Reminder active?	Notification sync
reminderDateTime	DateTimeTimeZone	Reminder time	Local notification sync
linkedResources	List	Link to email/doc source	"Open in App" deep links
C. Microsoft Planner
API Resource: plannerTask Primary Use: Team-based "Task" duties.

Field Name	Type	Description	Relevance to AliMinder
title	String	Task name	Duty.title
startDateTime	DateTime	Targeted start	Duty.startTime
dueDateTime	DateTime	Deadline	Duty.endTime
percentComplete	Int	0-100	Progress tracking
priority	Int	0-10	Mapping required (0-1=Urgent, 2-4=Important)
assignments	Object	User IDs assigned	Filter for "My Tasks"
details	Object	Description, Checklist	Duty.description, Sub-tasks
### D. Microsoft Teams (Approvals Only)
**API Resources:** `approvalItem`
**Primary Use:** User approvals (Pending actions). Note: "Shifts" are out of scope.

#### 1. Approvals
| Field Name | Type | Description | Relevance to AliMinder |
| :--- | :--- | :--- | :--- |
| `displayName` | String | Request Title | `Duty.title` |
| `description` | String | Details | `Duty.description` |
| `status` | String | Pending, Approved | `Duty.category="Pending"` |
| `expirationDateTime`| DateTime | Time to expiry | `Duty.deadline` |
2. Google Workspace
A. Google Calendar Events
API Resource: event Primary Use: "Event" duties.

Field Name	Type	Description	Relevance to AliMinder
summary	String	Event title	Duty.title
description	String	HTML content	Duty.description
start
 / 
end
Object	dateTime or 
date
 (all-day)	Duty.startTime, Duty.endTime
location	String	Text location	Critical for PoNR. Needs geocoding if raw text.
attendees	List	Email, ResponseStatus	"Pending" invites logic.
hangoutLink	String	Google Meet URL	"Join Meeting" action button
status	String	confirmed, tentative, cancelled	Filter cancelled events
reminders	Object	Overrides/Defaults	Notification sync
colorId	String	Color key	UI Styling match
B. Google Tasks
API Resource: task Primary Use: Simple "Task" duties.

Field Name	Type	Description	Relevance to AliMinder
title	String	Task title	Duty.title
notes	String	Text description	Duty.description
due
String	RFC3339 Date (No Time)	Duty.endTime (Note: Google Tasks often date-only)
status	String	needsAction, completed	Filtering
links	List	Related URLs	Context links
webViewLink	String	URL to task	"Open in Browser"
3. Discrepancies & Action Items
A. Location Fidelity
MS Graph: Returns 
Location
 object with structured address AND coordinates.
Google: Returns raw 
String
 location.
Action: AliMinder needs a robust Geocoding fallback for Google events (and MS events where coords are missing).
B. "Pending" Status
MS Graph: Explicit responseStatus field.
Google: attendees list must be parsed to find "self" and check responseStatus.
Action: Improve DutyMapper to parse both formats into pending category.
C. Task Deadlines
MS ToDo: Has exact dueDateTime.
Google Tasks: Often only 
due
 date (no time).
Action: Decide on default time (e.g., 5:00 PM or End of Day) for date-only tasks.