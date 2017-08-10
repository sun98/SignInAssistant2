# SignInAssistant2
An Android app which could automatically finish sign-in task in some other apps by root access and acc-service.

## Some references
* About AccessibilityService:
https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html

* About app\src\main\res\xml\acc_service_config.xml:
https://developer.android.com/reference/android/R.styleable.html#AccessibilityService

* About FloatWindow:
https://developer.android.com/reference/android/view/WindowManager.html
http://blog.csdn.net/qq_17250009/article/details/52908791

* About getevent:
You can try to connect you Android phone with use debugging mode and open cmd, type
`adb shell` and
`getevent -l`
Then tap on your phone, swipe, or other operation. You will find some output in cmd.
