package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        String route = "";
        boolean flag = false;
        for (Station station : trainEntryDto.getStationRoute()) {
            if(flag)
            {
                route+=',';
            }
            route+=station.toString();
            flag= true;
        }
        Train train = new Train();
        train.setRoute(route.toString());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        Train savedTrain = trainRepository.save(train);
        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats available in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //In short : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();

        int totalSeats= train.getNoOfSeats();
        int bookings= 0;
        String route = train.getRoute();

        int boardingStationIndex = route.indexOf(seatAvailabilityEntryDto.getFromStation().toString());
        int destinationStationIndex = route.indexOf(seatAvailabilityEntryDto.getToStation().toString());

        for(Ticket ticket:train.getBookedTickets())
        {

            int startIndexOfTicket = route.indexOf(ticket.getFromStation().toString());
            int endIndexOfTicket =  route.indexOf(ticket.getToStation().toString());

            if((startIndexOfTicket < destinationStationIndex && startIndexOfTicket >= boardingStationIndex) ||
                    (endIndexOfTicket > boardingStationIndex && endIndexOfTicket <= destinationStationIndex) ||
                    (startIndexOfTicket <= boardingStationIndex && endIndexOfTicket >= destinationStationIndex))
            {
                bookings+=ticket.getPassengersList().size();
            }
        }
        int remainingSeats = totalSeats - bookings;
        return remainingSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train = trainRepository.findById(trainId).get();
        String boardingStation = station.toString();
        boolean flag = false;
        String comp = "";
        if(!train.getRoute().contains(boardingStation))
        {
            throw new Exception("Train is not passing from this station");
        }
        List<Ticket> tickets = train.getBookedTickets();
        Integer count = 0;
        for (Ticket ticket:tickets) {
            if(ticket.getFromStation().toString().equals(boardingStation))
            {
                count = count + ticket.getPassengersList().size();
            }
        }
        return count;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Train train = trainRepository.findById(trainId).get();
        List<Ticket> tickets= train.getBookedTickets();
        int maxAge = 0;
        for (Ticket ticket : tickets) {
            List<Passenger> passengers = ticket.getPassengersList();
            for (Passenger passenger : passengers) {
                maxAge = Math.max(maxAge,passenger.getAge());
            }
        }
        return maxAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milliseconds value will be 0 in a LocalTime format.

        int startMin = startTime.getHour()*60 + startTime.getMinute();
        int endMin = endTime.getHour()*60 +endTime.getMinute();

        List<Train> trains = trainRepository.findAll();
        List<Integer> trainIdList = new ArrayList<>();
        String currentStation = station.toString();
        for (Train train: trains) {
            String route = train.getRoute();
            String[] results = route.split(",");
            int extraHours = 0;
            for(String st: results)
            {
                if(st.equals(currentStation))
                {
                    break;
                }
                extraHours++;
            }

            int totalHOurs = train.getDepartureTime().getHour() + extraHours;
            int totalMinutes = train.getDepartureTime().getMinute();

            int time = totalHOurs*60 + totalMinutes;

            if(time >= startMin && time <= endMin)
            {
                trainIdList.add(train.getTrainId());
            }
        }
        return trainIdList;
    }
}
