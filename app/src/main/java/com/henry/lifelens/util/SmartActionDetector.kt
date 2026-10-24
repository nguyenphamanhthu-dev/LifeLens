package com.henry.lifelens.util

import com.google.android.gms.common.util.CollectionUtils.listOf

data class SmartActionResult(
    val phoneNumber: String? = null,
    val url: String? = null,
    val date: String? = null,
    val time: String? = null,
    val address: String? = null
)

object SmartActionDetector {

    fun detect(text: String): SmartActionResult {

        return SmartActionResult(
            phoneNumber = detectPhoneNumber(text),
            url = detectUrl(text),
            date = detectDate(text),
            time = detectTime(text),
            address = detectAddress(text)
        )
    }


    // ========================================================
    // PHONE
    // ========================================================

    private fun detectPhoneNumber(
        text: String
    ): String? {

        val regex = Regex(
            """(?:\+84|0)(?:[\s.-]?\d){9,10}"""
        )

        return regex
            .find(text)
            ?.value
            ?.trim()
    }


    // ========================================================
    // URL
    // ========================================================

    private fun detectUrl(
        text: String
    ): String? {

        val regex = Regex(
            """(?i)\b((?:https?://|www\.)[^\s]+)"""
        )

        return regex
            .find(text)
            ?.value
            ?.trim()
    }


    // ========================================================
    // DATE
    // ========================================================

    private fun detectDate(
        text: String
    ): String? {

        val regex = Regex(
            """\b(0?[1-9]|[12]\d|3[01])[/.-](0?[1-9]|1[0-2])[/.-](\d{4})\b"""
        )

        return regex
            .find(text)
            ?.value
            ?.replace(".", "/")
            ?.replace("-", "/")
    }


    // ========================================================
    // TIME
    // ========================================================

    private fun detectTime(
        text: String
    ): String? {

        val regex = Regex(
            """\b([01]?\d|2[0-3])(?:[:hH])([0-5]\d)\b"""
        )

        return regex
            .find(text)
            ?.value
            ?.replace("h", ":")
            ?.replace("H", ":")
    }


    // ========================================================
    // ADDRESS
    // ========================================================

    private fun detectAddress(
        text: String
    ): String? {

        val addressKeywords = listOf(
            "đường",
            "phường",
            "xã",
            "quận",
            "huyện",
            "thành phố",
            "tp.",
            "tp ",
            "tỉnh",
            "ấp",
            "khu phố",
            "đại lộ",
            "nguyễn",
            "trần",
            "lê ",
            "võ ",
            "phan ",
            "lý ",
            "hoàng ",
            "huỳnh "
        )

        val lines = text
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val addressLines = lines.filter { line ->

            val lowerLine = line.lowercase()

            val containsKeyword =
                addressKeywords.any { keyword ->
                    lowerLine.contains(keyword)
                }

            val startsWithHouseNumber =
                Regex("""^\d+[A-Za-z]?([/-]\d+[A-Za-z]?)?\s+.+""")
                    .containsMatchIn(line)

            containsKeyword || startsWithHouseNumber
        }

        if (addressLines.isEmpty()) {
            return null
        }

        return addressLines
            .take(3)
            .joinToString(", ")
    }
}