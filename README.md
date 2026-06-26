# Jabra Cloud Integration - Capabilities & Configuration
This document covers Jabra Cloud Aggregator Capabilities and Configuration.

Symphony integrates with the Jabra Cloud platform to provide monitoring and control of Jabra conference and personal audio/video devices across an organization.
Main features are: real-time device health monitoring, firmware tracking, room assignment visibility, connected computer details, and camera settings control for supported models.

## Jabra Cloud - Prerequisites
The Jabra Cloud Aggregator authenticates using an API Access code generated from the Jabra Cloud portal.

To obtain the API Access code:
1. Log in to the Jabra Cloud portal and navigate to Integrations → API Access
2. Copy an existing API Access code, or create a new one via Add API Access
3. Use this code as the **Password** in the Symphony device configuration (Username is not required)

## Jabra Cloud - Device Configuration and Provisioning

### Jabra Cloud - Connection Setup

| Field | Value |
|---|---|
| Device Type | Infrastructure |
| Category | Management |
| Manufacturer | Jabra |
| Model | Jabra+ |
| Monitoring Service | Advanced Monitoring |
| Ping Protocol | TCP |
| Monitoring Source | Direct |
| Management Address | `api.cloud.jabra.com` (default — may differ if a proxy or custom DNS is in use) |
| Protocol | HTTPS |
| Username | Leave blank |
| Password | Jabra API Access code |
| Port Number | 443 |

### Jabra Cloud - Device Provisioning

By default, unprovisioned devices appear on Aggregated Devices → Unprovisioned Devices tab.

To import a Jabra device for monitoring:
1. Open Aggregated Devices
2. Press the blue (+) icon on the unprovisioned device
3. Verify the provisioning details, mark the device, press Import, and confirm

### Jabra Cloud - Adapter configuration properties

| Property | Description |
|---|---|
| clientTypeFilter | Filters which devices are aggregated. Values: `MeetingRoom` (default), `Personal`, `All` |
| configManagement | Set to `true` to expose controllable properties. `false` by default — controls are hidden. |
| displayPropertyGroups | Comma-separated list of property groups to display. Default: empty (no optional groups shown). Values (case-sensitive): `JabraRoom`, `Computer`, `JabraClient`, `Settings` |
| devicesInterval | Retrieval interval for device data in milliseconds. Default and minimum: 30000 |
| deviceSettingsInterval | Retrieval interval for device settings in milliseconds. Default and minimum: 30000 |
| roomsInterval | Retrieval interval for room data in milliseconds. Default and minimum: 30000 |

For detailed information on the aggregator and its configuration, please refer to our knowledgebase -> https://symphony.knowledgeowl.com/help/jabra-cloud-aggregator

## Jabra Cloud - Available Monitored Data

### Aggregator Properties
Adapter metadata: AdapterBuildDate, AdapterVersion, AdapterUptime, AdapterUptime(min), LastMonitoringCycleDuration(sec), MonitoredDevicesTotal.

**JabraRoom group** (one group per room; requires `displayPropertyGroups` to include `JabraRoom`):

| Property | Description |
|---|---|
| Name, Location, Status, Type | Room identity and current state |
| ID, GroupID | Unique room and group identifiers |
| DateAndTimeCreated(UTC) | When the room was created |
| Reboot | Reboots all online devices in the room. Offline devices are skipped. Not available for disconnected rooms. While rebooting, DeviceConnectionStatus reports as `Rebooting`, which Symphony treats as Online. |

### Aggregated Device Properties

**General** (all devices):

| Property | Description |
|---|---|
| deviceName, deviceId, serialNumber, ProductName, ProductID | Device identity |
| deviceOnline, DeviceConnectionStatus | Online status (DeviceConnectionStatus not applicable for personal devices) |
| FirmwareVersion, FirmwareUpdateInProgress | Firmware state |
| RoomName, RoomLocation, RoomType | Room assignment (conference devices only) |
| AddedAt(UTC), LastSeenAt(UTC) | Lifecycle timestamps |
| IsMeetingDevice, VariantType, GroupID | Classification details |

**Computer group** (requires `displayPropertyGroups` to include `Computer`): IPAddress, MACAddress, Name, OperatingSystem, Username of the host computer.

**JabraClient group** (requires `displayPropertyGroups` to include `JabraClient`): Client, Name, Version of the Jabra client application.

**Settings group** (requires `displayPropertyGroups` to include `Settings`): Camera and audio settings reported by the Jabra Cloud API — exact properties depend on the device model. Settings are **controllable** for Jabra PanaCast 50 devices when online; **read-only** for all other models and for any offline device. When settings are changed, ApplySettings and CancelSettings buttons appear; unsaved changes are auto-discarded after 5 minutes. Applying settings requires a device reboot within 3 minutes.

## Jabra Cloud - Troubleshooting

**Login Error**
- Verify the Password field contains the correct Jabra API Access code from the Jabra Cloud portal
- Confirm the API Access code has not expired or been revoked in the Jabra Cloud portal

**API Error**
- Check the API error description in the aggregator extended properties
- Confirm the Management Address is correct — default is `api.cloud.jabra.com`, but may differ in environments using a proxy or custom DNS

**Link Error / Ping Timeout**
- Verify the Cloud Connector can reach the Jabra Cloud API on port 443
- Confirm Ping Protocol is set to TCP in the Symphony device configuration

**No Optional Groups Visible (JabraRoom, Computer, JabraClient, Settings)**
- Add the required group names to the `displayPropertyGroups` adapter property (case-sensitive)

**Controls Not Available**
- Set `configManagement` to `true` to expose controllable properties
- Settings controls are only available for Jabra PanaCast 50 devices when online

If none of the recommended steps help, please enter an SOS ticket at {https://avi-spl.atlassian.net/servicedesk/customer/portals}

## Jabra Cloud - What AI Assistant can do with it:
- Find Jabra Cloud Aggregated Devices (Jabra+ as Monitoring Proxy) in Symphony
- Verify Jabra Cloud Aggregator configuration and adapter property settings
- Report on device online status, firmware versions, room assignments, and computer details
- Check room reboot status and Jabra room information

## Jabra Cloud - What AI Assistant cannot do with it:
- Provision devices
- Generate or manage API Access codes in the Jabra Cloud portal
- Apply camera settings changes directly
