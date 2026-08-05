# lurkerx-apk
This is the Java codebase for the Android LurkerX malware

## Tested and succeeded evasion from Android 7 to Android 16

## Contributions are ver much welcomed: :[CONTRIBUTING.md](CONTRIBUTING.md)

To focus on near-native performance and nearly absent of runtime surprises, this project is written in purely Java. 

You can clone this repository in Android studio or you can create a new Android project paste the contents of this folder into it.

Currently based on Gradle 8.10.2+

## Guidelines

1. Make code modular (by making capsules that contains related methods)
2. ONLY comment when NECCESSARY (clarity > quantity)
3. No hate speeches
4. New monitoring features goes into a subpackage, for e.g (com/.../notifications), which monitors and reports notificatoins