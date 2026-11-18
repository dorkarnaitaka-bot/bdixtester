package com.jubayerhossain.bdixtester.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.jubayerhossain.bdixtester.R

object ExternalBrowserHelper {

    fun isAllowedBrowserUrl(rawUrl: String): Boolean {
        val uri = try {
            Uri.parse(rawUrl.trim())
        } catch (_: Exception) {
            null
        } ?: return false
        val scheme = uri.scheme?.lowercase() ?: return false
        if (scheme != "http" && scheme != "https") return false
        return !uri.host.isNullOrBlank()
    }

    fun openUrl(context: Context, rawUrl: String): Boolean {
        val trimmed = rawUrl.trim()
        val uri = try {
            Uri.parse(trimmed)
        } catch (_: Exception) {
            null
        }
        val scheme = uri?.scheme?.lowercase()
        if (scheme == "ftp" || scheme == "ftps") {
            Toast.makeText(
                context,
                context.getString(R.string.details_open_browser_ftp_unsupported),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (!isAllowedBrowserUrl(trimmed) || uri == null) {
            Toast.makeText(
                context,
                context.getString(R.string.details_open_browser_invalid_scheme),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.details_open_browser_error),
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }
}
