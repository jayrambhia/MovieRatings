package com.fenchtose.movieratings.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue

class LintRulesRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = arrayListOf(TextAppearanceDetector.ISSUE)
}