# XAppDebug

Debug any application of your choice!

In recent version of Android (starting from... Nougat or Oreo I guess?),
un-debuggable apps won't show its name in the logcat view of Android Studio.

This is somehow inconvenient since at some point we always have to build
our apps with release configuration, which disables the debuggable flag and
making life difficult.

This xposed module allows you to toggle debuggable for any app you want.

## Features

- Reasonable UI
- Hooks **both** `Process.start()` **and** 
  `PackageManagerService.getPackageInfo()`.
- Works with multi-user

## How it works?
This module inject hooks to the `android` system package, and tricks the system
think another package is build with `debuggable` option.

## Usage
1. Enable `XAppDebug` (this module) in your xposed framework settings app
2. Hook the `android` system package if any varient of "application scope"
 is enabled. **DO NOT HOOK THE INDIVIDUAL PACKAGE YOU WANT TO DEBUG**
3. Launch `XAppDebug` UI, select the app you want to debug
4. kill and restart the victim app
5. Enjoy!

## Notes

1. For users with EdXposed or LSPosed using "application scope", attach this
   module to `android` system package.
2. For users with Work profile, install this all in **both** your Main profile
   and Work profile. Attach to `android` package in your **Main** profile,
   and tick the apps you want to debug in the according profile.

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
