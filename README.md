# Propellerads Android SDK
## Installation guide
### Install from sources:
1. Copy `propellerads-sdk` directory to the desired Android project.
2. Append the line `include ':propellerads-sdk'` to the `settings.gradle` file of the project.
3. Add `implementation project(":propellerads-sdk")` to the dependency section of `build.gradle`.
4. Synchronize the project with Gradle files.

### Install from Maven repository:
tbd

## Integration guide
### PropellerButton (Widget #1)
#### Application side configuration:
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

#### Server side configuration:
| Parameter name | Json example | Description | Is required |
| ------ | ------ | ------ | ------ |
| buttonLabel | "Download Now" | Text string | true |
| buttonLabelSize | 18 | Font size in integer pixels | true |
| buttonLabelColor | "#ffffff" | Label color. Hex color code with alpha (string) | true |
| isButtonLabelBold | true | Is bold font enabled (boolean) | false |
| isButtonLabelItalic | true | Is italic font enabled (boolean) | false |
| buttonLabelShadowColor | "#40000000" | Label shadow color. Hex color code with alpha (string) | false |
| buttonRadius | 4 | Button corner radius in integer pixels | false |
| buttonColors | ["#B2D96D", "#789F32"] | Button color (gradient). Array of Hex color codes with alpha (strings). [1..∞] | true |
| buttonLabelAllCaps | true | Are label letters capitalized | false |
| horizontalPadding | 24 | Horizontal paddings integer size. Android's default value is used if parameter not presented. | false |
| verticalPadding | 24 | Vertical paddings integer size. Android's default value is used if parameter not presented. | false |