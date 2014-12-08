# Gloomy Dungeons II

Gloomy Dungeons II is a continuation of old–school 3d–shooter (Gloomy Dungeons 3D) in the style of Doom and Wolfenstein.
Second part still use wolf-like engine (but *not* raycasting), but this time engine improved a bit:

  - Simple lighting effects
  - Different floor and ceiling textures in each cell
  - Extruded ceiling and doors
  - Rocket launcher

Credits, as usual:

  - Code: restorer
  - Graphics: Denis Smoktunovich (smoktunovich@gmail.com)
  - Levels: Denis Smoktunovich (smoktunovich@gmail.com), restorer
  - Sound: www.nongnu.org/freedoom, AlexanderGIN (kohedlo3d@gmail.com)

This game is released under MIT License (http://www.opensource.org/licenses/mit-license.php).

## Note

It is oblivious, but I still want to note: all game resources are open-sourced *except* mp3 rock music which is downloaded from the server.

# Compiling

There are 2 variants of game: for google play (with zeemote, facebook, and analytics support) and for f-droid
You can compile either by using build script or directly using gradle.

## Compile and install debug build

  - `./z-build fdroid debug install` or
  - `./gradlew installForfdroidWoutgplayWoutzeemoteWoutkoDebug`

## Compile release builds

To be able to compile release builds, create put your keystore file (or create new) to `tools/signing.keystore` and create `tools/signing.properties`:

```
keyAlias=put_key_alias_here
storePassword=put_keystore_password_here
keyPassword=put_key_password_here
```

  - `./z-build fdroid release` or
  - `./gradlew assembleForfdroidWoutgplayWoutzeemoteWoutkoRelease`

Search for result .apk files in build/outputs/apk/
