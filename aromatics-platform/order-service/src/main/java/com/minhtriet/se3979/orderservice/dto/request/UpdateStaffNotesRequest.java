package com.minhtriet.se3979.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStaffNotesRequest {
    @JsonAlias({"note", "staffNotes"})
    private String staffNotes;
}
