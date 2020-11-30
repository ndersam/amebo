package com.amebo.amebo.screens.newpost

import com.amebo.core.domain.Board

sealed class FormData(var body: String, var title: String, open val titleIsEditable: Boolean)
class NewPostFormData(body: String = "", title: String = "", var followTopic: Boolean = true) :
    FormData(body, title, titleIsEditable = false)

class ModifyPostFormData(body: String = "", title: String = "", override var titleIsEditable: Boolean = false) :
    FormData(body, title, titleIsEditable)

class NewTopicFormData(body: String = "", title: String = "", var board: Board? = null) :
    FormData(body, title, titleIsEditable = true)