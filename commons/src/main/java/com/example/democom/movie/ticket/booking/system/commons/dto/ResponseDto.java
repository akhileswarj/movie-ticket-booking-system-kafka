package com.example.democom.movie.ticket.booking.system.commons.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto {
    private List<String> errorMessage;
    private String errorDescription;
    private String errorStatusMssg;
    private BookingDto bookingDto;
}
