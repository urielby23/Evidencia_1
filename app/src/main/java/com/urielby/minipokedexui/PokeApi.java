package com.urielby.minipokedexui;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PokeApi {
    @GET("pokemon/{query}")
    Call<PokemonResponse> getPokemon(@Path("query") String query);
}
