package com.exchange.trader.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record RegisterRequest(@NotBlank
                                  @Size(min = 3, max = 30,message = "Mininmum ve maksimum aralıkta değer giriniz.") String userName,     @NotBlank(message = "Email boş olamaz")
@Email(message = "Geçerli bir email giriniz") String email,
                              @NotBlank
                              @Size(min = 6,message = "En az 6 karakterli olmalı") String password,
                              @NotNull
                              @JsonProperty("role_id") Long roleId) {
}