package com.example.hdfc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreditResponse {
    private String status;
    private ResponseCodeType responseCode;
    private String upiTxnId;
    private String rrn;
    private String bankTxnId;
    private String failureReason;

    public enum ResponseCodeType{
        U03, //for account not found
        U02, //for account is inactive
        U00  //for success
    }
}
