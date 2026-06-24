![Recents app banner](fastlane/metadata/android/en-US/images/featureGraphic.png)

Recents is a replacement for the default system "recent apps" menu, especially useful if the system utility is unavailable (e.g. after removing the default launcher).
It aims to provide all the system features, including launching the last launched app, displaying all apps by last use time and killing selected or all apps.

[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroidButtonGreyBorder_nofont.png" height="80" alt="Get it at IzzyOnDroid">](https://apt.izzysoft.de/packages/com.tymwitko.recents)

[<img src="https://www.openapk.net/images/badge_obtainium.png" height="80" alt="Get it on Obtainium">](http://apps.obtainium.imranr.dev/redirect.html?r=obtainium://app/%7B%22id%22%3A%22com.tymwitko.recents%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Ftymwitko%2FRecents%22%2C%22author%22%3A%22tymwitko%22%2C%22name%22%3A%22Recents%22%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Afalse%7D%22%7D)

[![IzzyOnDroid Downloads](https://img.shields.io/badge/dynamic/json?url=https://dlstats.izzyondroid.org/iod-stats-collector/stats/basic/yearly/rolling.json&query=$.['com.tymwitko.recents']&label=IzzyOnDroid%20downloads)](https://apt.izzysoft.de/packages/com.tymwitko.recents)

![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/tymwitko/Recents/total?style=for-the-badge&color=blue)

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/tymwitko)

## How to use

### Launch "Recent apps" screen
Assign the app to a custom shortcut (e.g. double-click of the power button) in your system settings.
Upon triggering the shortcut, you will be shown a list of all your apps, sorted by last launch time.
Alternatively, you could simply place the app in a convenient spot in your launcher.

### Launch last app
The app includes a custom shortcut, which (or its activity, `LastAppActivity`) can also be assigned to a custom shortcut, using any shortcut assigner.
If you trigger the shortcut, you will be taken to the most recent app (excluding launchers and the current app), just like when double-tapping the "recents" button on a 3-button navbar.

### Kill apps (Shizuku or root required)
If you have Shizuku or root access, you may retain the functionality of killing apps.
You can either kill apps individually, or use the "KILL ALL" button.
Note: system apps cannot be killed this way.

### Setup your custom whitelist
Don't want to show a bunch of OEM bloat on the list?
Don't want to kill your alarm clock?
Don't want to launch an app even if it was the last one launched?
Mark them in settings as non-showing, non-killable or non-launchable.

### Display running apps
You may toggle between a full list of installed apps or just the running ones in settings.
Bear in mind that "running apps" are defined by having an active task, which may be a bit different to how the system "recent apps" screen treats them.

### Pin apps
You may pin selected apps above the list.
They will stay there regardless of their recency or their running status.
Pinning an app doesn't mean that it's gone from the actual list and doesn't affect the "launch last app" functionality.

### Quick actions
Upon holding a list item you can display a list of quick actions, including:
- launching app info
- uninstalling app
- launching app in split-screen mode
- launching app in free-form mode
- setting whitelist parameters (show/kill/launch)
