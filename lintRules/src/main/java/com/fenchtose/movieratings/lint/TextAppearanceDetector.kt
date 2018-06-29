package com.fenchtose.movieratings.lint

import com.android.tools.lint.detector.api.*
import org.w3c.dom.Element

class TextAppearanceDetector : ResourceXmlDetector() {

    companion object {
        val ISSUE: Issue = Issue.create(
                "MissingTextAppearance",
                "textAppearance attribute is missing",
                "Use textAppearance attribute to provide text style",
                Category.TYPOGRAPHY,
                4,
                Severity.ERROR,
                Implementation(TextAppearanceDetector::class.java, Scope.RESOURCE_FILE_SCOPE)
        )
    }

    override fun getApplicableElements(): Collection<String>? {
        return arrayListOf("TextView", "CheckBox", "AppCompatCheckBox", "EditText")
    }

    override fun visitElement(context: XmlContext, element: Element) {
        if (!element.hasAttributeNS("http://schemas.android.com/apk/res/android", "textAppearance")) {
            context.report(ISSUE, context.getLocation(element), "missing attribute textAppearance")
        }
    }
}