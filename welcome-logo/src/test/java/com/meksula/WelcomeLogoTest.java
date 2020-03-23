package com.meksula;

import org.junit.Test;

public class WelcomeLogoTest {

    @Test
    public void shouldReturnLogo() {
        WelcomeLogo welcomeLogo = new WelcomeLogo();
        welcomeLogo.action();
    }
}