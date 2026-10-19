package com.app.client;

import com.app.config.MercadoPagoFeignConfig;
import com.app.dto.MercadoPagoPaymentResponse;
import com.app.dto.MercadoPagoPreferenceRequest;
import com.app.dto.MercadoPagoPreferenceResponse;
import com.app.dto.MercadoPagoRefundResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
        name = "mercadoPago",
        url = "${mercadopago.api-url:https://api.mercadopago.com}",
        configuration = MercadoPagoFeignConfig.class
)
public interface MercadoPagoClient {

    @PostMapping("/checkout/preferences")
    MercadoPagoPreferenceResponse crearPreferencia(@RequestBody MercadoPagoPreferenceRequest request);

    @GetMapping("/v1/payments/{id}")
    MercadoPagoPaymentResponse obtenerPago(@PathVariable("id") String id);

    @PostMapping("/v1/payments/{id}/refunds")
    MercadoPagoRefundResponse reembolsar(@PathVariable("id") String id);
}
