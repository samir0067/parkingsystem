package com.parkit.parkingsystem;

import com.parkit.parkingsystem.service.InteractiveShell;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class App {

    private static final Logger LOGGER = getLogger(App.class);

    public static void main(String[] args) {
        LOGGER.info("<== Initializing Parking System ==>");
        InteractiveShell.loadInterface();
    }
}
