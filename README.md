Android MJPEG Viewer
====================

This is an app for viewing an MJPEG video stream on Google Glass and alike.
I used this to stream video from my smartphone employing [IP Webcam](https://play.google.com/store/apps/details?id=com.pas.webcam), which offers an MJPEG stream via HTTP.
The viewer consumes that stream, decodes it an renders it to a full screen view.

![Viewer in Action](https://github.com/schulzp/uni-android-mjpeg-viewer/raw/gh-pages/report/figures/images/Live-Video.jpg)

This project was created as part of an [independent study](https://github.com/schulzp/uni-android-mjpeg-viewer/raw/gh-pages/report/report.pdf) at University of Bremen.

# Native Libraries

The MJPEG decoder is based on [TurboJPEG](https://libjpeg-turbo.org/About/TurboJPEG), which is not part of this repository.
Either build it yourself or look around for pre-built versions, e.g. [TurboJpeg-Android](https://github.com/chendongMarch/TurboJpeg-Android).
