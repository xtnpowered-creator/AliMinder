Location Strategy & PoNR Integration Report
Executive Summary
AliMinder employs a "Fused Strategy" for location acquisition, prioritizing battery efficiency while ensuring high-fidelity data when it matters most (the "Point of No Return"). The system operates on an intelligent state machine that transitions between Dormant, Monitoring, and Active states based on real-world context (Activity, Geofence, Time-to-Event).

1. The Location State Machine
The core logic resides in 
LocationService
 and 
TrackingState
.

States & Intervals
State	Context	Update Interval	Battery Impact
DORMANT	User is inside a "Safe Zone" (Home/Work) AND no imminent duties.	OFF (0 ms)	None
MONITORING	User has left a Safe Zone (Geofence Exit) OR Duty is approaching (<1h).	3 Minutes	Very Low
ACTIVE	User is moving (IN_VEHICLE) or approaching a critical PoNR.	30 Seconds	Low-Medium
2. Acquisition Triggers & Methods
The system uses a multi-layered approach to wake up and refine data quality.

Layer 1: The "Wake Up" Triggers
These systems run efficiently in the background to detect when to start tracking.

Geofencing (
GeofenceService
)

Trigger: User EXITS Home or Work radius (200m).
Action: Transitions state from DORMANT -> MONITORING.
Fallback: If Geofence misses (e.g., reboot), the ProactiveWorker acts as a safety net.
Proactive Worker (
ProactiveLocationTrackingWorker
)

Schedule: Runs every 30 minutes.
Condition: checks for any Duty with a 
PoNR
 within 1 Hour.
Action: Force-starts MONITORING if not already running.
Activity Recognition (
ActivityRecognitionService
)

Trigger: Android detects IN_VEHICLE (Confidence > 75%).
Action: Transitions MONITORING -> ACTIVE.
Trigger: Android detects STILL.
Action: Transitions ACTIVE -> MONITORING.
3. Strategic Batching (Power conditionals)
Even in the ACTIVE state (30s updates), the system intelligently "batches" GPS points to save radio power based on Time Urgency (Minutes to PoNR).

Time to PoNR	Batching Strategy	Rationale
> 15 Minutes	5 Minute Delay	We are far from the critical moment. Data can be delayed.
8 - 15 Minutes	2 Minute Delay	Getting closer. Moderate updates.
< 8 Minutes	REAL-TIME (No Batching)	CRITICAL ZONE. GPS radio stays active for maximizing accuracy.
Defined in 
TrackingState.kt
 logic.

4. PoNR Calculation & Data Quality
The 
CalculatePoNRUseCase
 fuses the location data with stored knowledge to produce the "Commute Time".

Data Quality Hierarchy
The system assigns a "Confidence Score" (Quality) to every calculation:

GOOD (High Fidelity)

Source: Fresh GPS 
Location
 object.
Condition: accuracy < 100 meters.
Result: Real-time Google Maps API Traffic query.
COARSE (Low Fidelity)

Source: Fresh Network/WiFi 
Location
 object.
Condition: accuracy > 100 meters.
Result: Real-time Google Maps API Traffic query (flagged with Orange warning).
STALE (Fallback)

Source: Duty.lastCalculatedCommuteMinutes (Persisted).
Condition: No Location Signal OR API Failure.
Result: Uses historical traffic data from the last successful check. (Yellow warning).
VIRTUAL (Zero Travel)

Source: Duty.virtualMeetingLink is present.
Condition: location is NULL.
Result: 0 Minutes commute (Always "GOOD" quality).
5. Visual "Persona" Logic
The UI reacts to the 
delta
 (Minutes until PoNR) calculated above.

Time Window	Color	Persona State	Meaning
> 60 Mins before PoNR	🟢 Green	OPTIMISTIC	"You have plenty of time."
60 Mins to 0 Mins	🟡 Yellow	WEARY	"Wrap it up. Buffer incoming."
< 0 Mins (Past PoNR)	🟠 Orange	URGENT	"LEAVE NOW. You are eating into buffer."
Past Start Time	🔴 Red	LATE	"You are late."
Default Threshold: 60 Minutes (Configurable in Settings).

Conclusion
The architecture is designed to remain silent and battery-neutral for 90% of the day (Dormant), wake up gently when context changes (Monitoring), and only engage high-power GPS/Radio during the specific 15-minute window before a critical departure.