package ru.mralexeimk.yedom.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

/**
 * Controller for handling errors
 * @author mralexeimk
 */
@ControllerAdvice
public class ErrorsController {
    @ExceptionHandler(IOException.class)
    public String handleAbortedConnection(final IOException ex) {
        if (ex.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException")) {
            return null;
        }

        return "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex) {
        ex.printStackTrace();
        return "redirect:/";
    }
}
