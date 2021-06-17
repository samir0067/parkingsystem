package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.commons.lang.time.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final Logger LOGGER = getLogger(InputReaderUtil.class);
    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("care-68");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void TestParkingSpotForCar() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Number additionalSpotParking = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        parkingService.processIncomingVehicle();
        Ticket ticket = ticketDAO.getTicket("care-68");
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        assertNotNull(ticket);
        assertNotNull(parkingSpot);
        assertFalse(parkingSpot.isAvailable());
        assertEquals("care-68", ticket.getVehicleRegNumber());
        assertEquals(additionalSpotParking, parkingSpot.getId());
        // TODO: check that a ticket is actually saved in DB and Parking table is updated with availability
        LOGGER.info("\n le ticket N°: " + ticket.getId() + " du véhicule immatriculé: " + ticket.getVehicleRegNumber()
                + "\n utilise la place de stationnement N°: " + parkingSpot.getId()
                + "\n ainsi, confirme qu'elle n'est plus disponible: " + parkingSpot.isAvailable()
                + "\n et que la place utiliser n° " + parkingSpot.getId() + " et identique la place de stationnement supplémentaire N°: " + additionalSpotParking);

    }

    @Test
    public void testPriceAndTimeToExitTheParkingLot() throws InterruptedException {
        TestParkingSpotForCar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Ticket ticket = ticketDAO.getTicket("care-68");
        Date outTimeParam = DateUtils.addHours(ticket.getInTime(), 2);
        parkingService.processExitingVehicle(outTimeParam);
        ticket = ticketDAO.getTicket("care-68");
        assertNotNull(ticket);
        assertNotNull(ticket.getInTime());
        assertNotNull(ticket.getOutTime());

        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();
        long diffTime = outTime - inTime;
        long timeToInvoiceInMinutes = (diffTime) / 1000 / 60;
        double timeToInvoiceInHours = timeToInvoiceInMinutes / 60.0;
        double total = timeToInvoiceInHours * Fare.CAR_RATE_PER_HOUR;

        assertEquals(total, ticket.getPrice());
        //TODO: check that the fare generated and out time are populated correctly in the database
        LOGGER.info("\n le ticket N°: " + ticket.getId() + " arriver à: " + ticket.getInTime()
                + "\n et bien partir à: " + ticket.getOutTime()
                + "\n ainsi, confirme que le prix du ticket sera de: " + ticket.getPrice()
                + "\n et que le prix attendu et de => " + total
                + " qui correspond bien au prix obtenu de => " + ticket.getPrice());

    }

}
