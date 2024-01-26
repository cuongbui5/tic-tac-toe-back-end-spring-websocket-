package com.javamaster.controller;

import com.javamaster.controller.dto.ConnectRequest;
import com.javamaster.exception.InvalidGameException;
import com.javamaster.exception.InvalidParamException;
import com.javamaster.exception.NotFoundException;
import com.javamaster.model.Game;
import com.javamaster.model.GamePlay;
import com.javamaster.model.Player;
import com.javamaster.service.GameService;
import com.javamaster.storage.GameStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@AllArgsConstructor
@Controller
public class GameController {

    private final GameService gameService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping("/start")
    public ResponseEntity<Game> start(@RequestBody Player player) {
        log.info("start game request: {}", player);
        System.out.println(player);
        return ResponseEntity.ok(gameService.createGame(player));
    }



    @PostMapping("/connect")
    public ResponseEntity<Game> connect(@RequestBody ConnectRequest request) throws InvalidParamException, InvalidGameException {
        log.info("connect request: {}", request);
        return ResponseEntity.ok(gameService.connectToGame(request.getPlayer(), request.getGameId()));
    }


    @PostMapping("/connect/random")
    public ResponseEntity<Game> connectRandom(@RequestBody Player player) throws NotFoundException {
        Game game=gameService.connectToRandomGame(player);
        simpMessagingTemplate.convertAndSend("/topic/playing/" + game.getGameId(), game);

        return ResponseEntity.ok(game);
    }

    @MessageMapping("/gameplay")
    public Game gamePlay(GamePlay request) throws NotFoundException, InvalidGameException {
        log.info("gameplay: {}", request);
        Game game=gameService.gamePlay(request);
        simpMessagingTemplate.convertAndSend("/topic/playing/" + game.getGameId(), game);
        return game;

    }
}
