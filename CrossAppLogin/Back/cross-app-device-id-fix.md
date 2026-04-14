# Fixing Cross App device ID

## The Problem

If apps are unable to communicate when the `DeviceInfoUpdateWorker` is running, we can end up in a situation where apps have different Cross App Device IDs.

## The goal

To fix this, we will need to resync/overwrite it when appropriate.

## The risks
1. Race conditions if multiple apps trigger the resync simultaneously.
2. One app can die during the resync, or can be unavailable (e.g. being updated)

## The solution

### When

We want to schedule the resync with a Worker…
- …manually (hidden for users with a staff account), to test 1st
- …when the app is updated
- …when retrieving accounts

### The algorithm (client side)

The resync work has an important metadata element:
- the start time (elapsedRealtimeNanos)

If we get a request from another app, based on who started first, we either:
- keep going while asking it to abort
- cancel our resync

```
val apps = `all our installed apps`.filter { not(it.up2date) }

apps.forEach { app ->
   send _it_ our cross-app-device-id, asking to use it
   get result. It can be:
      a. a confirmation that it was already in sync
      b. a confirmation/ack (that its been updated)
      c. an abort request + id (if there's a concurrent non-stale resync with earlier start time)
   if c:
      update the id with the one received
      mark _it_ as up2date in storage
      abort (exit as retry/reschedule)
   if a or b:
      mark _it_ as up2date in storage
      continue
}

apps.asReversed().forEach { app -> // Reversed, so processes are more likely to still be alive.
   
}
```

### The algorithm (server side)
