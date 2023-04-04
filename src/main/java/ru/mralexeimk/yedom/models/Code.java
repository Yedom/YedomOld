package ru.mralexeimk.yedom.models;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Model of sending code (to confirm email)
 */
@Data
public class Code {

    @NotEmpty(message = "{auth.code.empty}")
    private String code;

    private Long startTime;

    public Code() {
        code = "";
        startTime = System.currentTimeMillis();
    }

    public Code(String code) {
        this.code = code;
        update();
    }

    public void update() {
        startTime = System.currentTimeMillis();
    }
}
