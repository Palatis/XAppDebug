# XAppDebug

Debug any application of your choice!

In recent version of Android (starting from... Nougut or Oreo I guess?),
undebuggable apps won't show its name in the logcat view of Android Studio.

This is somehow inconvenient since at some point we always have to build
our apps with release configuration, which disables the debuggable flag and
making life difficult.

## Features

- Reasonable UI
- Hooks **both** `Process.start()` **and** `PackageManagerService.getPackageInfo()`.
- Works with multi-user

## Special Thanks

I've tried several Xposed modules, but they either don't work with multi-user
(work profile) or is too old to even work correctly.

That's why I started this project. However, lots of codes are ~~copied from~~ 
refactored out of these projects.

- [XDebuggable](https://github.com/ttimasdf/XDebuggable):
  - Works the best IMHO
  - Hooks `Process.start()`
  - Can only enable one app because it utilize the debug options in
    "Developer Options" from `System.UI`.
- [XDebug](https://github.com/deskid/XDebug): 
  - Works, no UI.
  * Hooks `Process.start()`
  - Making **EVERY** app debuggable
- [App Debuggable](https://github.com/dirname/AppDebuggable): 
  - Good UI
  - Hooks `PackageManagerService.getPackageInfo()`
  - Writes file to `/sdcard` and `/data` (which is IMHO dirty)
  - No multi-user support (because of ↑↑↑)
  - Requests **LOTS** of unnecessary permissions (even wants SU...!!)
- [OpenDbg](https://github.com/ZhouHoubin/OpenDbg): 
  - Good UI, too.
  - Hooks `PackageManagerService.getPackageInfo()`
  - Pollute `/sdcard`