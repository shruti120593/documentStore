package com.shruti.storage.documentstorage.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by user on 4/17/19.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextResponse {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
