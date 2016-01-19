package com.imslbd.call_center;

/**
 * Created by someone on 12/11/2015.
 */
public enum MyMessageCodes {
    fileuploadSuccess("File uploaded successfully.", 1),
    file_already_exists("File already exists.", 2),
    filename_invalid("File name is invalid.", 3),
    validation_error("Validation Failed.", 4),
    parse_error("Parse Failed.", 5),
    UNEXPECTED_ERROR("Unknown Error.", 6),
    filename_empty("Filename is empty.", 7),
    empty_file("File size is zero.", 8),
    ok("Request processed successfully.", 9),
    NOT_FOUND("No result found.", 10),
    ALREADY_UPDATED("Already updated", 11),
    VALUE_ALREADY_EXISTS("Value Already Exists.", 12);

    public final String code;
    public final String message;
    public final int id;

    MyMessageCodes(String message, int id) {
        this.id = id;
        this.code = name();
        this.message = message;
    }
}
