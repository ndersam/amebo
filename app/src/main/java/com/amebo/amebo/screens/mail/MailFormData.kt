package com.amebo.amebo.screens.mail

class MailFormData(title: String, body: String, val editable: Boolean) {
    var title: String = title
        set(value) {
            if (editable) {
                field = value
            }
        }
    var body: String = body
        set(value) {
            if (editable) {
                field = value
            }
        }
}