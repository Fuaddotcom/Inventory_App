# Warehouse Inventory Tracker (RFID-Based)  
*Internal deployment only | Android 8.0+*  

## Overview  
Real-time cargo tracking system for large warehouses using RFID. Built for **{Your Company Name}** employees.  

## Core Features  
- [ ] RFID In/Out Scanning  
- [ ] Offline Sync (SQLite → Cloud)  
- [ ] Storage Zone Mapping (Grid UI)  
- [ ] Unauthorized Exit Alerts  

## Tech Stack  
- **Mobile:** Kotlin, Android SDK 26+, Retrofit, Room  
- **Backend:** AWS EC2, PostgreSQL  
- **RFID Hardware:** {Specify Model + SDK Docs Link}  

## Setup (Employees)  
1. Install via internal APK shared through Google Drive.  
2. Biometric/PIN authentication required on first launch.  
3. Sync warehouse layout data from server on login.  

## Development Setup  
```kotlin  
// Clone repo  
git clone https://github.com/{your-org}/warehouse-inventory-android.git  

// Required:  
- Android Studio Arctic Fox+  
- Minimum JDK 11  
- RFID Scanner API Key (stored in local.properties)
```

### **GitHub Project Board Configuration**  
**Columns:**  
1. **Backlog**  
2. **To Do**  
3. **In Progress**  
4. **Code Review**  
5. **Done**  

**Automation Rules:**  
- When issue is created, add to "To Do".  
- When PR is opened, move to "Code Review".  

**Sample High-Priority Issues (To Create):**  
```markdown  
### [P0] Implement RFID Scan Trigger  
**Description:**  
- Auto-detect RFID tags via Bluetooth when app is near warehouse entry/exit zones.  
- Handle collision errors using EPC Gen2 anti-collision logic.  

**Acceptance Criteria:**  
- Scan success rate ≥ 90% in testing (10 tags simultaneously).  
- Log missed scans to `errors.log`.  

### [P1] Storage Zone Grid UI  
**Description:**  
- Interactive map showing cargo locations (zones A1, B2, etc.).  
- Drag-and-drop support for manual repositioning (audit mode).  

**Assets Needed:**  
- Warehouse layout PNG from facility manager.  
