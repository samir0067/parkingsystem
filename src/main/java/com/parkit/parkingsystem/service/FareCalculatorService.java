package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class FareCalculatorService {

    private static final Logger LOGGER = getLogger(FareCalculatorService.class);
    private final TicketDAO ticketDao;

    public FareCalculatorService(TicketDAO ticketDAO) {
        this.ticketDao = ticketDAO;
    }

    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            assert ticket.getOutTime() != null;
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        boolean isFree = false;

        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();

        long diffTime = outTime - inTime;
        long diffTimeMinute = (diffTime / 60 / 1000);

        if (diffTimeMinute <= 30) {
            // We calculate minutes since this is the same "hour"
            ticket.setPrice(0);
            isFree = true;
        }

        long timeToInvoiceInMinutes = (diffTime) / 1000 / 60;
        double timeToInvoiceInHours = timeToInvoiceInMinutes / 60.0;

        boolean isDiscount = false;

        Ticket ticketInDB = ticketDao.getTicket(ticket.getVehicleRegNumber());

        if (ticketInDB != null) {
            isDiscount = true;
        }

        if (!isFree) {
            double total = 0;
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    double subTotalCar = timeToInvoiceInHours * Fare.CAR_RATE_PER_HOUR;
                    total = (isDiscount ? (subTotalCar - subTotalCar * Fare.DISCOUNT) : subTotalCar);
                    break;
                }
                case BIKE: {
                    double subTotalBike = timeToInvoiceInHours * Fare.BIKE_RATE_PER_HOUR;
                    total = (isDiscount ? (subTotalBike - subTotalBike * Fare.DISCOUNT) : subTotalBike);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }
            ticket.setPrice(total);
        }
    }
}