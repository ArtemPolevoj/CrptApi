package org.example;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {

        CrptApi api = new CrptApi(TimeUnit.SECONDS, 10);
        Object document = createDocument(); // Create your document object here
        String signature = "your_signature";
        api.createDocument(document, signature);


    }
}