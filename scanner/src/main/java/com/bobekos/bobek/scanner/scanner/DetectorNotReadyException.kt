package com.bobekos.bobek.scanner.scanner

/**
 * When an app is first installed, it may be necessary to download required files.
 * If this is throw, those files are not yet available.
 * Usually this download is taken care of at application install time, but this is not guaranteed.
 * In some cases the download may have been delayed.
 */
class DetectorNotReadyException : Exception()