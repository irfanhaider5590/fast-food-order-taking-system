# Server ID Explanation

## What is Server ID?

Server ID is a **unique identifier** generated for your server instance. It looks like this:
```
531fdd24-9608-3be3-8a83-90c29ebd236c
```

## How is it Generated?

The Server ID is created from your server's properties:
- Server hostname/computer name
- Server IP address

These properties are combined and converted into a UUID (Universally Unique Identifier) format. The Server ID is stored in the database and persists across server restarts.

## Why Do We Need Server ID?

### 1. **Multi-User License Sharing**
   - Each license is bound to a specific Server ID
   - **All users** (branch manager, staff, etc.) connecting to the same server share the same Server ID
   - Once activated on a server, the license can be used by **all users** on that server
   - This allows multiple users to access the system with a single license

### 2. **Server-Based Licensing**
   - License is tied to the server instance, not individual client machines
   - Users can log in from different computers and use the same license
   - Perfect for multi-user environments (restaurants, branches, etc.)

### 3. **Security**
   - Prevents license sharing between different servers
   - Even if someone gets your license key, they cannot use it on a different server
   - License is tied to your specific server instance

### 4. **License Management**
   - Helps track which server a license is activated on
   - Prevents duplicate activations on different servers
   - Ensures fair usage per server

## Example Scenario

**Scenario 1: Multi-User Access**
- Server A has Server ID: `531fdd24-9608-3be3-8a83-90c29ebd236c`
- License is activated on Server A ✅
- Branch Manager logs in from Computer 1 → License works ✅
- Staff Member logs in from Computer 2 → License works ✅
- Another Staff logs in from Computer 3 → License works ✅
- All users share the same Server ID and license

**Scenario 2: License Sharing Prevention**
- License is activated on Server A (ID: `531fdd24-9608-3be3-8a83-90c29ebd236c`)
- Someone tries to activate same license key on Server B (ID: `a1b2c3d4-e5f6-7890-abcd-ef1234567890`)
- System detects different Server ID
- Activation fails: "License is already activated on another server" ❌

**Scenario 3: Different Users, Same Server**
- Admin creates users: Branch Manager, Staff Member 1, Staff Member 2
- All users log in from different computers
- All connect to the same server instance
- All share the same Server ID
- All can use the same license ✅

## Important Notes

1. **Server ID is Shared**: All users connecting to the same server share the same Server ID
2. **Persistent Storage**: Server ID is stored in database and persists across restarts
3. **One License Per Server**: A server can only have one active license at a time
4. **Multi-User Support**: Multiple users can use the same license on the same server
5. **Transfer Not Allowed**: License cannot be transferred to another server once activated

## Where to Find Server ID?

Server ID is displayed in:
- **Settings → License Management** section
- Shows current Server ID for your server instance
- Used when activating licenses
- Same for all users on the same server

## Benefits of Server-Based Licensing

✅ **Multi-User Support**: All users on the same server can use the same license
✅ **Flexible Access**: Users can log in from different computers
✅ **Cost Effective**: One license for entire server, not per user
✅ **Easy Management**: Admin manages one license for all users
✅ **Secure**: Prevents license sharing between different servers

## Troubleshooting

### Server ID Changed?
- If server hostname or IP changes significantly, Server ID might change
- Contact administrator to reset license binding
- Provide your new Server ID to administrator

### License Not Activating?
- Check if Server ID matches
- Verify license key is correct
- Ensure license is not already activated on another server
- Check if license is active in database

### Multiple Users Can't Access?
- Verify all users are connecting to the same server
- Check Server ID is the same for all users
- Ensure license is active and not expired
- Verify system activation record exists in database

