package com.argumentor.app.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SearchUtilsTest {

    @Test
    fun `isSafeFtsQuery returns true for empty query`() {
        assertThat(SearchUtils.isSafeFtsQuery("")).isTrue()
        assertThat(SearchUtils.isSafeFtsQuery("   ")).isTrue()
    }

    @Test
    fun `isSafeFtsQuery returns true for simple words`() {
        assertThat(SearchUtils.isSafeFtsQuery("hello")).isTrue()
        assertThat(SearchUtils.isSafeFtsQuery("hello world")).isTrue()
        assertThat(SearchUtils.isSafeFtsQuery("test123")).isTrue()
    }

    @Test
    fun `isSafeFtsQuery returns false for FTS special characters`() {
        assertThat(SearchUtils.isSafeFtsQuery("test*")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("test-")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("\"test\"")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("(test)")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("[test]")).isFalse()
    }

    @Test
    fun `isSafeFtsQuery returns false for FTS operators`() {
        assertThat(SearchUtils.isSafeFtsQuery("test AND query")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("test OR query")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("NOT test")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("test NEAR query")).isFalse()
    }

    @Test
    fun `isSafeFtsQuery returns false for NEAR with distance`() {
        assertThat(SearchUtils.isSafeFtsQuery("test NEAR/5 query")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("test near/10 query")).isFalse()
    }

    @Test
    fun `isSafeFtsQuery handles case insensitive operators`() {
        assertThat(SearchUtils.isSafeFtsQuery("test and query")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("test or query")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("not test")).isFalse()
    }

    @Test
    fun `isSafeFtsQuery returns false for unbalanced quotes`() {
        assertThat(SearchUtils.isSafeFtsQuery("test \"unbalanced")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("\"test")).isFalse()
    }

    @Test
    fun `isSafeFtsQuery returns false for unbalanced brackets`() {
        assertThat(SearchUtils.isSafeFtsQuery("test [unbalanced")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("test (unbalanced")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("test ]misplaced")).isFalse()
    }

    @Test
    fun `isSafeFtsQuery returns false for consecutive operators`() {
        assertThat(SearchUtils.isSafeFtsQuery("test**")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("test--")).isFalse()
        assertThat(SearchUtils.isSafeFtsQuery("test*-*")).isFalse()
    }

    @Test
    fun `sanitizeLikeQuery escapes wildcards`() {
        assertThat(SearchUtils.sanitizeLikeQuery("100%")).isEqualTo("100\\%")
        assertThat(SearchUtils.sanitizeLikeQuery("test_value")).isEqualTo("test\\_value")
        assertThat(SearchUtils.sanitizeLikeQuery("100% complete_")).isEqualTo("100\\% complete\\_")
    }

    @Test
    fun `sanitizeLikeQuery escapes backslash first`() {
        assertThat(SearchUtils.sanitizeLikeQuery("path\\file")).isEqualTo("path\\\\file")
        assertThat(SearchUtils.sanitizeLikeQuery("100\\%")).isEqualTo("100\\\\\\%")
    }

    @Test
    fun `sanitizeLikeQuery handles empty string`() {
        assertThat(SearchUtils.sanitizeLikeQuery("")).isEmpty()
    }

    @Test
    fun `sanitizeLikeQuery preserves safe characters`() {
        val safeString = "Hello World 123"
        assertThat(SearchUtils.sanitizeLikeQuery(safeString)).isEqualTo(safeString)
    }

    @Test
    fun `isSafeFtsQuery handles escaped quotes`() {
        // Escaped quotes should not count towards balance
        assertThat(SearchUtils.isSafeFtsQuery("test\\\"escaped")).isFalse() // Contains special char
    }

    @Test
    fun `isSafeFtsQuery returns true for words containing operator substrings`() {
        // Words that contain operator strings but aren't standalone operators
        assertThat(SearchUtils.isSafeFtsQuery("android")).isTrue()
        assertThat(SearchUtils.isSafeFtsQuery("orchestra")).isTrue()
        assertThat(SearchUtils.isSafeFtsQuery("notify")).isTrue()
        assertThat(SearchUtils.isSafeFtsQuery("nearby")).isTrue()
    }
}
