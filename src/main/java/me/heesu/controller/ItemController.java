package me.heesu.controller;

import me.heesu.domain.Item;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ItemController {

    WebClient webClient = WebClient.create("http://localhost:8080"); //base url

    //todo retrieve, exchange 구분
    /*
      retrieve -> response body 를 넘겨줌
      exchange -> ClientResponse 를 넘겨줌
     */

    @GetMapping("/client/retrieve")
    public Flux<Item> getAllItemsUsingRetrieve(){

       return webClient.get().uri("/v1/items")
                .retrieve()
                .bodyToFlux(Item.class)
                .log("Retrieve : ");
    }

    @GetMapping("/client/exchange")
    public Flux<Item> getAllItemsUsingExchange(){

        return webClient.get().uri("/v1/items")
                //.exchange()  deprecate
                .exchangeToFlux(response -> {
                    return response.bodyToFlux(Item.class);
                })
                .log("Exchange : ");
    }
}
