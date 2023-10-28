package ru.nspk.performance.theatre.controllers;

import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.nspk.performance.theatre.TheatreApp;
import ru.nspk.performance.theatre.dto.ReserveResponse;
import ru.nspk.performance.theatre.service.EventService;
import ru.nspk.performance.theatre.service.PurchaseService;
import ru.nspk.performance.theatre.service.ReserveService;

import java.time.Instant;
import java.util.Set;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TheatreApp.class})
class TheatreControllerTest {

    @MockBean
    EventService eventService;

    @MockBean
    ReserveService reserveService;

    @MockBean
    PurchaseService purchaseService;

    @Autowired
    MockMvc mockMvc;

    @BeforeAll
    void init() {
    }

    @Test
    void theatreShouldReturnListOf3Events() throws Exception {
        val events = generateEvents();
        Mockito.doReturn(events).when(eventService).eventNames();

        mockMvc.perform(MockMvcRequestBuilders.get("/theatre/events"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void theatreShouldReturn9FreeSeatsOfEvents() throws Exception {
        String eventName = "Война и мир";
        val seats = generateSeats();
        Mockito.doReturn(seats).when(eventService).seats(eventName);

        mockMvc.perform(MockMvcRequestBuilders.get("/theatre/seats/" + eventName))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(9)));
    }

    @Test
    void theatreShouldReserveSeatOfEvent() throws Exception {
        String eventName = "Война и мир";
        long reserveId = 33;

        Mockito.doReturn(ReserveResponse.builder()
                        .reserveId(reserveId)
                        .nonFreeSeats(Set.of())
                        .reserveStarted(Instant.now()).build())
                .when(reserveService).reserve(eq(eventName), Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/theatre/reserve?event=" + eventName + "&seats=[\"A3\", \"A4\"]"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reserveId").value(String.valueOf(reserveId)))
                .andExpect(jsonPath("$.nonFreeSeats", hasSize(0)));
    }

    @Test
    void theatreShouldReleaseTheReserve() throws Exception {
        long reserveId = 34;

        Mockito.doNothing().when(reserveService).release(reserveId);

        mockMvc.perform(MockMvcRequestBuilders.post("/theatre/release?reserveId=" + reserveId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());

        Mockito.verify(reserveService, new Times(1)).release(34);
    }

    private Set<String> generateEvents() {
        return Set.of(
                "Евгений Онегин",
                "Война и мир",
                "Божественная комедия"
        );
    }

    private Set<String> generateSeats() {
        return Set.of(
                "A1",
                "A2",
                "A3",
                "A4",
                "A5",
                "B1",
                "B2",
                "B3",
                "B4"
        );
    }
}