To achieve a **< 2-minute accuracy** while maximizing battery life, you should transition from the "No-GPS" server-side approach to a **Client-Side "Fused" Strategy**.

The goal is to stop treating the GPS as an "all-or-nothing" tool and instead use it as a **surgical validator**.

### 1. The "Fused" Structural Mechanism

Instead of requesting raw GPS data, you should use the **Google Play Services Fused Location Provider (FLP)**. This is a "broker" that sits between your app and the hardware.

* **How it saves battery:** It doesn't just turn on the GPS. It looks at what *other* apps are doing. If Google Maps or a weather app just got a location, the FLP gives that data to you for free (zero battery cost).
* **The "Balanced Power" Mode:** You can set the priority to `PRIORITY_BALANCED_POWER_ACCURACY`. This tells the phone: "Give me a location accurate to about 40–100 meters using WiFi and Cell towers, and only turn on the GPS radio if you absolutely have to."

### 2. The Strategic "Pulse" Algorithm

To keep your 1-minute updates efficient, do not just request a location every 60 seconds. Use a **state-based logic**:

| User State | Frequency | Accuracy Requirement | Battery Load |
| --- | --- | --- | --- |
| **Stationary (Office)** | 5 mins | Low (WiFi/Cell) | < 1% |
| **Active (Grooming)** | 1 min | Medium (Balanced Power) | ~2-3% |
| **In Transit (Driving)** | 30 sec | High (GPS) | ~5-8% |

### 3. Solving the "10-Minute Error"

By requesting local location permissions, you gain access to the **Velocity** and **Bearing** (heading) metadata. This is the "silver bullet" for your accuracy problem:

* **Bearing:** If the app knows the user is moving at 180° (South), you pass this `heading` to the Google Routes API.
* **The Result:** Google will no longer "snap" the user to the Northbound lanes or the wrong side of the divided McKinney boulevard. This eliminates the "Ghost U-turn" that causes the 10-minute calculation error.

### 4. Implementation Audit (Conceptual)

Since we are in a Conceptual Audit phase, here is the structural flow for your "Execution" phase:

1. **Request `ACCESS_COARSE_LOCATION`:** This is less "scary" to users than `FINE_LOCATION` but still gives you the WiFi-based accuracy you need for the < 2-minute goal.
2. **Use `setInterval(60000)`:** Set the `fastestInterval` to 30 seconds. This allows the app to "hitchhike" on location updates requested by other apps without triggering its own hardware cycle.
3. **The "Distance Matrix" Trigger:** Only call the expensive/power-intensive Distance Matrix API if the user's physical displacement has exceeded a certain threshold (e.g., 200 meters). If they are sitting at their desk, don't re-calculate the distance; the result hasn't changed.

### 5. Logistical Signal (Efficiency)

By using `PRIORITY_BALANCED_POWER_ACCURACY`, you typically use **1/10th the power** of standard GPS while maintaining enough precision to ensure Google doesn't put the user on the wrong highway.