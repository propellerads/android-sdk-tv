# Propellerads Android SDK
## Installation guide
### Install from Maven Central repository:
```groovy
implementation 'com.propellerads:sdk:x.x.x'
```
### Install from sources (Alternative):
1. Copy `propellerads-sdk` directory to the desired Android project.
2. Append the line `include ':propellerads-sdk'` to the `settings.gradle` file of the project.
3. Add `implementation project(":propellerads-sdk")` to the dependency section of `build.gradle`.
4. Synchronize the project with Gradle files.

## Integration guide
### Base integration
1. Obtain `PublisherId` from Propellerads Administration.
2. Add `PublisherId` as meta-data to the `AndroidManifest.xml` of the application.
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
### PropellerButton integration
#### Application side configuration:
1. Place the widget to the desired area of the layout.
```xml
    <com.propellerads.sdk.widget.PropellerButton
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:widget_id="xxxxxxxx" />
```
2. Specify `app:widget_id` parameter with the value provided by Propellerads Administration.

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
### PropellerBanner integration
#### Application side configuration:
1. Create `PropellerBannerRequest` object in `onCreate()` method of `Activity` or `Fragment`.
2. Pass required constructor parameters:
    * `adId` – request id provided by Propellerads Administration
    * `lifecycle` – `androidx.lifecycle.Lifecycle` object of `Activity` or `Fragment`.
    * `fragmentManager` – `supportFragmentManager` in case of `Activity` or `childFragmentManager` in case of `Fragment`.
    * `callback` – the object that will be notified of the `PropellerBanner` state change.
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    PropellerBannerRequest(
        "qr_code_2",
        lifecycle,
        childFragmentManager
    ) { isShow ->
        val label = "${if (isShow) "Show" else "Hide"} banner callback (First Fragment)"
        Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
    }
}
```
#### Server side configuration:
| Parameter name | Json example | Description | Is required |
| ------ | ------ | ------ | ------ |
| layoutTemplate | "qr_code_3_1" | String id of the layout | true |
| titleLabel | "Confirm you're not a robot" | Text string | true |
| descriptionLabel | "Scan the qr-code with your phone" | Text string | true |
| extraDescriptionLabel | "QR-CAPTCHA" | Text string | true |
| titleColor | "#29BFFF" | Hex color code with alpha (string) | true |
| descriptionColor | "#000000" | Hex color code with alpha (string) | true |
| extraDescriptionColor | "#4D000000" | Hex color code with alpha (string) | true |
| backgroundColor | "#ffffff" | Hex color code with alpha (string) | true |
| isFullWidth | true | Banner fills the screen width (boolean) | true |
| isFullHeight | false | Banner fills the screen height (boolean) | true |
| hasRoundedCorners | false | Banner has rounded corners (boolean) | true |
| positionOnScreen | "bottom" | Banner position ("top", "bottom", "center") | true |
| dismissTimerValue | 20 | Time until the banner auto dismiss (seconds) | true |
| timeout | 5 | Timeout before the first banner impression (seconds) | true |
| interval | 40 | Time interval between the banner impressions (seconds) | true |
| capping | 120 | Frequency measurement time interval (seconds) | true |
| frequency | 3 | Max impressions in capping time interval | true |
