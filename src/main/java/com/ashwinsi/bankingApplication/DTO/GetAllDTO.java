package com.ashwinsi.bankingApplication.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllDTO <T> {
    T data;
    int currPage;
    int size;
    int totalPages;
}
