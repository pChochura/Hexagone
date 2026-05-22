package com.pointlessgames.hexagone.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

internal val dateFormat = LocalDate.Format {
    day(Padding.ZERO)
    char('.')
    monthNumber(Padding.ZERO)
}
