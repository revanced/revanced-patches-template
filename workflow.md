## Reverse engineering tools

### Merge split APKs

Download [ApkEditor](https://github.com/REAndroid/APKEditor) from their latest release.

```bash
java -jar APKEditor.jar m -i split.xapk
```

### Apktool

Install apktool : https://apktool.org/docs/install/#linux

```bash
apktool d -o output app.apk
```

### Jadx

Download jadx : https://github.com/skylot/jadx/releases

```bash
./jadx/bin/jadx -d output app.apk
```

## Patches

### Setup

```bash
cd /tmp
sudo apt install sdkmanager
cp -r /usr/lib/android-sdk/ android-sdk
sudo chown -R $USER:$USER android-sdk
export ANDROID_HOME=/tmp/android-sdk
sdkmanager --licenses
```

### Building

```bash
./gradlew build
jar cf patches.jar -C patches/build/classes/kotlin/main . -C patches/build/resources/main .
```

### Running

```bash
java -jar revanced-cli.jar list-patches \
    --with-descriptions=true \
    --with-versions=true \
    patches.jar

java -jar revanced-cli.jar patch \
    app.apk \
    --patches patches.jar \
    --out patched.apk
```

## Emulation

```bash
/home/mubelotix/Android/Sdk/emulator/emulator -avd Medium_Phone_API_35 -gpu host
```

## Logs

```bash
adb shell
logcat | grep yourapp
```
