# safe-getting-started-android
|Android|
|:-:|
|[![Build Status](https://travis-ci.com/maidsafe/safe-getting-started-android.svg?branch=master)](https://travis-ci.com/maidsafe/safe-getting-started-android)|

Example application to demonstrate the use of the [safe-app-android](https://github.com/maidsafe/safe_app_java/tree/master/safe-app-android) library to build a simple ToDo Android App for the SAFE Network.

## Introduction

This example demonstrates use of the [safe-app-android](https://github.com/maidsafe/safe_app_java/tree/master/safe-app-android) library to build a ToDo app for Android on the SAFE Network. There are two modes of the app showcased:
1. mock - user can build the app to work on a mock simulation of the SAFE Network, created on the users local device,
2. non-mock - user can build the app to work on the current live alpha-2 or local network.


## Pre-requisites

* Android Studio
* Java 8
* Android SDK


## Supported Platforms

Android API 24 and above (armeabi-v7a, x86_64 support).

## Features
* **Authentication using the [SAFE Authenticator](https://github.com/maidsafe/safe-authenticator-mobile):**

    A SAFE application needs to be authorised by the user before being able to connect to the network, this is achieved by sending an authorisation request to the SAFE Authenticator.
* **Mock & Non-mock flavours:**

    User should be able to easily switch between mock and non-mock using the [product flavours feature](https://developer.android.com/studio/build/build-variants#product-flavors) of Android Studio making it convenient to develop, test and build the apps on these environments.
* **Mutable data operations:**

    Perform CRUD operations on Mutable data.
* **Usage of App's Container:**

    Store mutable data information in an app's default container. This can be used to retain and retrieve data used in the application.

## Screenshots

<img alt="Authentication" src="/screenshots/authentication.png?raw=true" width="300"/>  <img alt="Add Section" src="/screenshots/add_section.png?raw=true" width="300"/>

<img alt="Sections Page" src="/screenshots/sections_page.png?raw=true" width="300"/> <img alt="Tasks Page" src="screenshots/tasks_page.png?raw=true" width="300"/>

<img alt="Task Info" src="/screenshots/task_info.png?raw=true" width="300"/> <img alt="On Disconnected" src="/screenshots/disconnected.png?raw=true" width="300"/>

## Further help

Get your developer related questions clarified on [SAFE Dev Forum](https://forum.safedev.org/). If your looking to share any other ideas or thoughts on the SAFE Network you can reach out on [SAFE Network Forum](https://safenetforum.org/)

## License

This SAFE repository is dual-licensed under the Modified BSD ([LICENSE-BSD](LICENSE-BSD) https://opensource.org/licenses/BSD-3-Clause) or the MIT license ([LICENSE-MIT](LICENSE-MIT) https://opensource.org/licenses/MIT) at your option.

## Contributing

Want to contribute? Great :tada:

There are many ways to give back to the project, whether it be writing new code, fixing bugs, or just reporting errors. All forms of contributions are encouraged!

For instructions on how to contribute, see our [Guide to contributing](CONTRIBUTING.md).
