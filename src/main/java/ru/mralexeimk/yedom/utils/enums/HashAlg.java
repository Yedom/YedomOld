package ru.mralexeimk.yedom.utils.enums;

public enum HashAlg {
    MD5("MD5"),
    SHA256("SHA-256");

    private final String name;

    HashAlg(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
