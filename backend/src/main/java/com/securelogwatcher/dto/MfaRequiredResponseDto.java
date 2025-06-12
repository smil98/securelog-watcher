package com.securelogwatcher.dto;

import com.securelogwatcher.domain.MfaType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MfaRequiredResponseDto {
    private String username;
    private MfaType mfaType;
    private String mfaToken;
}
