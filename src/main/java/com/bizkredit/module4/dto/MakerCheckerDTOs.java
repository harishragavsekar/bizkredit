package com.bizkredit.module4.dto;

import com.bizkredit.common.enums.MakerCheckerAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MakerCheckerDTOs {

    public record MakerCheckerRequest(
            @NotBlank String entityType,
            Long entityId,
            @NotNull MakerCheckerAction action,
            String payloadJson,
            @NotBlank String requiredCheckerRole
    ) {}

    public record MakerCheckerActionRequest(
            String comments
    ) {}
}
