
Drupapp
-------

Example Drupal module able to save a title and image coming from 
an Android (Phone and Glass) or iOS application. The source for this module and the application
can be found at http://github.com/swentel/Drupoid

Blog post: http://realize.be/posting-images-android-drupal
Screenshots: http://realize.be/mobile

Drupal content type
-------------------

You need to create at least one content type with an image field.
You can configure this to your own needs.

Drupal Installation
-------------------
Enable the module, give yourself the 'administer drupapp' permission
and go to admin/config/media/drupapp where you need to configure
settings like image field etc.

Give roles the "upload images via app" so they can upload images.

Every request is logged into watchdog - good or bad.

Android
-------

The Phone app is written on Android 2.3.3, SDK level 10.
After compiling, run the application and configure
the url, username and password. The path in the url
matches the Drupal path, eg http://example.com/mobile-post

The Glass version adds a new voice command and activity to take a picture
and then allows you to share it to your site. Default command is "Drupal Glass".
You need to change the configuration in Config.java so we can post
to your site. In future versions, we'll add authentication, but this
is a little hairy to add right now in non-official glassware.

Note that we currently always override the picture take, so there's no history on the Glass.

iOS
---
To come

License
-------

    Copyright (C) 2012 Kristof De Jaeger

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
