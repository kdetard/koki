# Koki
A beautiful OpenRemote client in COMPLETELY PURE Java (the Gradle Kotlin DSL doesn't count).

## Usage
You can customize the instance URL and authentication URL via `app/src/main/res/values/keycloak-config.xml`.
Please specify `MAPTILER_API_KEY` in your `local.properties` file by getting one at https://www.maptiler.com/.
Uncomment the relevant code that is used to build release APKs if needed.

We are assuming that you have some assets that will be used in the Home and Monitor tab, which will fail
if you don't use the default instance. Feel free to remove it to your liking.

## Notable features
It can show a list of asset groups and mark assets with locations on the map. You can also change app language
and theme by looking at the relevant controllers at `app/src/main/java/io/github/kdetard/koki/feature/settings`.

There is also a MMKV cookie store written from scratch to store cookies in a MMKV database.

# Acknowledgements
We want to thank Kotlin for being able to interop with Java so that we can use Kotlin libraries in our COMPLETELY
PURE Java codebase.

Below is a non exhaustive list of libraries that we used and acknowledged:
- RxJava 3
- RxAndroid 3
- RxBinding 4
- Autodispose for disposing observables
- RxDogTag
- RxPM (unused but we want to include as well)
- Hilt for Dependency Injection
- Insetter for adding status bar insets to view
- Retrofit
- OkHttp
- Moshi for ser/deserialize JSON
- Moshi Polymorphic Adapter
- FastAdapter for Recycle View adapter
- Androidx (AppCompat, Datastore, Fragment, Activity, Lifecycle, Constraint Layout, Preference, Splash Screen etc.)
- Protobuf for writing Preference schema
- Conductor for navigation
- Tachiyomi
- Tachiyomi's conductor-preference-helper for using Androidx Preference with conductor
- Jsoup for HTML parsing
- MapLibre for map
- AppAuth-Android for Keycloak OpenID integration
- Timber for log
- Vico for graph

# Conclusion
NEVER BUILD ANOTHER ANDROID APPLICATION IN JAVA IN 2023 OR WHATEVER YEAR YOU'RE LIVING IN.
