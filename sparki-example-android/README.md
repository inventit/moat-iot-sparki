MOAT IoT Application Example
========
Sparki Example
--------

This APK is a gateway between [Sparki](http://arcbotics.com/products/sparki/), an [Arduino](http://www.arduino.cc/) based easy robot, and ServiceSync Sandbox Cloud with [MOAT PubSub API](http://dev.inventit.io/references/moat-pubsub-api-document.html).

The following APK must be installed into a device where this application runs.

- Inventit ServiceSync Gateway

You can get it from [Goole Play](https://play.google.com/store/search?q=inventit+service-sync&c=apps) for free.

See [the tutorial](http://dev.inventit.io/guides/get-started.html) to learn more.

The directory structure of this application is as follows:

    |-- .settings (E)
    |-- assets
    |   `-- moat (1)
    |-- gen (E)
    |-- libs
    |   `-- com
    |       `-- hoho
    |           `-- usb-serial-for-android
    |               `-- v0.1.0-5c8a655-inventit-0.1.0
    |-- res
    |   |-- drawable
    |   |-- drawable-hdpi
    |   |-- drawable-ldpi
    |   |-- drawable-mdpi
    |   |-- drawable-xhdpi
    |   |-- layout
    |   `-- values
    |-- src
    |   `-- main
    |       `-- java

- (1) where ``signed.bin`` file is placed
- (E) for Eclipse specific directories