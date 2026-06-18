package com.pointlessgames.hexagone.share

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIView
import platform.UIKit.popoverPresentationController

class IosShareManager : ShareManager {
    @OptIn(ExperimentalForeignApi::class)
    override fun shareImage(image: ImageBitmap, title: String, text: String) {
        val skiaBitmap = image.asSkiaBitmap()
        val bytes = Image.makeFromBitmap(skiaBitmap).encodeToData(EncodedImageFormat.PNG)?.bytes ?: return
        
        val nsData = bytes.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
        }
        val uiImage = UIImage(data = nsData)
        
        val activityViewController = UIActivityViewController(
            activityItems = listOf(title, text, uiImage),
            applicationActivities = null
        )
        
        val window = UIApplication.sharedApplication.keyWindow
        val rootViewController = window?.rootViewController
        
        if (rootViewController != null) {
            activityViewController.popoverPresentationController?.sourceView = rootViewController.view
            activityViewController.popoverPresentationController?.sourceRect = rootViewController.view.bounds.useContents {
                platform.CoreGraphics.CGRectMake(
                    size.width / 2,
                    size.height,
                    0.0,
                    0.0
                )
            }
            rootViewController.presentViewController(activityViewController, animated = true, completion = null)
        }
    }
}
