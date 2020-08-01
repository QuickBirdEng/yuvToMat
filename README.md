# ⚠️ DEPRECATED 
This library is no longer maintained and will not be updated.

If you need converting to OpenCV Mat - take a look at official [OpenCV solution](
https://github.com/opencv/opencv/blob/master/modules/java/generator/android-21/java/org/opencv/android/JavaCamera2View.java#L344).

If you need Bitmap and you do not want to add any library into you project - RenderScript soulution in [Google's repo android/camera-samples](https://github.com/android/camera-samples/blob/2a6b5bd9b8a8d732536e65d1716c2aed1f879101/CameraUtils/lib/src/main/java/com/example/android/camera/utils/YuvToRgbConverter.kt).

If you need correct ByteBuffer for futher processing with neural network engine (for instance [MNN](https://github.com/alibaba/MNN/)) - [single file snippet](https://github.com/gordinmitya/yuv2buf).


# YuvToMat
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
