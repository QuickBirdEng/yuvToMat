#YuvToMat

## Usage
### Kotlin
Simply use the extension function on android.media.Image! 

```kotlin
val mat = image.toMat()
```

### Java
Simply use the static function ```Yuv.toMat```

```java
Mat mat = Yuv.toMat(image)
```


## Get the the dependency

### Gradle
```
dependencies {
    ...
    implementation "com.quickbirdstudios:yuvtomat:0.1.0"
}
```

Also include the kotlin standard library for non-kotlin projects

```
dependencies {
    ...
    implementationn "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"

}
```