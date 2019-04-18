package com.shruti.storage.documentstorage.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by user on 4/17/19.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentResponse {

    private String id;
    private String errorMessage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
