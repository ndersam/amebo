package com.amebo.core.crawler

import org.jsoup.nodes.Element

fun Element.isTag(tag: String) = tagName().equals(tag, true)
fun Element.hasClass(className: String) = classNames().any { it.equals(className, true) }