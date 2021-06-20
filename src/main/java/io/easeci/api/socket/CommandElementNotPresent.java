package io.easeci.api.socket;

public class CommandElementNotPresent extends Exception {

    private String input;
    private String propertyKey;

    public CommandElementNotPresent(String input, String propertyKey) {
        this.input = input;
        this.propertyKey = propertyKey;
    }

    @Override
    public String getMessage() {
        return "Could not find property with name: '" + this.propertyKey + "'" + " in command: '" + this.input + "'";
    }
}
