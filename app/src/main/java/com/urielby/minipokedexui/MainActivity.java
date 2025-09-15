package com.urielby.minipokedexui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etQuery;
    private Button btnSearch, btnClear;
    private ImageButton btnRandom;
    private CheckBox cbShiny;
    private ToggleButton tgSpriteArtwork;
    private RadioGroup rgSide;
    private RadioButton rbFront, rbBack;
    private Switch swDetails;
    private ImageView ivPokemon;
    private TextView tvBasic, tvDetails;
    private ProgressBar progressBar;

    private PokeApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // EdgeToEdge y padding
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        etQuery = findViewById(R.id.etQuery);
        btnSearch = findViewById(R.id.btnSearch);
        btnClear = findViewById(R.id.btnClear);
        btnRandom = findViewById(R.id.btnRandom);
        cbShiny = findViewById(R.id.cbShiny);
        tgSpriteArtwork = findViewById(R.id.tgSpriteArtwork);
        rgSide = findViewById(R.id.rgSide);
        rbFront = findViewById(R.id.rbFront);
        rbBack = findViewById(R.id.rbBack);
        swDetails = findViewById(R.id.swDetails);
        ivPokemon = findViewById(R.id.ivPokemon);
        tvBasic = findViewById(R.id.tvBasic);
        tvDetails = findViewById(R.id.tvDetails);

        progressBar = findViewById(R.id.progressBar); // Agrega ProgressBar en tu XML
        progressBar.setVisibility(View.GONE);

        // Retrofit
        api = ApiClient.getClient().create(PokeApi.class);

        // Botón Buscar
        btnSearch.setOnClickListener(v -> {
            String query = etQuery.getText().toString().trim().toLowerCase();
            if (!query.isEmpty()) {
                if (isNetworkAvailable()) fetchPokemon(query);
                else Toast.makeText(this, "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Escribe un nombre o ID", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón Aleatorio
        btnRandom.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                int randomId = new Random().nextInt(1010) + 1;
                fetchPokemon(String.valueOf(randomId));
            } else {
                Toast.makeText(this, "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón Limpiar
        btnClear.setOnClickListener(v -> clearUI());

        // Switch detalles
        swDetails.setOnCheckedChangeListener((buttonView, isChecked) ->
                tvDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE)
        );

        // Toggle Artwork deshabilita RadioButtons
        tgSpriteArtwork.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rbFront.setEnabled(!isChecked);
            rbBack.setEnabled(!isChecked);
        });
    }

    private void fetchPokemon(String query) {
        progressBar.setVisibility(View.VISIBLE);

        api.getPokemon(query).enqueue(new Callback<PokemonResponse>() {
            @Override
            public void onResponse(Call<PokemonResponse> call, Response<PokemonResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    PokemonResponse p = response.body();
                    tvBasic.setText(p.name + " / ID: " + p.id);

                    StringBuilder details = new StringBuilder();
                    details.append("Peso: ").append(p.weight).append("\nTipos: ");
                    for (PokemonResponse.TypeWrapper t : p.types) {
                        details.append(t.type.name).append(" ");
                    }
                    details.append("\nHabilidades: ");
                    for (PokemonResponse.AbilityWrapper a : p.abilities) {
                        details.append(a.ability.name).append(" ");
                    }
                    tvDetails.setText(details.toString());

                    // Selección de imagen
                    String url;
                    boolean shiny = cbShiny.isChecked();
                    boolean artwork = tgSpriteArtwork.isChecked();
                    boolean front = rbFront.isChecked();

                    if (artwork) {
                        url = p.sprites.other.officialArtwork.frontDefault;
                    } else {
                        url = front ? (shiny ? p.sprites.front_shiny : p.sprites.front_default)
                                : (shiny ? p.sprites.back_shiny : p.sprites.back_default);
                    }

                    Glide.with(MainActivity.this)
                            .load(url)
                            .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(ivPokemon);

                } else if (response.code() == 404) {
                    Toast.makeText(MainActivity.this, "Pokémon no encontrado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error al obtener Pokémon", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PokemonResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearUI() {
        etQuery.setText("");
        cbShiny.setChecked(false);
        tgSpriteArtwork.setChecked(false);
        rbFront.setChecked(true);
        swDetails.setChecked(false);
        tvBasic.setText("Nombre / ID");
        tvDetails.setText("Detalles (tipo, peso, habilidades...)");
        ivPokemon.setImageResource(android.R.color.transparent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
