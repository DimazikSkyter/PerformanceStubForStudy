package ru.study.api.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.study.api.dto.EventDto;
import ru.study.api.dto.EventResponse;
import ru.study.api.dto.SeatDto;
import ru.study.api.dto.SeatResponse;
import ru.study.api.service.EventService;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(value = EventController.class)
class EventControllerTest {


    @MockBean
    EventService eventService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void clientCanGetEventsOfDateAndMerchant() throws Exception {
        String dateStr = "2022-03-04";
        String merchant = "testMerchant";
        String name = "test event";

        Mockito
                .when(eventService.getEventsByMerchantAndDate(eq(merchant), any()))
                .thenReturn(EventResponse.success(List.of(new EventDto(name,
                        name,
                        new GregorianCalendar(2022, Calendar.MARCH, 4, 1, 1).getTime()))));
        mockMvc.perform(get("/events/list")
                        .param("merchant", merchant)
                        .param("date", dateStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events.*.eventName").value(name))
                .andExpect(jsonPath("$.events.*.eventDate").value(dateStr))
                .andDo(print());
    }

    @Test
    void clientCanGetEventFreeSeats() throws Exception {
        String name = "some event name";
        SeatResponse seatResponse = new SeatResponse(name, null, Set.of(new SeatDto("3A", 11.2), new SeatDto("5A", 13.2)));

        Mockito.when(eventService.getSeatsFromEvent(eq(name))).thenReturn(seatResponse);

        mockMvc.perform(get("/events/" + name + "/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seats[?(@.place == '3A')].place").exists())
                .andExpect(jsonPath("$.seats[?(@.price == 11.2)].price").exists())
                .andDo(print());
    }
}