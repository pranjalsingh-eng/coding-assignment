package com.example.iceandfire.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Character {

    @JsonProperty("url")
    private String apiUrl;

    private String name;
    private String gender;
    private String culture;
    private String born;
    private String died;
    private List<String> aliases;
    private List<String> titles;
    private String father;
    private String mother;
    private String spouse;
    private List<String> allegiances;
    private List<String> books;
    private List<String> povBooks;
    private List<String> tvSeries;       // used to count season appearances
    private List<String> playedBy;


    public int getSeasonCount() {
        if (tvSeries == null) return 0;
        return (int) tvSeries.stream()
                .filter(s -> s != null && !s.isBlank())
                .count();
    }

    public String getDisplayName() {
        if (name != null && !name.isBlank()) return name;
        if (aliases != null && !aliases.isEmpty()) return aliases.get(0);
        return "Unknown";
    }
}
