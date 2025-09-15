package com.urielby.minipokedexui;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PokemonResponse {
    public int id;
    public String name;
    public int weight;
    public Sprites sprites;
    public List<TypeWrapper> types;
    public List<AbilityWrapper> abilities;

    public static class Sprites {
        public String front_default;
        public String back_default;
        public String front_shiny;
        public String back_shiny;
        public Other other;

        public static class Other {
            @SerializedName("official-artwork")
            public OfficialArtwork officialArtwork;

            public static class OfficialArtwork {
                @SerializedName("front_default")
                public String frontDefault;
            }
        }
    }

    public static class TypeWrapper {
        public Type type;
        public static class Type {
            public String name;
        }
    }

    public static class AbilityWrapper {
        public Ability ability;
        public static class Ability {
            public String name;
        }
    }
}
