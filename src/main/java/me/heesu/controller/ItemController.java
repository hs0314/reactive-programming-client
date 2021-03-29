package me.heesu.controller;

import lombok.extern.slf4j.Slf4j;
import me.heesu.domain.Item;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
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

    @GetMapping("/client/retrieve/singleItem")
    public Mono<Item> getItemUsingRetrieve(){

        String id = "item4";

        return webClient.get().uri("/v1/items/{id}", id)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Retrieve : ");
    }

    @GetMapping("/client/exchange/singleItem")
    public Mono<Item> getItemUsingExchange(){

        String id = "item4";

        return webClient.get().uri("/v1/items/{id}", id)
                .exchangeToMono(response -> response.bodyToMono(Item.class))
                .log("Retrieve : ");
    }

    @PostMapping("/client/createItem")
    public Mono<Item> createItem(@RequestBody Item item){

        Mono<Item> itemMono = Mono.just(item);

        return webClient.post().uri("/v1/items")
                .contentType(MediaType.APPLICATION_JSON)
                .body(itemMono, Item.class)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Created item : ");
    }

    @PutMapping("/client/updateItem/{id}")
    public Mono<Item> updateItem(@PathVariable String id,
                                 @RequestBody Item item){

        Mono<Item> itemBody = Mono.just(item);
        return webClient.put().uri("/v1/items/{id}", id)
                .body(itemBody, Item.class)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Updated Item : ");

    }

    @DeleteMapping("/client/deleteItem/{id}")
    public Mono<Void> deleteItem(@PathVariable String id){

        return webClient.delete().uri("/v1/items/{id}",id)
                .retrieve()
                .bodyToMono(Void.class)
                .log("Deleted Item : ");
    }

    // exception handling - retrieve
    @GetMapping("/client/retrieve/error")
    public Flux<Item> errorRetrieve(){

        return webClient.get().uri("/v1/items/exception")
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMono = clientResponse.bodyToMono(String.class);
                    return errorMono.flatMap(errorMessage -> {
                        log.error("The error message is : " + errorMessage);
                        throw new RuntimeException(errorMessage);
                    });
                })
                .bodyToFlux(Item.class); // return type을 맞춰주기 위해서
    }

    // exception handling - exchange
    @GetMapping("/client/exchange/error")
    public Flux<Item> errorExchange(){

        return webClient.get().uri("/v1/items/exception")
                .exchange()
                .flatMapMany(clientResponse -> {
                    if(clientResponse.statusCode().is5xxServerError()){
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(error -> {
                                    log.error(error);
                                    throw new RuntimeException(error);
                                });
                    }else{
                        return clientResponse.bodyToFlux(Item.class);
                    }

                });
    }

}
