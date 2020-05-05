# YuvToMat

# ⚠️ DEPRECATED 
This library is no longer maintained and will not be updated.

### YUV_420_888 Image to OpenCV RGB Mat Conversion
High-performance library for converting YUV_420_888 images from Android's Camera v2 API to OpenCV Mats. The resulting Mat contains RGB pixels.
  
## Usage
### Kotlin
Simply use the extension function on android.media.Image.  

```kotlin
val mat = image.rgb()
```

### Java
Simply use the static function ```Yuv.toMat```

```java
Mat mat = Yuv.rgb(image)
```

### Clipping the image
This library supports efficient clipping of the image before converting. 
Just pass a clip to the function:

```kotlin
val yuv = YuvImage(image).clip(left=20, top=20, right=40, bottom=40)
val yuv = YuvImage(image).with(YuvImage.Clip(left=20, top=20, right=40, bottom=40))
val yuv = YuvImage(image) with YuvImage.Clip(left=20, top=20, right=40, bottom=40)
val rgb: Mat = yuv.rgb()
```

## Get the the dependency

### Gradle
```
dependencies {
    ...
    implementation "com.quickbirdstudios:yuvtomat:1.1.0"
}
```

Also include the kotlin standard library for non-kotlin projects

```
dependencies {
    ...
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"

}
```

If you encounter any issues with this library, please submit an issue. We'll come back to you as soon as possible.
