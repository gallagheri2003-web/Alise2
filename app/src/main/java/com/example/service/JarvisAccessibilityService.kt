package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ScreenContext(
    val currentPackage: String = "",
    val currentActivityTitle: String = "",
    val readableTexts: List<String> = emptyList(),
    val isServiceActive: Boolean = false,
    val lastUpdated: Long = 0L
)

class JarvisAccessibilityService : AccessibilityService() {

    companion object {
        private val _screenContext = MutableStateFlow(ScreenContext())
        val screenContext: StateFlow<ScreenContext> = _screenContext.asStateFlow()

        var activeInstance: JarvisAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        activeInstance = this
        _screenContext.value = _screenContext.value.copy(isServiceActive = true)

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: ""
        val className = event.className?.toString() ?: ""

        val texts = mutableListOf<String>()
        rootInActiveWindow?.let { root ->
            extractNodeTexts(root, texts, maxDepth = 4)
        }

        _screenContext.value = ScreenContext(
            currentPackage = pkgName,
            currentActivityTitle = className,
            readableTexts = texts.take(30),
            isServiceActive = true,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun extractNodeTexts(node: AccessibilityNodeInfo, list: MutableList<String>, maxDepth: Int) {
        if (maxDepth <= 0 || list.size >= 30) return

        node.text?.toString()?.takeIf { it.isNotBlank() }?.let {
            if (!list.contains(it)) list.add(it)
        }
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let {
            if (!list.contains(it)) list.add(it)
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                extractNodeTexts(child, list, maxDepth - 1)
                child.recycle()
            }
        }
    }

    override fun onInterrupt() {
        _screenContext.value = _screenContext.value.copy(isServiceActive = false)
        activeInstance = null
    }

    override fun onDestroy() {
        super.onDestroy()
        _screenContext.value = _screenContext.value.copy(isServiceActive = false)
        activeInstance = null
    }

    fun performBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun performHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)
    fun performRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)
    fun performNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
}
