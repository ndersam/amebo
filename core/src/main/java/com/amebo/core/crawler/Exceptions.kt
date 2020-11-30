package com.amebo.core.crawler

class ParseException(msg: String): Exception(msg)
object TopicLockedException: Exception()
class UnauthorizedAccessException: Exception()
