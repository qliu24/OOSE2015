//-------------------------------------------------------------------------------------------------------------//
// Code based on a tutorial by Shekhar Gulati of SparkJava at
// https://blog.openshift.com/developing-single-page-web-applications-using-java-8-spark-mongodb-and-angularjs/
//-------------------------------------------------------------------------------------------------------------//

package com.oose2015.qliu24.hareandhounds;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static spark.Spark.*;

public class GameController {

    private static final String API_CONTEXT = "/hareandhounds/api";

    private final GameService gameService;

    private final Logger logger = LoggerFactory.getLogger(GameController.class);

    public GameController(GameService gameService) {
        this.gameService = gameService;
        setupEndpoints();
    }

    private void setupEndpoints() {
        post(API_CONTEXT + "/games", "application/json", (request, response) -> {
            try {
                Map<String, String> rst = gameService.createNewGame(request.body());
                response.status(201);
                return rst;
            } catch (GameService.GameServiceException ex) {
                logger.error("Failed to create new entry");
                response.status(ex.getCode());
                return Collections.EMPTY_MAP;
            }
        }, new JsonTransformer());

        get(API_CONTEXT + "/games/:id/board", "application/json", (request, response) -> {
            try {
                return gameService.findBoard(request.params(":id"));
            } catch (GameService.GameServiceException ex) {
                logger.error(String.format("Fail to find board for game id: %s", request.params(":id")));
                response.status(ex.getCode());
                return Collections.EMPTY_MAP;
            }
        }, new JsonTransformer());

        get(API_CONTEXT + "/games/:id/state", "application/json", (request, response)-> {
            try {
                return gameService.findState(request.params(":id")) ;
            } catch (GameService.GameServiceException ex) {
                logger.error(String.format("Fail to find state for game id: %s", request.params(":id")));
                response.status(ex.getCode());
                return Collections.EMPTY_MAP;
            }
        }, new JsonTransformer());

        put(API_CONTEXT + "/games/:id", "application/json", (request, response) -> {
            try {
                return gameService.joinGame(request.params(":id"));
            } catch (GameService.GameServiceException ex) {
                logger.error(ex.getMessage());
                response.status(ex.getCode());
                return Collections.EMPTY_MAP;
            }
        }, new JsonTransformer());
        
        post(API_CONTEXT + "/games/:id/turns", "application/json", (request, response) -> {
        	try {
                return gameService.playGame(request.params(":id"), request.body());
            } catch (GameService.GameServiceException ex) {
                logger.error(ex.getMessage());
                response.status(ex.getCode());
                if (ex.getCode()==404 || ex.getCode()==422) {
	                Map<String, String> rst = new LinkedHashMap<String,String>();
	                rst.put("reason", ex.getMessage());
	                return rst;
                }
                return Collections.EMPTY_MAP;
            }
        }, new JsonTransformer());

    }
}
