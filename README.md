# Propellerads Android SDK

### Installation guide

#### Install from sources:
1. Copy `propellerads-sdk` directory to the desired Android project.
2. Append the line `include ':propellerads-sdk'` to the `settings.gradle` file of the project.
3. Add `implementation project(":propellerads-sdk")` to the dependency section of `build.gradle`. 
4. Synchronize the project with Gradle files.

#### Install from Maven repository:
tbd

### Integration guide

#### PropellerButton (Widget #1):
1. Obtain PublisherId from Propellerads Administration.
2. Add PublisherId as meta-data to the `AndroidManifest.xml` off the application.
```xml
    <manifest>
        <application>
            <activity></activity>
            <meta-data
                android:name="com.propellerads.sdk.PublisherId"
                android:value="XXXXXX" />
        </application>
    </manifest>
```
3. Place the widget to the desired area of the layout.
```xml
    <com.propellerads.sdk.widget.PropellerButton
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:widget_id="xxxxxxxx" />
```
4. Specify `app:widget_id` parameter with the value provided by Propellerads Administration.
