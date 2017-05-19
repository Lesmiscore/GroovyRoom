# GroovyRoom
A Groovy code runner application for Android

## For watchers who wants to use this
- No binary is provided
  - Please compile yourself
- This is not well-tested and adjusted for some devices
  - This app may not work on your device
- Untrusted code could run on this app
  - Running this with root permission requires more attention
- This is intended to be only used by me
  - Some UIs may feel you bad
  - May not be easy to use
- There's so many dirty code
  - You may not understand my code

## Disable including optional libraries
### Patch
Patch the following:
```diff
diff --git a/app/build.gradle b/app/build.gradle
index de9cbfe..a9facda 100644
--- a/app/build.gradle
+++ b/app/build.gradle
@@ -39,7 +39,7 @@ android {
 def groovyVersion='2.4.10'
 def appCompatVersion='25.3.0'
 
-def includeOptionalLibs=true
+def includeOptionalLibs=false
 
 dependencies {
     compile fileTree(dir: 'libs', include: ['*.jar'])
```
### Do by hand
Change `includeOptionalLibs=true` to `includeOptionalLibs=false` in `app/build.gradle`

## Usage
Check the code at all, or compile and open it

## Compile
Change the current directory where this repo was cloned, and type the following on the shell: (Change `assembleDebug` if you need)     
- On Windows, `gradlew.bat assembleDebug`
- On Linux, `./gradlew assembleDebug`

You'll see `app-debug.apk` at `(Repo Directory)/build/outputs/apk` (Please just ignore other files.)    
